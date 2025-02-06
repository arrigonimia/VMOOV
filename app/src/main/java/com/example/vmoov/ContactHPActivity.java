package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Button;

public class ContactHPActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneTextView;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Button backButton;

    private static final String TAG = "ContactHPActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_hp);

        nameTextView = findViewById(R.id.textViewName);
        emailTextView = findViewById(R.id.textViewEmail);
        phoneTextView = findViewById(R.id.textViewPhone);
        backButton = findViewById(R.id.back_button);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactHPActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        if (currentUserId == null) {
            Log.e(TAG, "Usuario no autenticado.");
            showToast("Usuario no autenticado.");
            return;
        }

        Log.d(TAG, "Usuario autenticado con ID: " + currentUserId);

        // Buscar al profesional de salud asociado al paciente logueado
        findHealthProfessional(currentUserId);
    }

    private void findHealthProfessional(String patientUserId) {
        Log.d(TAG, "Buscando profesional de salud para el paciente ID: " + patientUserId);

        mDatabase.child("healthProfessionals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot professionalsSnapshot = task.getResult();
                boolean found = false;

                for (DataSnapshot professional : professionalsSnapshot.getChildren()) {
                    DataSnapshot patientsSnapshot = professional.child("patients");

                    // Verificar si el ID del paciente está dentro del nodo "patients" de este profesional
                    if (patientsSnapshot.hasChild(patientUserId)) {
                        String professionalUserId = professional.getKey();
                        Log.d(TAG, "Profesional de salud encontrado: " + professionalUserId);
                        fetchProfessionalDetails(professionalUserId);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Log.e(TAG, "No se encontró un profesional de salud asociado.");
                    showToast("No se encontró un profesional de salud asociado.");
                }
            } else {
                Log.e(TAG, "Error al buscar el profesional de salud.", task.getException());
                showToast("Error al buscar el profesional de salud.");
            }
        });
    }

    private void fetchProfessionalDetails(String professionalUserId) {
        Log.d(TAG, "Obteniendo datos del profesional de salud con ID: " + professionalUserId);

        mDatabase.child("users").child(professionalUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot userSnapshot = task.getResult();

                String firstName = userSnapshot.child("firstName").getValue(String.class);
                String lastName = userSnapshot.child("lastName").getValue(String.class);
                String email = userSnapshot.child("email").getValue(String.class);

                if (firstName == null && lastName == null && email == null) {
                    Log.e(TAG, "No se encontraron datos del profesional.");
                    showToast("No se encontraron datos del profesional de salud.");
                    return;
                }

                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                nameTextView.setText(!fullName.trim().isEmpty() ? fullName : "Nombre no disponible");
                emailTextView.setText(email != null ? email : "Email no disponible");
                phoneTextView.setText("Teléfono no disponible");

                Log.d(TAG, "Datos obtenidos: Nombre=" + fullName + ", Email=" + email);
            } else {
                Log.e(TAG, "Error al obtener los datos del profesional de salud.", task.getException());
                showToast("Error al obtener los datos del profesional de salud.");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(ContactHPActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
