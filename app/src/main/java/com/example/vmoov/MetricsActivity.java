package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MetricsActivity extends AppCompatActivity {

    // Definir TextViews donde se mostrarán los datos
    private TextView trueCountTextView;
    private TextView birthDateTextView;
    private TextView averageTimeTextView;

    // Definir referencias de Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        // Vincular los TextViews con el layout
        trueCountTextView = findViewById(R.id.true_count);
        birthDateTextView = findViewById(R.id.birth_date);
        averageTimeTextView = findViewById(R.id.average_time);

        // Vincular los botones con el layout
        ImageButton myButton_logout = findViewById(R.id.logout_button);
        Button buttonConnect = findViewById(R.id.buttonConnect);
        ImageButton buttonSettings = findViewById(R.id.buttonSettings);

        // Inicializar FirebaseAuth y obtener el usuario actual
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Verificar si hay un usuario autenticado
        if (currentUser != null) {
            // Obtener el UID del usuario actual
            String userId = currentUser.getUid();

            // Inicializar la referencia de la base de datos para el nodo de patientmetrics
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Obtener los datos de gameplaydata y patients
            getGameData(userId);
            getPatientData(userId);
        } else {
            // Manejar el caso en que no hay un usuario autenticado
            trueCountTextView.setText("");
        }

        // Asignar acción al botón "Conectar Casco"
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

        // Asignar acción al botón "Cerrar sesión"
        myButton_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desconectar al usuario de Firebase
                FirebaseAuth.getInstance().signOut();

                // Redirigir al usuario a la pantalla de inicio de sesión o principal
                Intent intent = new Intent(MetricsActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finalizar la actividad actual para evitar que el usuario regrese con el botón "Atrás"
            }
        });

        // Asignar acción al botón "Configuración"
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getGameData(String userId) {
        DatabaseReference gameDataRef = mDatabase.child("patients").child(userId).child("gameplayData");
        gameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot lastGameSnapshot = null;
                    // Iterar sobre los juegos para obtener el último
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        lastGameSnapshot = gameSnapshot;
                    }

                    if (lastGameSnapshot != null) {
                        int trueCount = 0;
                        double totalTime = 0;
                        int stepCount = 0;

                        // Acceder a los steps dentro del último juego
                        for (DataSnapshot stepSnapshot : lastGameSnapshot.getChildren()) {
                            for (DataSnapshot stepData : stepSnapshot.getChildren()) {
                                // Log para verificar el valor de "result"
                                Boolean resultValue = stepData.child("result").getValue(Boolean.class);
                                Log.d("MetricsActivity", "Result value: " + resultValue);

                                if (resultValue != null && resultValue) {
                                    trueCount++;
                                }

                                // Log para verificar el valor de "time"
                                Double timeValue = stepData.child("time").getValue(Double.class);
                                Log.d("MetricsActivity", "Time value: " + timeValue);

                                if (timeValue != null) {
                                    totalTime += timeValue;
                                    stepCount++;
                                }
                            }
                        }

                        // Mostrar logs para verificar resultados de conteo
                        Log.d("MetricsActivity", "True count: " + trueCount);
                        Log.d("MetricsActivity", "Step count: " + stepCount);
                        Log.d("MetricsActivity", "Total time: " + totalTime);

                        // Mostrar la cantidad de 'true' en el TextView correspondiente
                        trueCountTextView.setText(String.valueOf(trueCount));

                        // Calcular y mostrar el tiempo promedio (si hay steps)
                        if (stepCount > 0) {
                            double averageTime = totalTime / stepCount;
                            // Redondear el valor a 2 decimales y mostrarlo en el TextView
                            averageTimeTextView.setText(String.format("%.2f", averageTime) + " s");
                        } else {
                            averageTimeTextView.setText("0");
                        }
                    } else {
                        trueCountTextView.setText("a");
                        averageTimeTextView.setText("b");
                    }
                } else {
                    trueCountTextView.setText("c");
                    averageTimeTextView.setText("d");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                trueCountTextView.setText("0");
                averageTimeTextView.setText("0");
            }
        });
    }

    private void getPatientData(String userId) {
        // Obtener los datos del nodo patients
        DatabaseReference patientsRef = mDatabase.child("patients").child(userId);
        patientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener birthDate y mostrar en el TextView
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                    birthDateTextView.setText(birthDate);
                } else {
                    birthDateTextView.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                birthDateTextView.setText("");
            }
        });
    }
}
