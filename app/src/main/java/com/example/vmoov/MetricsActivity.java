package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MetricsActivity extends AppCompatActivity {

    private TextView trueCountTextView;
    private TextView lastSessionTextView;
    private TextView averageTimeTextView;
    private BarChart barChart;
    private BarChart barChart2;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ImageButton logOutButton;
    private ImageButton settingsButton;
    private ImageButton connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        trueCountTextView = findViewById(R.id.true_count);
        lastSessionTextView = findViewById(R.id.birth_date);
        averageTimeTextView = findViewById(R.id.average_time);
        barChart = findViewById(R.id.barChart);
        barChart2 = findViewById(R.id.barChart2);

        logOutButton = findViewById(R.id.buttonLogOut);
        settingsButton = findViewById(R.id.buttonSettings);
        connectButton = findViewById(R.id.buttonConnect);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            getGameData(userId);
        }

        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MetricsActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MetricsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MetricsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        connectButton.setOnClickListener(v -> {
            Intent intent = new Intent(MetricsActivity.this, ConnectionActivity.class);
            startActivity(intent);
        });
    }

    private void getGameData(String userId) {
        DatabaseReference gameDataRef = mDatabase.child("patientmetrics").child(userId).child("gameplaydata");

        gameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long latestStartTime = 0;
                    DataSnapshot lastGameSnapshot = null;
                    List<BarEntry> barEntries = new ArrayList<>();
                    List<BarEntry> barEntriesTrueCount = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int gameIndex = 0;
                    float maxValue = 0f;

                    List<DataSnapshot> gameList = new ArrayList<>();
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        gameList.add(gameSnapshot);
                    }

                    int totalGames = gameList.size();
                    int startIndex = Math.max(0, totalGames - 5);
                    for (int i = startIndex; i < totalGames; i++) {
                        DataSnapshot gameSnapshot = gameList.get(i);

                        double totalTime = 0;
                        int stepCount = 0;
                        int trueCount = 0;

                        for (DataSnapshot gameDetailSnapshot : gameSnapshot.getChildren()) {
                            Long startTime = gameDetailSnapshot.child("startTime").getValue(Long.class);
                            if (startTime != null && startTime > latestStartTime) {
                                latestStartTime = startTime;
                                lastGameSnapshot = gameDetailSnapshot;
                            }

                            DataSnapshot stepsSnapshot = gameDetailSnapshot.child("steps");
                            if (stepsSnapshot.exists()) {
                                for (DataSnapshot stepSnapshot : stepsSnapshot.getChildren()) {
                                    Boolean resultValue = stepSnapshot.child("result").getValue(Boolean.class);
                                    Double timeValue = stepSnapshot.child("time").getValue(Double.class);

                                    if (resultValue != null && resultValue) {
                                        trueCount++;
                                    }

                                    if (timeValue != null) {
                                        totalTime += timeValue;
                                        stepCount++;
                                    }
                                }
                            }
                        }

                        if (stepCount > 0) {
                            double averageTime = totalTime / stepCount;
                            barEntries.add(new BarEntry(gameIndex, (float) averageTime));
                            barEntriesTrueCount.add(new BarEntry(gameIndex, trueCount));
                            maxValue = Math.max(maxValue, (float) averageTime);
                            gameIndex++;

                            if (latestStartTime != 0) {
                                labels.add(convertTimestampToDate(latestStartTime));
                            }
                        }
                    }

                    if (latestStartTime != 0) {
                        String formattedDate = convertTimestampToDate(latestStartTime);
                        lastSessionTextView.setText(formattedDate);
                    } else {
                        lastSessionTextView.setText("No disponible");
                    }

                    if (lastGameSnapshot != null) {
                        calculateGameStats(lastGameSnapshot);
                    }

                    float adjustedMaxValue = maxValue * 1.1f;
                    if (adjustedMaxValue == 0) {
                        adjustedMaxValue = 1f;
                    }

                    // Configurar gráfico de tiempo promedio
                    BarDataSet barDataSet = new BarDataSet(barEntries, null);
                    barDataSet.setColors(new int[]{0xFFA36BFA, 0xFF5C4CF1});
                    barDataSet.setValueTextSize(14f);
                    barDataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.format("%.2f", value);
                        }
                    });
                    BarData barData = new BarData(barDataSet);
                    barChart.setData(barData);
                    barChart.getAxisLeft().setAxisMaximum(adjustedMaxValue);
                    barChart.getAxisLeft().setAxisMinimum(0);
                    barChart.getAxisLeft().setGranularity(0.3f);
                    barChart.getAxisRight().setEnabled(false);
                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    barChart.getXAxis().setGranularity(1f);
                    barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    barChart.getDescription().setEnabled(false);
                    barChart.setDrawGridBackground(true);
                    barChart.setGridBackgroundColor(0xFFFFFFFF);
                    barChart.setBackgroundColor(0xFFFFFFFF);
                    barChart.getAxisLeft().setDrawLabels(false);
                    barChart.getAxisLeft().setDrawGridLines(false);
                    barChart.getXAxis().setDrawGridLines(false);
                    barChart2.getXAxis().setDrawGridLines(false);
                    barChart.getAxisLeft().setDrawAxisLine(false);
                    barChart.animateY(1500);
                    barChart.invalidate();

                    // Configurar gráfico de cantidad de movimientos exitosos
                    BarDataSet barDataSetTrueCount = new BarDataSet(barEntriesTrueCount, null);
                    barDataSetTrueCount.setColors(new int[]{0xFFA36BFA, 0xFF5C4CF1});
                    barDataSetTrueCount.setValueTextSize(14f);
                    barDataSetTrueCount.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.format("%d", (int) value);
                        }
                    });
                    BarData barDataTrueCount = new BarData(barDataSetTrueCount);
                    barChart2.setData(barDataTrueCount);
                    barChart2.getAxisLeft().setAxisMinimum(0);
                    barChart2.getAxisLeft().setGranularity(1f);
                    barChart2.getAxisRight().setEnabled(false);
                    barChart2.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    barChart2.getXAxis().setGranularity(1f);
                    barChart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    barChart2.getDescription().setEnabled(false);
                    barChart2.setDrawGridBackground(true);
                    barChart2.setGridBackgroundColor(0xFFFFFFFF);
                    barChart2.setBackgroundColor(0xFFFFFFFF);
                    barChart2.getAxisLeft().setDrawLabels(false);
                    barChart2.getAxisLeft().setDrawGridLines(false);
                    barChart2.getAxisLeft().setDrawAxisLine(false);
                    barChart2.animateY(1500);
                    barChart2.invalidate();
                } else {
                    trueCountTextView.setText("No data");
                    averageTimeTextView.setText("No data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                trueCountTextView.setText("Error");
                averageTimeTextView.setText("Error");
            }
        });
    }

    private void calculateGameStats(DataSnapshot lastGameSnapshot) {
        int trueCount = 0;
        double totalTime = 0;
        int stepCount = 0;

        DataSnapshot stepsSnapshot = lastGameSnapshot.child("steps");

        if (stepsSnapshot.exists()) {
            for (DataSnapshot stepSnapshot : stepsSnapshot.getChildren()) {
                Boolean resultValue = stepSnapshot.child("result").getValue(Boolean.class);
                Double timeValue = stepSnapshot.child("time").getValue(Double.class);

                if (resultValue != null && resultValue) {
                    trueCount++;
                }

                if (timeValue != null) {
                    totalTime += timeValue;
                    stepCount++;
                }

                Log.d("MetricsActivity", "Result: " + resultValue + ", Time: " + timeValue);
            }
        }

        trueCountTextView.setText(String.valueOf(trueCount));

        if (stepCount > 0) {
            double averageTime = totalTime / stepCount;
            averageTimeTextView.setText(String.format("%.2f", averageTime) + " s");
        } else {
            averageTimeTextView.setText("0");
        }
    }

    private String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
}
