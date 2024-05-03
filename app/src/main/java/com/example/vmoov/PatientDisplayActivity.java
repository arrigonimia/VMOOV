package com.example.vmoov;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_display);

        String userId = getIntent().getStringExtra("userId");
        Log.d("PatientDisplayActivity", "UserID: " + userId);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference metricsRef = databaseReference.child("patientmetrics").child(userId);

        // Fetch current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate);

        // Calculate start date for three months ago
        calendar.add(Calendar.MONTH, -3);
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set the day to the first day of the month
        Date startDate = calendar.getTime();
        String startDateString = dateFormat.format(startDate);

        // Map to store counts for each month
        Map<Integer, Integer> monthCounts = new HashMap<>();

        // Fetch data for the past three months
        metricsRef.orderByChild("seizureTimes")
                .startAt(startDateString)
                .endAt(currentDateString)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Use a Map to store counts for each month
                        Map<Integer, Integer> monthCounts = new HashMap<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Check if the snapshot has the required fields
                            if (snapshot.hasChild("seizureTimes")) {
                                PatientMetrics metric = snapshot.getValue(PatientMetrics.class);
                                if (metric != null) {
                                    try {
                                        Date seizureTime = dateFormat.parse(metric.getSeizureTimes());

                                        // Check if the seizure month is within the range of the past three months
                                        calendar.setTime(seizureTime);
                                        int seizureMonth = calendar.get(Calendar.MONTH);

                                        // Update counts in the map
                                        monthCounts.put(seizureMonth, monthCounts.getOrDefault(seizureMonth, 0) + 1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        // Handle parsing error
                                    }
                                }
                            }
                        }

                        // Log the month counts
                        for (Map.Entry<Integer, Integer> entry : monthCounts.entrySet()) {
                            Log.d("SeizureData", "Month " + entry.getKey() + ": " + entry.getValue());
                        }

                        // Convert map entries to a list and sort them
                        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(monthCounts.entrySet());
                        Collections.sort(sortedEntries, (e1, e2) -> e1.getKey().compareTo(e2.getKey()));

                        // Update bar chart entries based on the sortedEntries list
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
                        }

                        BarChart barChart = findViewById(R.id.barChart2);
                        BarDataSet dataSet = new BarDataSet(entries, "");
                        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

                        BarData barData = new BarData(dataSet);
                        barChart.setData(barData);
                        barChart.invalidate();

                        YAxis yAxis = barChart.getAxisLeft();
                        yAxis.setAxisMinimum(0);
                        yAxis.setAxisMaximum(100);

                        barChart.getDescription().setText("Seizures of the past three months");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error Handling
                    }
                });

        DatabaseReference userRef = databaseReference.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        String fullName = firstName + " " + lastName;
                        TextView userNameTextView = findViewById(R.id.user_name_textview2);
                        userNameTextView.setText(fullName);
                    } else {
                        Log.e("PatientDisplayActivity", "First name or last name is null");
                    }
                } else {
                    Log.e("PatientDisplayActivity", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PatientDisplayActivity", "Database error: " + databaseError.getMessage());
            }
        });


        // Reference to "patientmetrics" node
        DatabaseReference patientMetricsRef = FirebaseDatabase.getInstance().getReference("patientmetrics").child(userId);

        patientMetricsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterable<DataSnapshot> metricSnapshots = dataSnapshot.getChildren();
                    if (metricSnapshots.iterator().hasNext()) {
                        float totalDuration = 0;
                        int nonZeroCount = 0;
                        Map<Integer, Integer> monthCount = new HashMap<>();
                        DataSnapshot latestMetricSnapshot = null;

                        for (DataSnapshot metricSnapshot : metricSnapshots) {
                            String currentSeizureTimes = metricSnapshot.child("seizureTimes").getValue(String.class);
                            Float seizureDuration = metricSnapshot.child("seizureDuration").getValue(Float.class);

                            if (seizureDuration != null && seizureDuration > 0) {
                                totalDuration += seizureDuration;
                                nonZeroCount++;
                            }

                            if (latestMetricSnapshot == null || isNewerSeizureTimes(currentSeizureTimes, latestMetricSnapshot.child("seizureTimes").getValue(String.class))) {
                                latestMetricSnapshot = metricSnapshot;
                            }

                            if (metricSnapshot.hasChild("seizureTimes")) {
                                try {
                                    Date seizureTime = dateFormat.parse(metricSnapshot.child("seizureTimes").getValue(String.class));
                                    calendar.setTime(seizureTime);
                                    int seizureMonth = calendar.get(Calendar.MONTH);
                                    monthCount.put(seizureMonth, monthCount.getOrDefault(seizureMonth, 0) + 1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        int totalSeizureCount = 0;
                        int totalMonthCount = monthCount.size();

                        for (int count : monthCount.values()) {
                            totalSeizureCount += count;
                        }

                        float averageSeizuresPerMonth = (totalMonthCount > 0) ? (float) totalSeizureCount / totalMonthCount : 0;

                        Log.d(TAG, "Total Seizure Count: " + totalSeizureCount);
                        Log.d(TAG, "Total Month Count: " + totalMonthCount);
                        Log.d(TAG, "Average Seizures per Month: " + averageSeizuresPerMonth);

                        TextView seizureMonthTextView = findViewById(R.id.seizure_month2);
                        seizureMonthTextView.setText(String.valueOf(averageSeizuresPerMonth));

                        float averageDuration = (nonZeroCount > 0) ? totalDuration / nonZeroCount : 0;
                        TextView averageDurationTextView = findViewById(R.id.duration_value2);
                        averageDurationTextView.setText(String.valueOf(averageDuration));

                        if (latestMetricSnapshot != null) {
                            String lastDate = latestMetricSnapshot.child("seizureTimes").getValue(String.class);
                            Integer lastCount = latestMetricSnapshot.child("seizureCount").getValue(Integer.class);

                            if (lastCount != null) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date lastSeizureDate = sdf.parse(lastDate);
                                    Date currentDate = new Date();

                                    long diffInMillies = currentDate.getTime() - lastSeizureDate.getTime();
                                    long daysDifference = diffInMillies / (24 * 60 * 60 * 1000);

                                    Log.d(TAG, "Last Seizure Date: " + lastSeizureDate);
                                    Log.d(TAG, "Current Date: " + currentDate);
                                    Log.d(TAG, "Days Difference: " + daysDifference);

                                    TextView lastSeizureTextView = findViewById(R.id.days);
                                    if (lastSeizureTextView != null) {
                                        lastSeizureTextView.setText(String.valueOf(daysDifference));
                                    } else {
                                        Log.e("PatientDisplayActivity", "lastSeizureTextView is null");
                                    }


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Error parsing date");
                                }
                            } else {
                                Log.e(TAG, "lastCount is null");
                            }
                        } else {
                            Log.e(TAG, "latestMetricSnapshot is null");
                        }
                    } else {
                        Log.e(TAG, "No child nodes under 'patientmetrics'");
                    }
                } else {
                    Log.e(TAG, "DataSnapshot does not exist");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors ...
            }
        });
    }

    private boolean isNewerSeizureTimes(String newSeizureTimes, String oldSeizureTimes) {
        if (newSeizureTimes == null || oldSeizureTimes == null) {
            return false; // or handle the null values as appropriate for your use case
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date newDate = dateFormat.parse(newSeizureTimes);
            Date oldDate = dateFormat.parse(oldSeizureTimes);

            // Compare the dates
            return newDate.after(oldDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing seizureTimes", e);
            return false; // Handle parsing error
        }
    }
}