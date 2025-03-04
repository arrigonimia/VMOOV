package com.example.vmoov;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends BaseActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewContacts2;
    private ContactAdapter contactAdapter;
    private CardView sendReportButton;
    private CardView backButton;
    private String userId;
    private double averageExecutionTime = 0; // Tiempo promedio de ejecución
    private double prescribedExecutionTime = 0; // Tiempo de ejecución prescripto

    private static final String TAG = "ReportActivity";

    private String professionalEmail;
    private String patientName = "Desconocido";
    private String patientLastName = "Desconocido";
    private int prescribedSessions = 0; // 🔹 Cantidad de sesiones recetadas
    private int totalSessions = 0;
    private int prescribedSteps = 0;
    private String fechaUltimaSesion = "Fecha desconocida"; // 🔹 Variable para la fecha de la última sesión
    private int successfulSteps = 0;
    private int generalScore = 0;
    private double executionTimeChange = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("contacts").child(userId);

        obtenerEmailProfesional();
        obtenerDatosPaciente();
        obtenerPrescripcionPaciente();
        obtenerMetricasJuego();

        sendReportButton = findViewById(R.id.btn_send_report);
        sendReportButton.setOnClickListener(view -> generarYEnviarReporte());

        CardView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportActivity.this, MenuActivity.class);
            startActivity(intent);
        });
    }

    private void obtenerEmailProfesional() {
        DatabaseReference professionalsRef = FirebaseDatabase.getInstance().getReference("healthProfessionals");

        professionalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot professionalSnapshot : snapshot.getChildren()) {
                    DataSnapshot patientsSnapshot = professionalSnapshot.child("patients");
                    if (patientsSnapshot.hasChild(userId)) {
                        String professionalId = professionalSnapshot.getKey();
                        obtenerEmailDesdeUsers(professionalId);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al buscar ID del profesional en healthProfessionals: " + error.getMessage());
            }
        });
    }

    private void obtenerEmailDesdeUsers(String professionalId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(professionalId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    professionalEmail = snapshot.child("email").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al obtener email en users: " + error.getMessage());
            }
        });
    }

    private void obtenerDatosPaciente() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    patientName = snapshot.child("firstName").getValue(String.class);
                    patientLastName = snapshot.child("lastName").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al obtener datos del paciente: " + error.getMessage());
            }
        });
    }

    private void obtenerPrescripcionPaciente() {
        DatabaseReference prescriptionRef = FirebaseDatabase.getInstance().getReference("healthProfessionals");

        prescriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot professionalSnapshot : snapshot.getChildren()) {
                    DataSnapshot patientsSnapshot = professionalSnapshot.child("patients");
                    if (patientsSnapshot.hasChild(userId)) {
                        prescribedSessions = patientsSnapshot.child(userId).child("prescription").child("sessions").getValue(Integer.class);
                        prescribedSteps = patientsSnapshot.child(userId).child("prescription").child("steps").getValue(Integer.class);
                        prescribedExecutionTime = patientsSnapshot.child(userId).child("prescription").child("duration").getValue(Double.class); // 🔹 Obtener el tiempo prescripto
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al obtener prescripción del paciente: " + error.getMessage());
            }
        });
    }



    private void obtenerMetricasJuego() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("patientmetrics").child(userId).child("gameplaydata");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int gamesPlayed = 0;
                int successfulMovements = 0;
                double totalExecutionTime = 0;
                int totalSteps = 0; // 🔹 Contador de steps en la última sesión
                long latestTimestamp = 0;
                DataSnapshot latestGameSnapshot = null;

                // 🔍 Buscar la última sesión
                for (DataSnapshot gameNode : snapshot.getChildren()) {
                    for (DataSnapshot gameIdSnapshot : gameNode.getChildren()) {
                        Long startTime = gameIdSnapshot.child("startTime").getValue(Long.class);

                        if (startTime != null) {
                            gamesPlayed++;
                            if (startTime > latestTimestamp) {
                                latestTimestamp = startTime;
                                latestGameSnapshot = gameIdSnapshot;
                            }
                        }
                    }
                }

                // 📅 Obtener la fecha de la última partida en formato legible
                if (latestTimestamp > 0) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    fechaUltimaSesion = dateFormat.format(new Date(latestTimestamp));
                } else {
                    fechaUltimaSesion = "Fecha desconocida";
                }

                // 🏆 Procesar la última sesión encontrada
                if (latestGameSnapshot != null) {
                    for (DataSnapshot step : latestGameSnapshot.child("steps").getChildren()) {
                        Boolean result = step.child("result").getValue(Boolean.class);
                        Double stepTime = step.child("time").getValue(Double.class); // 🔹 Obtener tiempo de cada step

                        if (result != null && result) {
                            successfulMovements++;
                        }

                        if (stepTime != null) {
                            totalExecutionTime += stepTime;
                            totalSteps++; // 🔹 Contar el número de steps
                        }
                    }
                }

                successfulSteps = successfulMovements;
                totalSessions = gamesPlayed; // 🔹 Se actualiza la cantidad de sesiones completadas

                // ⏳ Calcular el tiempo promedio de ejecución SOLO si hay steps
                if (totalSteps > 0) {
                    averageExecutionTime = totalExecutionTime / totalSteps;
                } else {
                    averageExecutionTime = 0; // 🔹 Evitar división por 0
                }

                calcularPuntajeGeneral(successfulSteps);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al obtener métricas del paciente: " + error.getMessage());
            }
        });
    }



    private void calcularPuntajeGeneral(int successfulMovements) {
        double precisionMovimientos = (prescribedSteps > 0) ? Math.min(1.0, (double) successfulMovements / prescribedSteps) : 0;
        double tiempoFactor = 1.0;
        double puntajeGeneral = (0.7 * precisionMovimientos) + (0.3 * tiempoFactor);
        generalScore = (int) (puntajeGeneral * 100);
    }

    private void generarYEnviarReporte() {
        String[] reportLines = {
                "Reporte de Sesión",
                "Fecha de la sesión: " + fechaUltimaSesion,
                "Paciente: " + patientName + " " + patientLastName,
                "Sesiones completadas: " + totalSessions,
                "Sesiones recetadas: " + prescribedSessions,
                "Movimientos exitosos: " + successfulSteps,
                "Movimientos prescriptos: " + prescribedSteps,
                "Tiempo promedio de ejecución: " + String.format(Locale.US, "%.2f segundos", averageExecutionTime),
                "Tiempo prescripto: " + String.format(Locale.US, "%.2f segundos", prescribedExecutionTime),
                "Puntaje general: " + generalScore + "/100"
        };

        File pdfFile = PdfGenerator.createPdf(this, "VMOOV_Reporte_" + patientName + patientLastName + ".pdf", reportLines);

        if (pdfFile != null) {
            enviarCorreoConAdjunto(professionalEmail, pdfFile.getAbsolutePath());
        }
    }





    private void enviarCorreoConAdjunto(String email, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "❌ El archivo no existe: " + filePath);
            return;
        }

        Context context = this;
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Sesión - VMOOV");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Adjunto el reporte de sesión en PDF.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Enviar reporte"));
            Log.d(TAG, "📤 Enviando correo con archivo adjunto: " + filePath);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al enviar el correo: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String mensaje) {
        runOnUiThread(() -> {
            Toast.makeText(ReportActivity.this, mensaje, Toast.LENGTH_LONG).show();
        });
    }
}
