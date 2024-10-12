package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText editText_user;
    private EditText editText_pass;
    private Button guardarButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        editText_user = findViewById(R.id.user_text);
        editText_pass = findViewById(R.id.pass_text);
        guardarButton = findViewById(R.id.button_prim);

        guardarButton.setOnClickListener(v -> {
            String email = editText_user.getText().toString();
            String password = editText_pass.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            // Verificar si es un paciente o profesional de salud
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        int userType = dataSnapshot.child("userType").getValue(Integer.class);
                                        if (userType == 0) {
                                            // Redirigir a la actividad del paciente
                                            Intent intent = new Intent(MainActivity.this, MetricsActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // Redirigir a la actividad del profesional de salud
                                            Intent intent = new Intent(MainActivity.this, NotPatientActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        // Revisar en "healthProfessionals" si no está en "users"
                                        DatabaseReference healthRef = FirebaseDatabase.getInstance().getReference().child("healthProfessionals").child(userId);
                                        healthRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    // Si está en healthProfessionals, redirigir a NotPatientActivity
                                                    Intent intent = new Intent(MainActivity.this, NotPatientActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Tipo de usuario no encontrado", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(MainActivity.this, "Error al leer datos de usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(MainActivity.this, "Error al leer datos de usuario", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "Fallo en la autenticación", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Listener para registrarse
        Button signUpButton = findViewById(R.id.smallSignUp_button);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
