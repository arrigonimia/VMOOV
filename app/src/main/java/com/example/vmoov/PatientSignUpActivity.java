package com.example.vmoov;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PatientSignUpActivity extends BaseActivity {

    private TextView textViewBirth;
    private Spinner spinnerObraS;
    private TextView editTextNumeroAfi;
    private CardView guardarCard, backCard;
    private String firstName, lastName, dni, gender, phone, email, password;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientsignup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textViewBirth = findViewById(R.id.birth_text);
        spinnerObraS = findViewById(R.id.obraS_spinner);
        editTextNumeroAfi = findViewById(R.id.numeroAfi_text);
        guardarCard = findViewById(R.id.register_card);
        backCard = findViewById(R.id.back_card);

        textViewBirth.setOnClickListener(v -> showDatePickerDialog());
        setupHealthInsuranceSpinner();

        // Recibir datos del intent
        Intent intent = getIntent();
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        dni = intent.getStringExtra("dni");
        gender = intent.getStringExtra("gender");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        guardarCard.setOnClickListener(v -> registerPatient());

        backCard.setOnClickListener(v -> {
            Intent backIntent = new Intent(PatientSignUpActivity.this, SignUpActivity.class);
            startActivity(backIntent);
            finish();
        });
    }

    private void registerPatient() {
        String birthDate = textViewBirth.getText().toString();
        String obraS = spinnerObraS.getSelectedItem().toString();
        String numeroAfi = editTextNumeroAfi.getText().toString();

        if (!validateInput(birthDate, obraS, numeroAfi)) return;

        // 🔹 Crear cuenta en Firebase Authentication aquí
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(PatientSignUpActivity.this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        generateUniqueCode(uniqueCode -> {
                            // 🔹 Guardar datos en `patients`
                            Map<String, Object> patientData = new HashMap<>();
                            patientData.put("birthDate", birthDate);
                            patientData.put("obraSocial", obraS);
                            patientData.put("numeroAfiliado", numeroAfi);
                            patientData.put("uniqueCode", uniqueCode);

                            mDatabase.child("patients").child(userId).setValue(patientData);

                            // 🔹 Guardar también en `users`
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("firstName", firstName);
                            userData.put("lastName", lastName);
                            userData.put("dni", dni);
                            userData.put("gender", gender);
                            userData.put("phone", phone);
                            userData.put("email", email);
                            userData.put("userType", 0);
                            userData.put("uniqueCode", uniqueCode);

                            mDatabase.child("users").child(userId).setValue(userData);

                            // 🔹 Redirigir a la pantalla de éxito
                            Intent intent = new Intent(PatientSignUpActivity.this, RegistrationSuccessActivity.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        showToast("Error en el registro: " + task.getException().getMessage());
                    }
                });
    }


    private void setupHealthInsuranceSpinner() {
        String[] healthInsuranceArray = getResources().getStringArray(R.array.obra_social_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, healthInsuranceArray) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // La opción "Seleccione una Obra Social" no es seleccionable
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                int hintColor = ContextCompat.getColor(getContext(), R.color.lightblue);
                int normalColor = ContextCompat.getColor(getContext(), R.color.blue);

                textView.setTextColor(position == 0 ? hintColor : normalColor);
                return view;
            }
        };
        spinnerObraS.setAdapter(adapter);
        spinnerObraS.setSelection(0); // Establecer el hint como opción inicial
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    textViewBirth.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }


    private boolean validateInput(String birthDate, String obraS, String numeroAfi) {
        if (birthDate.isEmpty()) {
            showToast("Seleccione una fecha de nacimiento.");
            return false;
        }
        if (spinnerObraS.getSelectedItemPosition() == 0) {
            showToast("Seleccione una Obra Social válida.");
            return false;
        }
        if (numeroAfi.isEmpty()) {
            showToast("Ingrese el número de afiliado.");
            return false;
        }
        return true;
    }

    private void createDefaultPatientMetrics(String userId) {
        DatabaseReference patientMetricsRef = mDatabase.child("patientmetrics").child(userId);

        // Usar un HashMap vacío en lugar de null para que el nodo se cree
        patientMetricsRef.setValue(new HashMap<>(), (databaseError, databaseReference) -> {
            if (databaseError == null) {
                showToast("Nodo patientmetrics creado correctamente.");
            } else {
                showToast("Error al crear patientmetrics.");
            }
        });
    }


    private void generateUniqueCode(OnCodeGeneratedListener listener) {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;

        mDatabase.child("patients").orderByChild("uniqueCode").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    generateUniqueCode(listener);
                } else {
                    listener.onCodeGenerated(code);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    interface OnCodeGeneratedListener {
        void onCodeGenerated(int uniqueCode);
    }

    private void showToast(String message) {
        Toast.makeText(PatientSignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
