package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EditInfoActivity extends BaseActivity {

    private EditText dniEdit, phoneEdit, emailEdit, numeroAfiliadoEdit;
    private EditText firstNameEdit, lastNameEdit;
    private Spinner genderSpinner, obraSocialSpinner;
    private TextView birthDateEdit;
    private CardView saveButton, backButton;

    private DatabaseReference usersRef, patientsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        // Inicializar Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        patientsRef = FirebaseDatabase.getInstance().getReference("patients").child(userId);

        // Referencias a los elementos UI
        firstNameEdit = findViewById(R.id.edit_first_name);
        lastNameEdit = findViewById(R.id.edit_last_name);
        dniEdit = findViewById(R.id.edit_dni);
        phoneEdit = findViewById(R.id.edit_phone);
        emailEdit = findViewById(R.id.edit_email);
        birthDateEdit = findViewById(R.id.edit_birth_date);
        numeroAfiliadoEdit = findViewById(R.id.edit_numero_afiliado);
        genderSpinner = findViewById(R.id.gender_spinner);
        obraSocialSpinner = findViewById(R.id.obra_social_spinner);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        // Cargar los datos del usuario automáticamente
        loadUserData();

        // Configurar DatePicker para fecha de nacimiento
        birthDateEdit.setOnClickListener(v -> showDatePickerDialog());

        // Configurar Spinner de género
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Configurar Spinner de Obra Social
        ArrayAdapter<CharSequence> healthAdapter = ArrayAdapter.createFromResource(this,
                R.array.obra_social_array, android.R.layout.simple_spinner_item);
        healthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        obraSocialSpinner.setAdapter(healthAdapter);

        // Botón "Guardar" con confirmación
        saveButton.setOnClickListener(v -> confirmSaveData());

        // Botón "Volver" para regresar a SettingsActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditInfoActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        // Cargar datos de `users`
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    firstNameEdit.setText(snapshot.child("firstName").getValue(String.class));
                    lastNameEdit.setText(snapshot.child("lastName").getValue(String.class));
                    dniEdit.setText(snapshot.child("dni").getValue(String.class));
                    phoneEdit.setText(snapshot.child("phone").getValue(String.class));
                    emailEdit.setText(snapshot.child("email").getValue(String.class));

                    // Configurar Spinner de género
                    String gender = snapshot.child("gender").getValue(String.class);
                    setSpinnerSelection(genderSpinner, gender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditInfoActivity.this, "Error al cargar datos de usuario", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar datos de `patients`
        patientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object birthDateObj = snapshot.child("birthDate").getValue();
                    birthDateEdit.setText(birthDateObj != null ? String.valueOf(birthDateObj) : "");

                    Object numeroAfiliadoObj = snapshot.child("numeroAfiliado").getValue();
                    numeroAfiliadoEdit.setText(numeroAfiliadoObj != null ? String.valueOf(numeroAfiliadoObj) : "");

                    // Configurar Spinner de Obra Social
                    String obraSocial = snapshot.child("obraSocial").getValue(String.class);
                    setSpinnerSelection(obraSocialSpinner, obraSocial);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditInfoActivity.this, "Error al cargar datos de paciente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmSaveData() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar cambios")
                .setMessage("¿Estás seguro de que quieres guardar los cambios?")
                .setPositiveButton("Sí", (dialog, which) -> saveUserData())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveUserData() {
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String dni = dniEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String birthDate = birthDateEdit.getText().toString().trim();
        String obraSocial = obraSocialSpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String numeroAfiliado = numeroAfiliadoEdit.getText().toString().trim();

        // Validaciones
        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", firstName)) {
            showToast("Nombre inválido (solo letras y espacios)");
            return;
        }
        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", lastName)) {
            showToast("Apellido inválido (solo letras y espacios)");
            return;
        }
        if (!Pattern.matches("^\\d{8}$", dni)) {
            showToast("DNI inválido (8 dígitos numéricos)");
            return;
        }
        if (!Pattern.matches("^\\d{10}$", phone)) {
            showToast("Número de teléfono inválido (10 dígitos numéricos)");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showToast("Correo inválido (debe contener '@' y '.')");
            return;
        }
        if (birthDate.isEmpty()) {
            showToast("Seleccione una fecha de nacimiento");
            return;
        }

        // Guardar en Firebase
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("dni", dni);
        userData.put("phone", phone);
        userData.put("email", email);
        userData.put("gender", gender);

        Map<String, Object> patientData = new HashMap<>();
        patientData.put("birthDate", birthDate);
        patientData.put("obraSocial", obraSocial);
        patientData.put("numeroAfiliado", numeroAfiliado);

        usersRef.updateChildren(userData);
        patientsRef.updateChildren(patientData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Datos actualizados");
                finish();
            } else {
                showToast("Error al guardar");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                    birthDateEdit.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

}
