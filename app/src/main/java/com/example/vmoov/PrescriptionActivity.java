package com.example.vmoov;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PrescriptionActivity extends AppCompatActivity {

    private EditText editTextSessions, editTextDuration, editTextSteps, editTextObservations;
    private CardView saveButton, backButton;
    private TextView patientNameTextView;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String TAG = "PrescriptionActivity";
    private String professionalId;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        // Firebase setup
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        professionalId = mAuth.getCurrentUser().getUid();

        // Obtener el patientId del intent
        patientId = getIntent().getStringExtra("userId");

        // Inicializar los campos
        patientNameTextView = findViewById(R.id.patientNameTextView);
        editTextSessions = findViewById(R.id.editTextSessions);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextSteps = findViewById(R.id.editTextSteps);
        editTextObservations = findViewById(R.id.editTextObservations);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // Cargar el nombre del paciente desde Firebase
        loadPatientName();

        // Cargar datos existentes (si hay)
        loadPrescriptionData();

        // Configurar el botón Guardar
        saveButton.setOnClickListener(v -> validateAndSave());

        // Configurar el botón Volver
        backButton.setOnClickListener(v -> finish());
    }

    private void loadPatientName() {
        DatabaseReference userRef = mDatabase.child("users").child(patientId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        patientNameTextView.setText(firstName + " " + lastName);
                    } else {
                        patientNameTextView.setText("Nombre no disponible");
                    }
                } else {
                    patientNameTextView.setText("Paciente no encontrado");
                    Log.e(TAG, "El nodo del paciente no existe en 'users'");
                }
            } else {
                patientNameTextView.setText("Error al cargar nombre");
                Log.e(TAG, "Error al obtener datos: " + task.getException().getMessage());
            }
        });
    }

    private void loadPrescriptionData() {
        DatabaseReference prescriptionRef = mDatabase
                .child("healthProfessionals")
                .child(professionalId)
                .child("patients")
                .child(patientId)
                .child("prescription");

        prescriptionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    try {
                        Long sessions = snapshot.child("sessions").getValue(Long.class);
                        Long duration = snapshot.child("duration").getValue(Long.class);
                        Long steps = snapshot.child("steps").getValue(Long.class);
                        String observations = snapshot.child("observations").getValue(String.class);

                        if (sessions != null) editTextSessions.setText(String.valueOf(sessions));
                        if (duration != null) editTextDuration.setText(String.valueOf(duration));
                        if (steps != null) editTextSteps.setText(String.valueOf(steps));
                        if (observations != null) editTextObservations.setText(observations);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al cargar datos: " + e.getMessage());
                        Toast.makeText(PrescriptionActivity.this, "Error al cargar datos de la receta.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No se encontraron datos de receta médica para este paciente.");
                }
            } else {
                Log.e(TAG, "Error al obtener datos: " + task.getException().getMessage());
            }
        });
    }

    private void validateAndSave() {
        try {
            int sessions = Integer.parseInt(editTextSessions.getText().toString().trim());
            int duration = Integer.parseInt(editTextDuration.getText().toString().trim());
            int steps = Integer.parseInt(editTextSteps.getText().toString().trim());
            String observations = editTextObservations.getText().toString().trim();

            showConfirmationDialog(sessions, duration, steps, observations);
        } catch (NumberFormatException e) {
            Toast.makeText(PrescriptionActivity.this, "Por favor, ingrese solo números válidos en los campos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog(int sessions, int duration, int steps, String observations) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar cambios")
                .setMessage("¿Estás seguro de que deseas guardar los cambios?")
                .setPositiveButton("Guardar", (dialog, which) -> savePrescription(sessions, duration, steps, observations))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void savePrescription(int sessions, int duration, int steps, String observations) {
        DatabaseReference prescriptionRef = mDatabase
                .child("healthProfessionals")
                .child(professionalId)
                .child("patients")
                .child(patientId)
                .child("prescription");

        prescriptionRef.child("sessions").setValue(sessions);
        prescriptionRef.child("duration").setValue(duration);
        prescriptionRef.child("steps").setValue(steps);
        prescriptionRef.child("observations").setValue(observations)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PrescriptionActivity.this, "Receta médica guardada con éxito.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PrescriptionActivity.this, "Error al guardar la receta médica.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al guardar la receta médica: " + task.getException().getMessage());
                    }
                });
    }
}
