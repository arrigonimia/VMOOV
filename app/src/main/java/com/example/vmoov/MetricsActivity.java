package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MetricsActivity extends AppCompatActivity {

    // Definir TextViews donde se mostrarán los datos
    private TextView seizureCountTextView;
    private TextView seizureDurationTextView;
    private TextView birthDateTextView;
    private TextView bloodTypeTextView;

    // Definir referencias de Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        // Vincular los TextViews con el layout
        seizureCountTextView = findViewById(R.id.days);
        seizureDurationTextView = findViewById(R.id.duration_value);
        birthDateTextView = findViewById(R.id.seizure_month);
        bloodTypeTextView = findViewById(R.id.days);

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

            // Obtener los datos del usuario desde Firebase
            getMetricsData(userId);
            getPatientData(userId);
        } else {
            // Manejar el caso en que no hay un usuario autenticado
            seizureCountTextView.setText("");
            seizureDurationTextView.setText("");
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

    private void getMetricsData(String userId) {
        // Obtener los datos de patientmetrics para el usuario autenticado
        DatabaseReference metricsRef = mDatabase.child("patientmetrics").child(userId);
        metricsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener seizureCount y seizureDuration
                    int seizureCount = dataSnapshot.child("seizureCount").getValue(Integer.class);
                    int seizureDuration = dataSnapshot.child("seizureDuration").getValue(Integer.class);

                    // Mostrar solo los valores en los TextViews
                    seizureCountTextView.setText(String.valueOf(seizureCount));
                    seizureDurationTextView.setText(String.valueOf(seizureDuration));
                } else {
                    seizureCountTextView.setText("");
                    seizureDurationTextView.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores de Firebase
                seizureCountTextView.setText("");
                seizureDurationTextView.setText("");
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
                    // Obtener birthDate y bloodType
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                    String bloodType = dataSnapshot.child("bloodType").getValue(String.class);

                    // Mostrar solo los valores en los TextViews
                    birthDateTextView.setText(birthDate);
                    bloodTypeTextView.setText(bloodType);
                } else {
                    birthDateTextView.setText("");
                    bloodTypeTextView.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores de Firebase
                birthDateTextView.setText("");
                bloodTypeTextView.setText("");
            }
        });
    }
}
