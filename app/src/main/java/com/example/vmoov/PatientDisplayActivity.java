package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.*;
import androidx.cardview.widget.CardView;

public class PatientDisplayActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MetricsAdapter metricsAdapter;
    private List<Metric> metricsList;
    private List<Metric> filteredMetricsList;
    private TextView patientNameTextView;
    private Spinner spinnerMonth, spinnerOrder;
    private CardView backCard;
    private static final String TAG = "PatientDisplayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_display);

        patientNameTextView = findViewById(R.id.user_name);
        recyclerView = findViewById(R.id.recyclerViewMetrics);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backCard = findViewById(R.id.back_card);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerOrder = findViewById(R.id.spinner_order);

        metricsList = new ArrayList<>();
        filteredMetricsList = new ArrayList<>();
        metricsAdapter = new MetricsAdapter(filteredMetricsList);
        recyclerView.setAdapter(metricsAdapter);

        backCard.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDisplayActivity.this, NotPatientActivity.class);
            startActivity(intent);
        });

        String patientId = getIntent().getStringExtra("userId");
        Log.d(TAG, "Received patientId: " + patientId);

        if (patientId != null) {
            fetchPatientName(patientId);
            fetchPatientGames(patientId);
        } else {
            Log.e(TAG, "No patientId provided in Intent.");
        }

        setupSpinners();
    }

    private void setupSpinners() {
        // Configurar Spinner de Mes
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSorting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configurar Spinner de Orden
        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(this,
                R.array.order_array, android.R.layout.simple_spinner_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrder.setAdapter(orderAdapter);

        spinnerOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSorting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchPatientName(String patientId) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference().child("users").child(patientId);
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        patientNameTextView.setText(firstName + " " + lastName);
                    } else {
                        patientNameTextView.setText("Nombre no disponible");
                    }
                } else {
                    patientNameTextView.setText("No se encontró información del paciente");
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
                    metricsList.clear();
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot uniqueGameSnapshot : gameSnapshot.getChildren()) {
                            Long startTime = uniqueGameSnapshot.child("startTime").getValue(Long.class);
                            Long endTime = uniqueGameSnapshot.child("endTime").getValue(Long.class);

                            if (startTime == null || endTime == null) {
                                Log.e(TAG, "startTime o endTime es null, ignorando juego.");
                                continue;
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

                            String gameDurationFormatted = "Duración no disponible";
                            if (startTime != null && endTime != null) {
                                gameDurationFormatted = GameDurationCalculator.calculateGameDuration(startTime, endTime);
                            }

                            double averageTime = (stepCount > 0) ? totalTime / stepCount : 0;

                            Metric metric = new Metric(startTime, endTime, trueCount, averageTime, gameDurationFormatted, stepCount);
                            metricsList.add(metric);
                            Log.d(TAG, "Añadida métrica: " + metric.getGameDuration());
                        }
                    }

                    Log.d(TAG, "Total métricas cargadas: " + metricsList.size());
                    applyFiltersAndSorting();
                } else {
                    Log.e(TAG, "No gameplaydata found para el paciente: " + patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al obtener los juegos: " + databaseError.getMessage());
            }
        });
    }


    private void applyFiltersAndSorting() {
        String selectedMonth = spinnerMonth.getSelectedItem().toString().toLowerCase();
        String selectedOrder = spinnerOrder.getSelectedItem().toString();

        Log.d(TAG, "Filtrando por mes: " + selectedMonth + " | Orden: " + selectedOrder);

        filteredMetricsList.clear();

        for (Metric metric : metricsList) {
            String metricMonth = getMonthFromTimestamp(metric.getStartTime()).toLowerCase();
            if (selectedMonth.equals("todos") || metricMonth.equals(selectedMonth)) {
                filteredMetricsList.add(metric);
            }
        }

        Log.d(TAG, "Total métricas después del filtro: " + filteredMetricsList.size());

        // 📌 Mostrar lista antes de ordenar
        Log.d(TAG, "Métricas ANTES de ordenar:");
        for (Metric metric : filteredMetricsList) {
            Log.d(TAG, "StartTime: " + metric.getStartTime() + " -> " + convertTimestampToDate(metric.getStartTime()));
        }

        // 📌 Aplicar el orden seleccionado
        switch (selectedOrder) {
            case "Fecha (más antiguo a más nuevo)":
                filteredMetricsList.sort(Comparator.comparingLong(Metric::getStartTime));
                break;
            case "Fecha (más nuevo a más antiguo)":
                filteredMetricsList.sort((m1, m2) -> Long.compare(m2.getStartTime(), m1.getStartTime()));
                break;
            case "Tiempo de ejecución":
                filteredMetricsList.sort(Comparator.comparingDouble(Metric::getAverageTime));
                break;
            case "Duración de sesión":
                filteredMetricsList.sort(Comparator.comparing(Metric::getGameDuration)); // Asegúrate que Metric tenga este método
                break;
            case "Cantidad de movimientos correctos":
                filteredMetricsList.sort((m1, m2) -> Integer.compare(m2.getTrueCount(), m1.getTrueCount()));
                break;
        }

        // 📌 Mostrar lista después de ordenar
        Log.d(TAG, "Métricas DESPUÉS de ordenar:");
        for (Metric metric : filteredMetricsList) {
            Log.d(TAG, "StartTime: " + metric.getStartTime() + " -> " + convertTimestampToDate(metric.getStartTime()));
        }

        Log.d(TAG, "Orden aplicado. Total métricas después de ordenar: " + filteredMetricsList.size());

        metricsAdapter.notifyDataSetChanged();
    }




    private int getGameDurationInSeconds(Metric metric) {
        String duration = metric.getGameDuration(); // Ejemplo: "00:05:30"
        String[] parts = duration.split(":");

        if (parts.length == 3) {
            try {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                return (hours * 3600) + (minutes * 60) + seconds;
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing game duration: " + duration, e);
            }
        }
        return 0; // Si hay error, devuelve 0
    }


    private String getMonthFromTimestamp(long timestamp) {
        if (timestamp <= 0) return "desconocido";

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
        sdf.setTimeZone(TimeZone.getDefault()); // Asegura que usa la zona horaria correcta
        return sdf.format(new Date(timestamp));
    }

    private String convertTimestampToDate(long timestamp) {
        if (timestamp <= 0) return "Fecha no disponible"; // Manejo de error para timestamps inválidos

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

}
