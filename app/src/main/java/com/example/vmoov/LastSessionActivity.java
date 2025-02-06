package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class LastSessionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_session);

        Button signUpButton = findViewById(R.id.button_prim);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LastSessionActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        // References to layout components
        TextView titleText = findViewById(R.id.title_text);
        TextView subtitleText = findViewById(R.id.subtitle_text);
        TextView motivationalText = findViewById(R.id.motivational_text);
        TextView mainGaugeText = findViewById(R.id.main_gauge_text);

        PieChart mainGauge = findViewById(R.id.main_gauge);
        PieChart indicator1 = findViewById(R.id.indicator1);
        PieChart indicator2 = findViewById(R.id.indicator2);
        PieChart indicator3 = findViewById(R.id.indicator3);

        TextView indicator1Text = findViewById(R.id.indicator1_text);
        TextView indicator2Text = findViewById(R.id.indicator2_text);
        TextView indicator3Text = findViewById(R.id.indicator3_text);

        // Get logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            titleText.setText("Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("patientmetrics").child(userId).child("gameplaydata");

        // Fetch data from Firebase
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot dataSnapshot = task.getResult();

                String latestGameName = "Game1";
                String latestGameDate = "01/01/2025";
                int gamesPlayed = 0;
                long latestTimestamp = 0;
                DataSnapshot latestGameSnapshot = null;

                // Iterate over games and find the latest
                for (DataSnapshot gameNode : dataSnapshot.getChildren()) {
                    for (DataSnapshot gameIdSnapshot : gameNode.getChildren()) {
                        Long startTime = gameIdSnapshot.child("startTime").getValue(Long.class);
                        if (startTime != null) {
                            gamesPlayed++;
                            if (startTime > latestTimestamp) {
                                latestTimestamp = startTime;
                                latestGameSnapshot = gameIdSnapshot;
                                latestGameName = gameNode.getKey(); // Get the game name (e.g., "Game1")
                            }
                        }
                    }
                }

                if (latestGameSnapshot != null) {
                    latestGameDate = convertTimestampToDate(latestTimestamp);

                    // Process data for the latest game
                    DataSnapshot stepsSnapshot = latestGameSnapshot.child("steps");
                    int stepsCompleted = 0;
                    int successfulSteps = 0;
                    double totalTime = 0;

                    for (DataSnapshot step : stepsSnapshot.getChildren()) {
                        Boolean result = step.child("result").getValue(Boolean.class);
                        Double time = step.child("time").getValue(Double.class);

                        if (result != null && result) successfulSteps++;
                        if (time != null) totalTime += time;
                        stepsCompleted++;
                    }

                    long sessionDuration = latestGameSnapshot.child("endTime").getValue(Long.class) -
                            latestGameSnapshot.child("startTime").getValue(Long.class);
                    double avgExecutionTime = stepsCompleted > 0 ? totalTime / stepsCompleted : 0;

                    // Update UI
                    titleText.setText("Última Partida");
                    subtitleText.setText(latestGameName + " - " + latestGameDate);
                    mainGaugeText.setText(gamesPlayed + " de 10\nsesiones realizadas");

                    setupPieChart(mainGauge, gamesPlayed, 10, "#5C4CF1", "#EFEFEF");
                    setupPieChart(indicator1, (int) (sessionDuration / (1000 * 60)), 60, "#4CAF50", "#EFEFEF");
                    setupPieChart(indicator2, stepsCompleted, 10, "#FFC107", "#EFEFEF");
                    setupPieChart(indicator3, (int) avgExecutionTime, 10, "#2196F3", "#EFEFEF");

                    indicator1Text.setText((sessionDuration / (1000 * 60)) + " mins");
                    indicator2Text.setText(successfulSteps + " movimientos buenos");
                    indicator3Text.setText(String.format("%.2f segs", avgExecutionTime));
                } else {
                    titleText.setText("No se encontraron partidas");
                    subtitleText.setText("");
                }
            } else {
                titleText.setText("Error al cargar datos");
            }
        });

        // Set a random motivational phrase
        String[] motivationalPhrases = {
                "¡Seguí así, estás mejorando cada día!",
                "Cada sesión cuenta, ¡no te rindas!",
                "¡Gran trabajo! El progreso es constante.",
                "La práctica te hace más fuerte, ¡seguí así!",
                "¡Vas increíble! Mantené este ritmo."
        };
        int randomIndex = (int) (Math.random() * motivationalPhrases.length);
        motivationalText.setText(motivationalPhrases[randomIndex]);
    }

    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(timestamp));
    }

    private void setupPieChart(PieChart chart, int value, int max, String primaryColor, String secondaryColor) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(value, ""));
        entries.add(new PieEntry(max - value, ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(android.graphics.Color.parseColor(primaryColor), android.graphics.Color.parseColor(secondaryColor));
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);

        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(80f);
        chart.setTransparentCircleColor(android.graphics.Color.parseColor("#EFEFEF"));
        chart.setDrawEntryLabels(false);
        chart.setDrawCenterText(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.invalidate();
    }

}
