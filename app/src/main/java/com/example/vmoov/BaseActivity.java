package com.example.vmoov;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Arrays;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPreferences";
    private static final String FONT_SCALE_KEY = "fontScale";
    private static final String TAG = "BaseActivity";
    private static final String SESSION_KEY = "UserSessionActive";
    private boolean isListenerActive = false;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyFontScale();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "✅ Usuario autenticado en onStart(): " + userId);

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean wasLoggedIn = prefs.getBoolean(SESSION_KEY, false);

            if (!wasLoggedIn) {
                Log.d(TAG, "🚀 Primera vez iniciando sesión en esta sesión.");
                prefs.edit().putBoolean(SESSION_KEY, true).apply();
            }

            if (!isListenerActive) {
                setupFirebaseListener(userId);
            }
        } else {
            Log.d(TAG, "❌ No hay usuario autenticado. No se activa la escucha.");
            clearSession();
        }
    }

    private void applyFontScale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float fontScale = prefs.getFloat(FONT_SCALE_KEY, 1.0f);
        getResources().getConfiguration().fontScale = fontScale;
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
    }

    private void clearSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(SESSION_KEY).apply();
        isListenerActive = false;
        Log.d(TAG, "🛑 Sesión limpiada debido a usuario no autenticado.");
    }

    private DataSnapshot lastSnapshot = null; // Guarda el último estado de los datos
    private boolean firstLoad = true; // 🔹 Evita activar la alerta al cargar los datos por primera vez

    private void setupFirebaseListener(String userId) {
        if (isListenerActive) {
            Log.d(TAG, "🔵 Listener ya activo, no se vuelve a registrar.");
            return;
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("patientmetrics").child(userId).child("gameplaydata").child("game1"); // Solo escucha dentro de game1

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "⚠️ No hay datos en Firebase. No se muestra alerta.");
                    return;
                }

                // 🔹 Evitar mostrar alerta en la primera carga
                if (firstLoad) {
                    Log.d(TAG, "🆕 Carga inicial de partidas en Firebase. No se muestra alerta.");
                    lastSnapshot = snapshot;
                    firstLoad = false;
                    return;
                }

                // 🔍 Comparar solo si se ha agregado una nueva partida (nuevo gameId)
                if (lastSnapshot != null) {
                    boolean nuevaPartida = detectarNuevaPartida(lastSnapshot, snapshot);

                    if (!nuevaPartida) {
                        Log.d(TAG, "🔵 No hay nuevas partidas en game1. No se muestra alerta.");
                        return;
                    }
                } else {
                    Log.d(TAG, "🆕 No hay snapshot previo. Guardando estado inicial...");
                }

                // Guardar el nuevo estado de los datos SOLO si se agregó una nueva partida
                lastSnapshot = snapshot;

                // Mostrar alerta si estamos en una actividad permitida
                if (shouldShowAlert()) {
                    Log.d(TAG, "🔔 Nueva partida detectada en Firebase. Mostrando alerta.");
                    showAlert();
                } else {
                    Log.d(TAG, "🔕 Nueva partida detectada, pero estamos en una actividad no permitida.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al leer datos de Firebase: " + error.getMessage());
            }
        };

        databaseReference.addValueEventListener(valueEventListener);
        isListenerActive = true;
    }

    /**
     * Verifica si hay un nuevo `gameId` en `game1`.
     * Retorna `true` si hay una nueva partida, `false` si no hay cambios en game1.
     */
    private boolean detectarNuevaPartida(DataSnapshot oldSnapshot, DataSnapshot newSnapshot) {
        for (DataSnapshot newChild : newSnapshot.getChildren()) {
            String gameId = newChild.getKey();
            if (!oldSnapshot.hasChild(gameId)) {
                Log.d(TAG, "🆕 Nueva partida detectada: " + gameId);
                return true;
            }
        }
        return false;
    }




    private void showAlert() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Nuevo Reporte Disponible")
                    .setMessage("Se han recibido nuevos datos del paciente. ¿Quieres enviar un reporte?")
                    .setPositiveButton("Enviar", (dialog, which) -> enviarReporte())
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void enviarReporte() {
        Log.d(TAG, "📤 Generando y enviando reporte...");
        String[] reportData = {"VMOOV Reporte de Sesión", "Fecha: " + System.currentTimeMillis(), "Detalles del rendimiento..."};
        PdfGenerator.createPdf(BaseActivity.this, "Reporte_VMOOOV.pdf", reportData);

        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    private boolean shouldShowAlert() {
        String currentActivity = this.getClass().getSimpleName();
        List<String> excludedActivities = Arrays.asList("MainActivity", "SignUpActivity", "RegisterActivity","ReportActivity","");
        return !excludedActivities.contains(currentActivity);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            Log.d(TAG, "🔴 Listener de Firebase eliminado en onStop().");
        }
    }
}
