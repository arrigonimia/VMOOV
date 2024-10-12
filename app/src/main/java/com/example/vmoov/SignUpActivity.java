package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editText_fname;
    private EditText editText_lname;
    private EditText editText_dni;
    private EditText editText_gender;
    private EditText editText_email;
    private EditText editText_pass;
    private EditText editText_repass;
    private Button guardarButton;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int userType = 0; // Inicializar el tipo de usuario a 0

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
        editText_email = findViewById(R.id.email_text);
        editText_pass = findViewById(R.id.pass_text);
        editText_repass = findViewById(R.id.repass_text);
        guardarButton = findViewById(R.id.signUp_button);
        checkBox = findViewById(R.id.checkboxUserType);

        // Listener para el checkbox (para seleccionar si es profesional de salud)
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> userType = isChecked ? 1 : 0);

        guardarButton.setOnClickListener(v -> {
            String firstName = editText_fname.getText().toString();
            String lastName = editText_lname.getText().toString();
            String dni = editText_dni.getText().toString();
            String gender = editText_gender.getText().toString();
            String email = editText_email.getText().toString();
            String password = editText_pass.getText().toString();

            if (password.equals(editText_repass.getText().toString())) {
                // Crear cuenta en Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid(); // Obtener UID

                                // Crear el objeto usuario
                                User user = new User(firstName, lastName, dni, gender, email, password, userType);

                                // Guardar la información del usuario
                                if (userType == 0) {
                                    // Guardar paciente en "users"
                                    mDatabase.child("users").child(userId).setValue(user);
                                    // Redirigir a la actividad de pacientes
                                    Intent intent = new Intent(SignUpActivity.this, PatientSignUpActivity.class);
                                    intent.putExtra("userId", userId);
                                    startActivity(intent);
                                } else {
                                    // Guardar profesional de salud en "healthProfessionals"
                                    mDatabase.child("healthProfessionals").child(userId).setValue(user);
                                    // Redirigir a la actividad de profesionales de salud
                                    Intent intent = new Intent(SignUpActivity.this, NotPatientActivity.class);
                                    intent.putExtra("userId", userId);
                                    startActivity(intent);
                                }
                            } else {
                                Log.e(TAG, "Error durante el registro: ", task.getException());
                                Toast.makeText(SignUpActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(SignUpActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
