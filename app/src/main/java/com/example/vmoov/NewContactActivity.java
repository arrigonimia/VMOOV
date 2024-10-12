package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

public class NewContactActivity extends AppCompatActivity {

    private EditText editText_uniqueCode;
    private Button saveButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String TAG = "NewContactActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcontact);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editText_uniqueCode = findViewById(R.id.uniqueCode_text);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uniqueCode = editText_uniqueCode.getText().toString().trim();

                if (!uniqueCode.isEmpty()) {
                    // Validar el uniqueCode y vincular al paciente
                    validateUniqueCodeAndAddContact(uniqueCode);
                } else {
                    Toast.makeText(NewContactActivity.this, "Por favor, ingrese el código único.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Funcionalidad del botón de "Atrás"
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewContactActivity.this, NotPatientActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateUniqueCodeAndAddContact(String uniqueCode) {
        try {
            // Convertir el uniqueCode a un entero
            int uniqueCodeInt = Integer.parseInt(uniqueCode);

            DatabaseReference patientsRef = mDatabase.child("patients");

            // Consultar el nodo 'patients' para encontrar el paciente con el uniqueCode proporcionado
            patientsRef.orderByChild("uniqueCode").equalTo(uniqueCodeInt).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // uniqueCode encontrado, obtener la información del paciente
                        for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                            String patientId = patientSnapshot.getKey();
                            String patientName = patientSnapshot.child("nombrePS").getValue(String.class) + " " + patientSnapshot.child("apellidoPS").getValue(String.class);

                            // Vincular al profesional de salud con el paciente
                            linkContactWithPatient(patientId, patientName, uniqueCode);
                        }
                    } else {
                        // No se encontró ningún paciente con el uniqueCode
                        Toast.makeText(NewContactActivity.this, "No se encontró ningún paciente con este código único.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    Toast.makeText(NewContactActivity.this, "Error en la base de datos. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(NewContactActivity.this, "El código único debe ser un número.", Toast.LENGTH_SHORT).show();
        }
    }


    private void linkContactWithPatient(String patientId, String patientName, String uniqueCode) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference healthProfessionalRef = mDatabase.child("healthProfessionals").child(userId).child("patients");

        // Crear un nuevo vínculo entre el profesional de salud y el paciente
        healthProfessionalRef.child(patientId).setValue(uniqueCode).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewContactActivity.this, "Paciente vinculado exitosamente.", Toast.LENGTH_SHORT).show();
                    // Volver a NotPatientActivity
                    Intent intent = new Intent(NewContactActivity.this, NotPatientActivity.class);
                    startActivity(intent);
                    finish(); // Finalizar esta actividad
                } else {
                    Toast.makeText(NewContactActivity.this, "Error al vincular el paciente. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
