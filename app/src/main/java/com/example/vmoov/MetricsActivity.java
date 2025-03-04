package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class MetricsActivity extends BaseActivity {

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
        setContentView(R.layout.pruebas);

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
        DatabaseReference gameDataRef = mDatabase.child("patientmetrics").child(userId).child("gameplaydata").child("game1");

        gameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<DataSnapshot> gameList = new ArrayList<>();
                    for (DataSnapshot gameDetailSnapshot : dataSnapshot.getChildren()) {
                        gameList.add(gameDetailSnapshot);
                    }

                    // Ordenar la lista de juegos por startTime en orden ascendente (cronológico)
                    gameList.sort((game1, game2) -> {
                        Long startTime1 = game1.child("startTime").getValue(Long.class);
                        Long startTime2 = game2.child("startTime").getValue(Long.class);
                        if (startTime1 == null) startTime1 = 0L;
                        if (startTime2 == null) startTime2 = 0L;
                        return startTime1.compareTo(startTime2);
                    });

                    long latestStartTime = 0;
                    DataSnapshot latestGameSnapshot = null;
                    List<BarEntry> barEntries = new ArrayList<>();
                    List<BarEntry> barEntriesTrueCount = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int gameIndex = 0;
                    float maxValue = 0f;

                    for (DataSnapshot gameDetailSnapshot : gameList) {
                        double totalTime = 0;
                        int stepCount = 0;
                        int trueCount = 0;

                        // Obtener startTime
                        Long startTime = gameDetailSnapshot.child("startTime").getValue(Long.class);
                        if (startTime != null && startTime > latestStartTime) {
                            latestStartTime = startTime;
                            latestGameSnapshot = gameDetailSnapshot;
                        }

                        // Procesar los pasos para el gráfico
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

                        if (stepCount > 0) {
                            double averageTime = totalTime / stepCount;
                            barEntries.add(new BarEntry(gameIndex, (float) averageTime));
                            barEntriesTrueCount.add(new BarEntry(gameIndex, trueCount));
                            maxValue = Math.max(maxValue, (float) averageTime);
                            labels.add(convertTimestampToDate(startTime));
                            gameIndex++;
                        }
                    }

                    // Actualizar la fecha de la última sesión
                    if (latestStartTime != 0) {
                        lastSessionTextView.setText(convertTimestampToDate(latestStartTime));
                    } else {
                        lastSessionTextView.setText("No disponible");
                    }

                    // Calcular métricas del juego más reciente
                    if (latestGameSnapshot != null) {
                        calculateGameStats(latestGameSnapshot);
                    }

                    // Configurar gráficos
                    configureBarChart(barChart, barEntries, labels, maxValue);
                    configureBarChart(barChart2, barEntriesTrueCount, labels, maxValue);
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

    private void configureBarChart(BarChart chart, List<BarEntry> entries, List<String> labels, float maxValue) {
        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColors(new int[]{0xFFA36BFA, 0xFF5C4CF1});
        dataSet.setValueTextSize(14f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value);
            }
        });
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getAxisLeft().setAxisMaximum(maxValue * 1.1f);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getDescription().setEnabled(false);
        chart.animateY(1500);
        chart.invalidate();
    }

    private void calculateGameStats(DataSnapshot latestGameSnapshot) {
        int trueCount = 0;
        double totalTime = 0;
        int stepCount = 0;

        DataSnapshot stepsSnapshot = latestGameSnapshot.child("steps");

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(date);
    }
}
