package com.example.vmoov;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SummaryActivity extends AppCompatActivity {

    private BarChart barChart1, barChart2;
    private Spinner spinnerNumber, spinnerType;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Button signUpButton = findViewById(R.id.button_prim);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        barChart1 = findViewById(R.id.barChart1);
        barChart2 = findViewById(R.id.barChart2);
        spinnerNumber = findViewById(R.id.spinner_number);
        spinnerType = findViewById(R.id.spinner_type);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance()
                    .getReference("patientmetrics")
                    .child(currentUser.getUid())
                    .child("gameplaydata")
                    .child("game1");
        }

        ArrayAdapter<CharSequence> numberAdapter = ArrayAdapter.createFromResource(
                this, R.array.number_array, android.R.layout.simple_spinner_item);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumber.setAdapter(numberAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerNumber.setOnItemSelectedListener(listener);
        spinnerType.setOnItemSelectedListener(listener);
    }

    private void updateCharts() {
        if (mDatabase == null) return;

        int numberOfBars = Integer.parseInt(spinnerNumber.getSelectedItem().toString());
        String type = spinnerType.getSelectedItem().toString();

        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DataSnapshot> games = new ArrayList<>();

                for (DataSnapshot gameSnapshot : task.getResult().getChildren()) {
                    games.add(gameSnapshot);
                }

                games.sort((g1, g2) -> {
                    Long t1 = g1.child("startTime").getValue(Long.class);
                    Long t2 = g2.child("startTime").getValue(Long.class);
                    return t1 != null && t2 != null ? t1.compareTo(t2) : 0;
                });

                if (type.equals("Sesiones")) {
                    updateChartsForSessions(games, numberOfBars);
                } else if (type.equals("Semanas")) {
                    updateChartsForWeeks(games, numberOfBars);
                } else if (type.equals("Meses")) {
                    updateChartsForMonths(games, numberOfBars);
                }
            }
        });
    }

    private void updateChartsForSessions(List<DataSnapshot> games, int numberOfBars) {
        List<BarEntry> barEntries1 = new ArrayList<>();
        List<BarEntry> barEntries2 = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int totalGames = games.size();
        int startIndex = Math.max(0, totalGames - numberOfBars);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = startIndex; i < totalGames; i++) {
            DataSnapshot gameSnapshot = games.get(i);

            int trueCount = getTrueCount(gameSnapshot);
            double averageTime = getDurationTime(gameSnapshot);

            Long startTime = gameSnapshot.child("startTime").getValue(Long.class);
            String label = startTime != null ? dateFormatter.format(new Date(startTime)) : "Sin fecha";

            barEntries1.add(new BarEntry(i - startIndex, (float) averageTime));
            barEntries2.add(new BarEntry(i - startIndex, trueCount));
            labels.add(label);
        }

        configureBarChart(barChart1, barEntries1, labels, "Duración promedio de pasos (segundos)");
        configureBarChart(barChart2, barEntries2, labels, "Pasos exitosos");
    }

    private void updateChartsForWeeks(List<DataSnapshot> games, int numberOfBars) {
        SimpleDateFormat weekFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Map<Long, List<DataSnapshot>> weeklyData = new TreeMap<>(); // Clave: timestamp del lunes de cada semana

        Calendar calendar = Calendar.getInstance();

        // Agrupar las sesiones por semanas
        for (DataSnapshot game : games) {
            Long startTime = game.child("startTime").getValue(Long.class);
            if (startTime != null) {
                calendar.setTimeInMillis(startTime);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Ajustar al lunes de la semana
                long weekStart = calendar.getTimeInMillis();

                weeklyData.putIfAbsent(weekStart, new ArrayList<>());
                weeklyData.get(weekStart).add(game);
            }
        }

        // Ordenar las semanas y seleccionar las más recientes
        List<Map.Entry<Long, List<DataSnapshot>>> orderedWeeks = new ArrayList<>(weeklyData.entrySet());
        orderedWeeks = orderedWeeks.subList(Math.max(0, orderedWeeks.size() - numberOfBars), orderedWeeks.size());

        List<BarEntry> barEntries1 = new ArrayList<>();
        List<BarEntry> barEntries2 = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<Long, List<DataSnapshot>> entry : orderedWeeks) {
            List<DataSnapshot> weekGames = entry.getValue();
            double totalDuration = 0;
            int totalTrueCount = 0;

            for (DataSnapshot game : weekGames) {
                totalTrueCount += getTrueCount(game);
                totalDuration += getDurationTime(game);
            }

            // Generar etiqueta de rango de fechas (lunes a domingo)
            calendar.setTimeInMillis(entry.getKey());
            String weekLabel = weekFormatter.format(calendar.getTime()) + " - ";
            calendar.add(Calendar.DATE, 6); // Ajustar al domingo de la semana
            weekLabel += weekFormatter.format(calendar.getTime());

            barEntries1.add(new BarEntry(index, (float) (totalDuration / weekGames.size())));
            barEntries2.add(new BarEntry(index, totalTrueCount));
            labels.add(weekLabel);
            index++;
        }

        configureBarChart(barChart1, barEntries1, labels, "Duración promedio semanal (segundos)");
        configureBarChart(barChart2, barEntries2, labels, "Pasos exitosos semanales");
    }





    private void updateChartsForMonths(List<DataSnapshot> games, int numberOfBars) {
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM yyyy");
        Map<Long, List<DataSnapshot>> monthlyData = new TreeMap<>((a, b) -> b.compareTo(a));

        Calendar calendar = Calendar.getInstance();

        for (DataSnapshot game : games) {
            Long startTime = game.child("startTime").getValue(Long.class);
            if (startTime != null) {
                calendar.setTimeInMillis(startTime);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                long monthStart = calendar.getTimeInMillis();

                monthlyData.putIfAbsent(monthStart, new ArrayList<>());
                monthlyData.get(monthStart).add(game);
            }
        }

        List<BarEntry> barEntries1 = new ArrayList<>();
        List<BarEntry> barEntries2 = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<Long, List<DataSnapshot>> entry : monthlyData.entrySet()) {
            if (index >= numberOfBars) break;

            List<DataSnapshot> monthGames = entry.getValue();
            double totalDuration = 0;
            int totalTrueCount = 0;

            for (DataSnapshot game : monthGames) {
                totalTrueCount += getTrueCount(game);
                totalDuration += getDurationTime(game);
            }

            calendar.setTimeInMillis(entry.getKey());
            String monthLabel = monthFormatter.format(calendar.getTime());

            barEntries1.add(new BarEntry(index, (float) (totalDuration / monthGames.size())));
            barEntries2.add(new BarEntry(index, totalTrueCount));
            labels.add(monthLabel);
            index++;
        }

        configureBarChart(barChart1, barEntries1, labels, "Duración promedio mensual (segundos)");
        configureBarChart(barChart2, barEntries2, labels, "Pasos exitosos mensuales");
    }

    private int getTrueCount(DataSnapshot gameSnapshot) {
        int count = 0;
        DataSnapshot stepsSnapshot = gameSnapshot.child("steps");

        if (stepsSnapshot.exists()) {
            for (DataSnapshot stepSnapshot : stepsSnapshot.getChildren()) {
                Boolean result = stepSnapshot.child("result").getValue(Boolean.class);
                if (result != null && result) count++;
            }
        }
        return count;
    }

    private double getDurationTime(DataSnapshot gameSnapshot) {
        double total = 0;
        int count = 0;

        DataSnapshot stepsSnapshot = gameSnapshot.child("steps");

        if (stepsSnapshot.exists()) {
            for (DataSnapshot stepSnapshot : stepsSnapshot.getChildren()) {
                Double time = stepSnapshot.child("time").getValue(Double.class);

                if (time != null) {
                    total += time;
                    count++;
                }
            }
        }

        return count > 0 ? total / count : 0;
    }

    private void configureBarChart(BarChart chart, List<BarEntry> entries, List<String> labels, String description) {
        BarDataSet dataSet = new BarDataSet(entries, description);
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
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getDescription().setEnabled(false);
        chart.animateY(1500);
        chart.invalidate();
    }
}
