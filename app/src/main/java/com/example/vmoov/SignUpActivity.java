package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editText_fname;
    private EditText editText_lname;
    private EditText editText_dni;  // Nuevo campo para DNI
    private EditText editText_gender;
    private EditText editText_email;
    private EditText editText_pass;
    private EditText editText_repass;
    private Button guardarButton;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int userType = 0; // Inicializar el tipo de usuario a 0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inicializar referencias de Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button Back = findViewById(R.id.back_button);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Vincular los campos con los elementos en el layout
        editText_fname = findViewById(R.id.firstname_text);
        editText_lname = findViewById(R.id.lastname_text);
        editText_dni = findViewById(R.id.dni_text);            // Nuevo campo para DNI
        editText_gender = findViewById(R.id.gender_text);
        editText_email = findViewById(R.id.email_text);
        editText_pass = findViewById(R.id.pass_text);
        editText_repass = findViewById(R.id.repass_text);
        guardarButton = findViewById(R.id.signUp_button);
        checkBox = findViewById(R.id.checkboxUserType);

        // Listener para el checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Actualizar el tipo de usuario basado en el estado del checkbox
                userType = isChecked ? 1 : 0;
            }
        });

        // Habilitar o deshabilitar el botón de registro basado en la validación de las contraseñas
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Comprobar si las contraseñas coinciden y cumplen los criterios de longitud
                String password = editText_pass.getText().toString();
                String rePassword = editText_repass.getText().toString();

                boolean passwordsMatch = password.equals(rePassword);
                boolean passwordLengthValid = password.length() >= 6;

                // Habilitar o deshabilitar el botón de registro
                guardarButton.setEnabled(passwordsMatch && passwordLengthValid);

                // Proveer retroalimentación en tiempo real al usuario
                if (!passwordsMatch) {
                    editText_repass.setError("Las contraseñas no coinciden");
                } else {
                    editText_repass.setError(null);
                }

                if (!passwordLengthValid) {
                    editText_pass.setError("La contraseña es demasiado débil (mínimo 6 caracteres)");
                } else {
                    editText_pass.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        editText_pass.addTextChangedListener(passwordWatcher);
        editText_repass.addTextChangedListener(passwordWatcher);

        if (guardarButton != null) {
            guardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstName = editText_fname.getText().toString();
                    String lastName = editText_lname.getText().toString();
                    String dni = editText_dni.getText().toString();             // Obtener el valor del DNI
                    String gender = editText_gender.getText().toString();
                    String email = editText_email.getText().toString();
                    String password = editText_pass.getText().toString();

                    if (password.equals(editText_repass.getText().toString())) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String userId = mAuth.getCurrentUser().getUid(); // Obtener el UID

                                            // Crear el objeto usuario con el DNI y otros datos
                                            User user = new User(firstName, lastName, dni, gender, email, password, userType);

                                            // Guardar los datos del usuario en Firebase
                                            mDatabase.child("users").child(userId).setValue(user);

                                            if (userType == 0) {
                                                // Si es paciente, ir a PatientSignUpActivity
                                                Intent intent = new Intent(SignUpActivity.this, PatientSignUpActivity.class);
                                                intent.putExtra("userId", userId); // Pasar el UID a la siguiente actividad
                                                startActivity(intent);
                                            } else {
                                                // Si no es paciente, ir al MainActivity
                                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "El registro falló. Por favor, vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
