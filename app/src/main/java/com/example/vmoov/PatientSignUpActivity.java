package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;

public class PatientSignUpActivity extends AppCompatActivity {

    private EditText editText_birth;
    private EditText editText_contact;
    private EditText editText_obraS;
    private EditText editText_numeroAfi;
    private Button guardarButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientsignup);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editText_birth = findViewById(R.id.birth_text);
        editText_contact = findViewById(R.id.contact_text);
        editText_obraS = findViewById(R.id.obraS_text);
        editText_numeroAfi = findViewById(R.id.numeroAfi_text);

        guardarButton = findViewById(R.id.signUp_button);

        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Birthday_in = editText_birth.getText().toString();
                String Contact_in = editText_contact.getText().toString();
                String ObraS_in = editText_obraS.getText().toString();
                String NumeroAfi_in = editText_numeroAfi.getText().toString();

                String userId = mAuth.getCurrentUser().getUid(); // Get the current user's UID

                // Generate a unique 4-digit code
                generateUniqueCode(new OnCodeGeneratedListener() {
                    @Override
                    public void onCodeGenerated(int uniqueCode) {
                        // Create a new Patient object with the unique code
                        Patient patient = new Patient(Birthday_in, Contact_in, ObraS_in, NumeroAfi_in, uniqueCode);

                        // Save the patient data under the user's UID in the /patients node
                        mDatabase.child("patients").child(userId).setValue(patient, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(PatientSignUpActivity.this, "Patient data saved.", Toast.LENGTH_SHORT).show();
                                    createDefaultPatientMetrics(userId); // Create default patient metrics with only userId

                                    // Proceed to RegistrationSuccessActivity and pass the userId
                                    Intent intent = new Intent(PatientSignUpActivity.this, RegistrationSuccessActivity.class);
                                    intent.putExtra("userId", userId); // Pass the userId to the next activity
                                    startActivity(intent);
                                    finish(); // Close this activity to avoid returning to it
                                } else {
                                    Toast.makeText(PatientSignUpActivity.this, "Failed to save patient data.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        Button Back = findViewById(R.id.back_button);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientSignUpActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createDefaultPatientMetrics(String userId) {
        // Crear una referencia a patientmetrics con el userId como clave
        DatabaseReference patientMetricsRef = FirebaseDatabase.getInstance().getReference("patientmetrics").child(userId);

        // Establecer el nodo con un valor nulo, lo que genera un nodo vacío
        patientMetricsRef.setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(PatientSignUpActivity.this, "Patient metrics node created without value.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PatientSignUpActivity.this, "Failed to create patient metrics node.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    private void generateUniqueCode(OnCodeGeneratedListener listener) {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000; // Generate a 4-digit number between 1000 and 9999

        mDatabase.child("patients").orderByChild("uniqueCode").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the code already exists, generate a new one recursively
                    generateUniqueCode(listener);
                } else {
                    // If the code is unique, return it through the listener
                    listener.onCodeGenerated(code);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    // Listener interface for the unique code generation
    interface OnCodeGeneratedListener {
        void onCodeGenerated(int uniqueCode);
    }
}
