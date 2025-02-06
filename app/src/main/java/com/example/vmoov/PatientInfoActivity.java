package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class PatientInfoActivity extends AppCompatActivity {

    private TextView patientInfoText;
    private ImageButton homeButton; // CORREGIDO: Cambiado de Button a ImageButton
    private DatabaseReference mDatabase;

    private static final String TAG = "PatientInfoActivity";
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        patientId = getIntent().getStringExtra("userId");

        patientInfoText = findViewById(R.id.patientInfoText);
        homeButton = findViewById(R.id.home); // Asegúrate de que este ID coincide con el XML

        // Cargar y mostrar la información del paciente
        loadPatientInfo();

        // Configurar el botón Home para volver a NotPatientActivity
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientInfoActivity.this, NotPatientActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadPatientInfo() {
        DatabaseReference userRef = mDatabase.child("users").child(patientId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot userSnapshot = task.getResult();
                if (userSnapshot.exists()) {
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String dni = userSnapshot.child("dni").getValue(String.class);
                    String gender = userSnapshot.child("gender").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);

                    StringBuilder userInfo = new StringBuilder();
                    userInfo.append("Nombre: ").append(firstName != null ? firstName : "No disponible").append("\n");
                    userInfo.append("\nApellido: ").append(lastName != null ? lastName : "No disponible").append("\n");
                    userInfo.append("\nDNI: ").append(dni != null ? dni : "No disponible").append("\n");
                    userInfo.append("\nGénero: ").append(gender != null ? gender : "No disponible").append("\n");
                    userInfo.append("\nCorreo Electrónico: ").append(email != null ? email : "No disponible").append("\n");

                    loadAdditionalPatientInfo(userInfo);
                } else {
                    Log.e(TAG, "No se encontró información del paciente en el nodo 'users'.");
                    patientInfoText.setText("Información no disponible.");
                }
            } else {
                Log.e(TAG, "Error al obtener datos del nodo 'users': " + task.getException().getMessage());
                Toast.makeText(this, "Error al cargar la información del paciente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAdditionalPatientInfo(StringBuilder userInfo) {
        DatabaseReference patientRef = mDatabase.child("patients").child(patientId);
        patientRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot patientSnapshot = task.getResult();
                if (patientSnapshot.exists()) {
                    String birthDate = patientSnapshot.child("birthDate").getValue(String.class);
                    String contact = patientSnapshot.child("contacto").getValue(String.class);
                    String healthInsurance = patientSnapshot.child("obraSocial").getValue(String.class);
                    String memberNumber = patientSnapshot.child("numeroAfiliado").getValue(String.class);

                    if (birthDate != null && birthDate.length() == 8) {
                        String formattedBirthDate = formatBirthDate(birthDate);
                        userInfo.append("\nFecha de Nacimiento: ").append(formattedBirthDate).append("\n");

                        int age = calculateAge(birthDate);
                        userInfo.append("\nEdad: ").append(age).append(" años\n");
                    } else {
                        userInfo.append("Fecha de Nacimiento: No disponible\n");
                        userInfo.append("Edad: No disponible\n");
                    }

                    userInfo.append("\nContacto: ").append(contact != null ? contact : "No disponible").append("\n");
                    userInfo.append("\nObra Social: ").append(healthInsurance != null ? healthInsurance : "No disponible").append("\n");
                    userInfo.append("\nNúmero de Afiliado: ").append(memberNumber != null ? memberNumber : "No disponible").append("\n");

                    patientInfoText.setText(userInfo.toString());
                } else {
                    Log.e(TAG, "No se encontró información del paciente en el nodo 'patients'.");
                    patientInfoText.setText(userInfo.toString());
                }
            } else {
                Log.e(TAG, "Error al obtener datos del nodo 'patients': " + task.getException().getMessage());
                Toast.makeText(this, "Error al cargar la información adicional del paciente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatBirthDate(String birthDate) {
        String day = birthDate.substring(0, 2);
        String month = birthDate.substring(2, 4);
        String year = birthDate.substring(4, 8);
        return day + "-" + month + "-" + year;
    }

    private int calculateAge(String birthDate) {
        int day = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4));
        int year = Integer.parseInt(birthDate.substring(4, 8));

        Calendar birthDay = Calendar.getInstance();
        birthDay.set(year, month - 1, day);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
