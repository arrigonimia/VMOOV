package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PatientDisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MetricsAdapter metricsAdapter;
    private List<Metric> metricsList;
    private TextView patientNameTextView;
    private Button backButton;
    private static final String TAG = "PatientDisplayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_display);

        TimeZone defaultTimeZone = TimeZone.getDefault();
        Log.d("TimezoneCheck", "Default Timezone: " + defaultTimeZone.getID());


        patientNameTextView = findViewById(R.id.user_name);
        recyclerView = findViewById(R.id.recyclerViewMetrics);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDisplayActivity.this, NotPatientActivity.class);
            startActivity(intent);
            finish();
        });

        metricsList = new ArrayList<>();
        metricsAdapter = new MetricsAdapter(metricsList);
        recyclerView.setAdapter(metricsAdapter);

        String patientId = getIntent().getStringExtra("userId");
        Log.d(TAG, "Received patientId: " + patientId);

        if (patientId != null) {
            fetchPatientName(patientId);
            fetchPatientGames(patientId);
        } else {
            Log.e(TAG, "No patientId provided in Intent.");
        }
    }

    private void fetchPatientName(String patientId) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference().child("users").child(patientId);
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    Log.d(TAG, "Fetched patient name: " + firstName + " " + lastName);
                    if (firstName != null && lastName != null) {
                        patientNameTextView.setText(firstName + " " + lastName);
                    } else {
                        patientNameTextView.setText("Nombre no disponible");
                        Log.e(TAG, "firstName or lastName is null for patientId: " + patientId);
                    }
                } else {
                    patientNameTextView.setText("No se encontró información del paciente");
                    Log.e(TAG, "Patient data does not exist for patientId: " + patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching patient name: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPatientGames(String patientId) {
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference()
                .child("patientmetrics").child(patientId).child("gameplaydata");

        gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot uniqueGameSnapshot : gameSnapshot.getChildren()) {

                            Long startTime = uniqueGameSnapshot.child("startTime").getValue(Long.class);
                            Long endTime = uniqueGameSnapshot.child("endTime").getValue(Long.class);

                            // Log the raw timestamp values
                            Log.d(TAG, "Start Time (timestamp): " + startTime);
                            Log.d(TAG, "End Time (timestamp): " + endTime);

                            // Log formatted dates to verify the conversion
                            if (startTime != null) {
                                Log.d(TAG, "Formatted Start Time: " + convertTimestampToDate(startTime));
                            }
                            if (endTime != null) {
                                Log.d(TAG, "Formatted End Time: " + convertTimestampToDate(endTime));
                            }

                            int trueCount = 0;
                            int stepCount = 0;
                            double totalTime = 0;

                            DataSnapshot stepsSnapshot = uniqueGameSnapshot.child("steps");
                            if (stepsSnapshot.exists()) {
                                for (DataSnapshot stepSnapshot : stepsSnapshot.getChildren()) {
                                    Boolean result = stepSnapshot.child("result").getValue(Boolean.class);
                                    Double time = stepSnapshot.child("time").getValue(Double.class);

                                    if (result != null && result) trueCount++;
                                    if (time != null) {
                                        totalTime += time;
                                        stepCount++;
                                    }
                                }
                            }

                            // Calcular duración de la partida usando GameDurationCalculator
                            String gameDurationFormatted = "Duración no disponible";
                            if (startTime != null && endTime != null) {
                                gameDurationFormatted = GameDurationCalculator.calculateGameDuration(startTime, endTime);
                            }

                            double averageTime = (stepCount > 0) ? totalTime / stepCount : 0;

                            // Crear objeto Metric y agregarlo a la lista
                            metricsList.add(new Metric(
                                    startTime != null ? startTime : 0,
                                    endTime != null ? endTime : 0,
                                    trueCount,
                                    averageTime,
                                    gameDurationFormatted,
                                    stepCount
                            ));
                        }
                    }

                    // Ordenar por `startTime` de forma descendente
                    metricsList.sort((metric1, metric2) -> Long.compare(metric2.getStartTime(), metric1.getStartTime()));

                    metricsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "No gameplaydata found for patient: " + patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching games: " + databaseError.getMessage());
            }
        });
    }


    private String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp);
        // Use correct 24-hour format with uppercase HH
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }


}
