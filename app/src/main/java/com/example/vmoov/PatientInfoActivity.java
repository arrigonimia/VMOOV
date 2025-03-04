package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PatientInfoActivity extends BaseActivity {

    private TextView textName, textLastName, textDni, textAge, textBirthDate;
    private TextView textGender, textHealthInsurance, textMemberNumber, textEmail, textContact;
    private ImageButton homeButton;
    private DatabaseReference mDatabase;
    private static final String TAG = "PatientInfoActivity";
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        patientId = getIntent().getStringExtra("userId");

        // Asignamos los TextView del layout
        textName = findViewById(R.id.textName);
        textLastName = findViewById(R.id.textLastName);
        textDni = findViewById(R.id.textDni);
        textAge = findViewById(R.id.textAge);
        textBirthDate = findViewById(R.id.textBirthDate);
        textGender = findViewById(R.id.textGender);
        textHealthInsurance = findViewById(R.id.textHealthInsurance);
        textMemberNumber = findViewById(R.id.textMemberNumber);
        textEmail = findViewById(R.id.textEmail);
        textContact = findViewById(R.id.textContact);
        homeButton = findViewById(R.id.home);

        // Cargar la información del paciente
        loadPatientInfo();

        // Botón de Home
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
                    loadAdditionalPatientInfo(userSnapshot);
                } else {
                    Log.e(TAG, "No se encontró información en 'users'.");
                    Toast.makeText(this, "Información no disponible.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error en 'users': " + task.getException().getMessage());
                Toast.makeText(this, "Error al cargar datos del paciente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAdditionalPatientInfo(DataSnapshot userSnapshot) {
        DatabaseReference patientRef = mDatabase.child("patients").child(patientId);
        patientRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot patientSnapshot = task.getResult();
                if (patientSnapshot.exists()) {
                    // Datos de 'users'
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String dni = userSnapshot.child("dni").getValue(String.class);
                    String gender = userSnapshot.child("gender").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String contact = userSnapshot.child("phone").getValue(String.class); // 📌 Ahora se toma desde `phone`

                    // Datos de 'patients'
                    String birthDate = patientSnapshot.child("birthDate").getValue(String.class); // 📌 Ahora en formato `dd/MM/yyyy`
                    String healthInsurance = patientSnapshot.child("obraSocial").getValue(String.class);
                    String memberNumber = patientSnapshot.child("numeroAfiliado").getValue(String.class);

                    // Calcular edad con el nuevo formato `dd/MM/yyyy`
                    String ageText = "No disponible";
                    if (birthDate != null && birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                        int age = calculateAge(birthDate);
                        ageText = age + " años";
                    }

                    // Asignar valores a cada campo en la tabla del layout
                    textName.setText(firstName != null ? firstName : "No disponible");
                    textLastName.setText(lastName != null ? lastName : "No disponible");
                    textDni.setText(dni != null ? dni : "No disponible");
                    textAge.setText(ageText);
                    textBirthDate.setText(birthDate != null ? birthDate : "No disponible"); // 📌 Mostrar fecha sin necesidad de conversión
                    textGender.setText(gender != null ? gender : "No disponible");
                    textHealthInsurance.setText(healthInsurance != null ? healthInsurance : "No disponible");
                    textMemberNumber.setText(memberNumber != null ? memberNumber : "No disponible");
                    textEmail.setText(email != null ? email : "No disponible");
                    textContact.setText(contact != null ? contact : "No disponible");

                } else {
                    Log.e(TAG, "No se encontró información en 'patients'.");
                    Toast.makeText(this, "Información adicional no disponible.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error en 'patients': " + task.getException().getMessage());
                Toast.makeText(this, "Error al cargar datos adicionales.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateAge(String birthDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTime(sdf.parse(birthDate));

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;

        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear la fecha de nacimiento: " + e.getMessage());
            return 0;
        }
    }
}
