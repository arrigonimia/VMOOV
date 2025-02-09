package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editText_fname, editText_lname, editText_dni, editText_gender, editText_phone;
    private EditText editText_email, editText_pass, editText_repass;
    private CardView guardarButton, backButton;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int userType = 0;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inicializar FirebaseAuth y DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Vincular elementos del layout
        editText_fname = findViewById(R.id.firstname_text);
        editText_lname = findViewById(R.id.lastname_text);
        editText_dni = findViewById(R.id.dni_text);
        editText_gender = findViewById(R.id.gender_text);
        editText_phone = findViewById(R.id.phone_text);
        editText_email = findViewById(R.id.email_text);
        editText_pass = findViewById(R.id.pass_text);
        editText_repass = findViewById(R.id.repass_text);
        guardarButton = findViewById(R.id.signUp_button);
        backButton = findViewById(R.id.back_button);
        checkBox = findViewById(R.id.checkboxUserType);

        // Listener para el checkbox (para seleccionar si es profesional de salud)
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> userType = isChecked ? 1 : 0);

        // ✅ Llamada a validación y registro
        guardarButton.setOnClickListener(v -> validateAndRegister());

        // ✅ Botón de regresar a MainActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndRegister() {
        String firstName = editText_fname.getText().toString().trim();
        String lastName = editText_lname.getText().toString().trim();
        String dni = editText_dni.getText().toString().trim();
        String gender = editText_gender.getText().toString().trim();
        String phone = editText_phone.getText().toString().trim();
        String email = editText_email.getText().toString().trim();
        String password = editText_pass.getText().toString().trim();
        String confirmPassword = editText_repass.getText().toString().trim();

        // Validación de campos vacíos
        if (firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty() || gender.isEmpty() ||
                phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignUpActivity.this, "Ingrese un correo electrónico válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de contraseña
        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si pasa todas las validaciones, proceder con Firebase Auth
        registerUser(firstName, lastName, dni, gender, phone, email, password);
    }

    private void registerUser(String firstName, String lastName, String dni, String gender, String phone, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Crear objeto usuario con teléfono
                        User user = new User(firstName, lastName, dni, gender, phone, email, password, userType);

                        // Guardar en Firebase
                        mDatabase.child("users").child(userId).setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Registro exitoso.", Toast.LENGTH_SHORT).show();

                                        // 🚀 Redirección según el tipo de usuario
                                        Intent intent;
                                        if (userType == 1) {
                                            intent = new Intent(SignUpActivity.this, HealthProfessionalInfoActivity.class);
                                            intent.putExtra("source", "SignUpActivity");  // Pasar el origen
                                            startActivity(intent);
                                        } else {
                                            intent = new Intent(SignUpActivity.this, PatientSignUpActivity.class);
                                            intent.putExtra("userId", userId);
                                            startActivity(intent);
                                        }

                                        intent.putExtra("userId", userId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Error al guardar datos del usuario.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
