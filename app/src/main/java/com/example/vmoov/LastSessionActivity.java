package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class LastSessionActivity extends BaseActivity {
    private TextView titleText, subtitleText, motivationalText, chartTitle, chartDescription;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ChartPagerAdapter chartPagerAdapter;

    private final String TAG = "LastSessionActivity";

    private final String[] chartTitles = {
            "Puntaje General",
            "Tiempo Promedio por Paso",
            "Movimientos Exitosos",
            "Sesiones Completadas",
    };

    private final String[] chartDescriptions = {
            "Evaluación general del desempeño del paciente basada en múltiples métricas.",
            "Tiempo promedio en segundos de ejecución, comparado con la anteúltima sesión.",
            "Promedio de movimientos exitosos comparado con la prescripción.",
            "Cantidad de sesiones realizadas respecto del total recetado.",

    };

    private int prescribedSteps = 0;
    private int totalSessions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_session);

        Log.d(TAG, "Iniciando LastSessionActivity...");

        // Botón de regreso
        ImageButton homeButton = findViewById(R.id.back_button);
        homeButton.setOnClickListener(v -> startActivity(new Intent(LastSessionActivity.this, MenuActivity.class)));

        // Referencias UI
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
        motivationalText = findViewById(R.id.motivational_text);
        chartTitle = findViewById(R.id.chart_title);
        chartDescription = findViewById(R.id.chart_description);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tab_layout);
        chartPagerAdapter = new ChartPagerAdapter(this);
        viewPager.setAdapter(chartPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("#1");  // Nuevo nombre
                    break;
                case 1:
                    tab.setText("#2");  // Nuevo nombre
                    break;
                case 2:
                    tab.setText("#3");       // Nuevo nombre
                    break;
                case 3:
                    tab.setText("#4");    // Nuevo nombre
                    break;
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                chartTitle.setText(chartTitles[position]);
                chartDescription.setText(chartDescriptions[position]);
            }
        });

        motivationalText.setText(getRandomMotivationalPhrase());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado.");
            titleText.setText("Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Usuario autenticado: " + userId);

        // Obtener la cantidad de sesiones y pasos recetados ANTES de actualizar los gráficos
        obtenerPrescripciones(userId, () -> obtenerMetricasJuego(userId));
    }

    private void obtenerPrescripciones(String userId, Runnable callback) {
        DatabaseReference prescriptionRef = FirebaseDatabase.getInstance()
                .getReference("healthProfessionals");

        prescriptionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot professionalSnapshot : task.getResult().getChildren()) {
                    DataSnapshot patientsSnapshot = professionalSnapshot.child("patients");
                    if (patientsSnapshot.hasChild(userId)) {
                        totalSessions = patientsSnapshot.child(userId).child("prescription").child("sessions").getValue(Integer.class);
                        prescribedSteps = patientsSnapshot.child(userId).child("prescription").child("steps").getValue(Integer.class);
                        break;
                    }
                }

                Log.d(TAG, "🔍 Sesiones prescriptas: " + totalSessions);
                Log.d(TAG, "🔍 Pasos prescriptos: " + prescribedSteps);

                // Ejecutar la siguiente función después de obtener los datos
                callback.run();
            }
        });
    }

    private String fechaUltimaSesion = "Fecha desconocida"; // 🔹 Variable para almacenar la fecha en formato legible

    private void obtenerMetricasJuego(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("patientmetrics").child(userId).child("gameplaydata");

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot dataSnapshot = task.getResult();

                int gamesPlayed = 0;
                int movimientosExitosos = 0;
                double lastGameAvgExecutionTime = 0;
                double previousGameAvgExecutionTime = -1;
                long latestTimestamp = 0;
                long previousTimestamp = 0;
                DataSnapshot latestGameSnapshot = null;
                DataSnapshot previousGameSnapshot = null;

                // 🔹 Recorrer todas las partidas para detectar la última y la anteúltima
                for (DataSnapshot gameNode : dataSnapshot.getChildren()) {
                    for (DataSnapshot gameIdSnapshot : gameNode.getChildren()) {
                        Long startTime = gameIdSnapshot.child("startTime").getValue(Long.class);
                        if (startTime != null) {
                            gamesPlayed++;

                            if (startTime > latestTimestamp) {
                                previousTimestamp = latestTimestamp;
                                previousGameSnapshot = latestGameSnapshot;
                                latestTimestamp = startTime;
                                latestGameSnapshot = gameIdSnapshot;
                            } else if (startTime > previousTimestamp) {
                                previousTimestamp = startTime;
                                previousGameSnapshot = gameIdSnapshot;
                            }
                        }
                    }
                }

                // 📅 **Convertir `latestTimestamp` a una fecha legible**
                if (latestTimestamp > 0) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    fechaUltimaSesion = dateFormat.format(new Date(latestTimestamp));
                } else {
                    fechaUltimaSesion = "Fecha desconocida";
                }

                Log.d(TAG, "✅ Última sesión registrada el: " + fechaUltimaSesion);

                // 🔹 Actualizar `subtitleText` en la UI
                runOnUiThread(() -> subtitleText.setText(fechaUltimaSesion));

                // 🔹 Obtener el tiempo promedio de ejecución de la anteúltima partida
                if (previousGameSnapshot != null) {
                    double totalTime = 0;
                    int stepsCompleted = 0;

                    for (DataSnapshot step : previousGameSnapshot.child("steps").getChildren()) {
                        Double time = step.child("time").getValue(Double.class);
                        if (time != null) {
                            totalTime += time;
                            stepsCompleted++;
                        }
                    }

                    if (stepsCompleted > 0) {
                        previousGameAvgExecutionTime = totalTime / stepsCompleted;
                    }
                }

                Log.d(TAG, "🔍 Tiempo Promedio de la Anteúltima Sesión: " + previousGameAvgExecutionTime);

                // 🔹 Obtener el tiempo promedio de ejecución de la última partida
                if (latestGameSnapshot != null) {
                    movimientosExitosos = 0;
                    double totalTime = 0;
                    int stepsCompleted = 0;

                    for (DataSnapshot step : latestGameSnapshot.child("steps").getChildren()) {
                        Boolean result = step.child("result").getValue(Boolean.class);
                        Double time = step.child("time").getValue(Double.class);

                        if (result != null && time != null) {
                            if (result) {
                                movimientosExitosos++;
                            }
                            totalTime += time;
                            stepsCompleted++;
                        }
                    }

                    if (stepsCompleted > 0) {
                        lastGameAvgExecutionTime = totalTime / stepsCompleted;
                    }
                }

                Log.d(TAG, "✅ Sesiones completadas: " + gamesPlayed);
                Log.d(TAG, "✅ Movimientos exitosos en la última partida: " + movimientosExitosos);
                Log.d(TAG, "✅ Tiempo Promedio de la Última Sesión: " + lastGameAvgExecutionTime);

                double executionTimeChange = 0;
                if (previousGameAvgExecutionTime > 0) {
                    executionTimeChange = ((lastGameAvgExecutionTime - previousGameAvgExecutionTime) / previousGameAvgExecutionTime) * 100;
                }

                Log.d(TAG, "📊 Cambio en tiempo de ejecución: " + executionTimeChange + "%");

                // **Actualizar el adaptador con TODOS los datos**
                chartPagerAdapter.setExecutionTimeChange((int) executionTimeChange);
                chartPagerAdapter.setGamesPlayed(gamesPlayed);
                chartPagerAdapter.setSuccessfulSteps(movimientosExitosos);
                chartPagerAdapter.setTotalSessions(totalSessions);
                chartPagerAdapter.setPrescribedSteps(prescribedSteps);

                // **Forzar actualización del adaptador**
                viewPager.getAdapter().notifyDataSetChanged();

                calcularPuntajeGeneral(userId, lastGameAvgExecutionTime, movimientosExitosos);
            }
        });
    }


    /**
     * 🔹 Método público para obtener la fecha de la última sesión
     */
    public String getFechaUltimaSesion() {
        return fechaUltimaSesion;
    }



    private void calcularPuntajeGeneral(String userId, double lastGameAvgExecutionTime, int movimientosExitosos) {
        DatabaseReference prescriptionRef = FirebaseDatabase.getInstance()
                .getReference("healthProfessionals");

        prescriptionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int prescribedSteps = 0;
                double tiempoIdeal = 0;

                for (DataSnapshot professionalSnapshot : task.getResult().getChildren()) {
                    DataSnapshot patientsSnapshot = professionalSnapshot.child("patients");
                    if (patientsSnapshot.hasChild(userId)) {
                        prescribedSteps = patientsSnapshot.child(userId).child("prescription").child("steps").getValue(Integer.class);
                        tiempoIdeal = patientsSnapshot.child(userId).child("prescription").child("duration").getValue(Double.class);
                        break;
                    }
                }

                Log.d(TAG, "🔍 Datos para cálculo de puntaje general:");
                Log.d(TAG, "    Pasos prescriptos: " + prescribedSteps);
                Log.d(TAG, "    Movimientos exitosos en la última sesión: " + movimientosExitosos);
                Log.d(TAG, "    Tiempo ideal por paso: " + tiempoIdeal);
                Log.d(TAG, "    Tiempo promedio última sesión: " + lastGameAvgExecutionTime);

                // 🔹 Cálculo corregido de precisión de movimientos
                double precisionMovimientos = (prescribedSteps > 0) ? Math.min(1.0, (double) movimientosExitosos / prescribedSteps) : 0;
                double tiempoFactor = Math.min(1.0, tiempoIdeal / lastGameAvgExecutionTime);
                double puntajeGeneral = (0.7 * precisionMovimientos) + (0.3 * tiempoFactor);

                int puntajeFinal = (int) (puntajeGeneral * 100);
                Log.d(TAG, "✅ Puntaje General Calculado: " + puntajeFinal);

                chartPagerAdapter.setGeneralScore(puntajeFinal);
            }
        });
    }




    private String getRandomMotivationalPhrase() {
    String[] phrases = {
            "Cada paso cuenta. ¡Sigue adelante!",
            "Pequeños logros llevan a grandes cambios.",
            "La constancia es la clave del éxito.",
            "Hoy es un buen día para mejorar.",
            "El esfuerzo de hoy es el éxito de mañana.",
            "Sigue avanzando, cada sesión suma.",
            "No te rindas, ¡estás progresando!",
            "Cree en ti. ¡Puedes lograrlo!",
            "Cada intento te acerca más a tu meta.",
            "Tu determinación te hace más fuerte."
    };
    return phrases[new Random().nextInt(phrases.length)];
}}

