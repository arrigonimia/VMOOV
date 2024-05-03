package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PatientSignUpActivity extends AppCompatActivity {

    private EditText editText_birth;
    private EditText editText_blood;
    private EditText editText_epitherapy;
    private EditText editText_medications;
    private EditText editText_pathologies;
    private Button guardarButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientsignup);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Updated database reference

        editText_birth = findViewById(R.id.birth_text);
        editText_blood = findViewById(R.id.blood_text);
        editText_epitherapy = findViewById(R.id.epitherapy_text);
        editText_medications = findViewById(R.id.medications_text);
        editText_pathologies = findViewById(R.id.pathologies_text);

        guardarButton = findViewById(R.id.signUp_button);

        if (guardarButton != null) {
            guardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Birthday_in = editText_birth.getText().toString();
                    String Blood_in = editText_blood.getText().toString();
                    String EpilepsyTherapy_in = editText_epitherapy.getText().toString();
                    String Medications_in = editText_medications.getText().toString();
                    String Pathologies_in = editText_pathologies.getText().toString();

                    String userId = mAuth.getCurrentUser().getUid(); // Get the current user's UID

                    // Create a new Patient object
                    Patient patient = new Patient(Birthday_in, Blood_in, EpilepsyTherapy_in, Medications_in, Pathologies_in);

                    // Save the patient data under the user's UID in the /patients node
                    mDatabase.child("patients").child(userId).setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PatientSignUpActivity.this, "Patient data saved.", Toast.LENGTH_SHORT).show();
                                createDefaultPatientMetrics(userId);
                                // Proceed to the main activity or any other activity
                                Intent intent = new Intent(PatientSignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(PatientSignUpActivity.this, "Failed to save patient data.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

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
        // Assuming you have a PatientMetrics class with default values
        PatientMetrics defaultMetrics = new PatientMetrics(0.0f, 0, "", 0.0f);

        // Save the default patient metrics data under the user's UID in the /patientmetrics node
        mDatabase.child("patientmetrics").child(userId).setValue(defaultMetrics)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PatientSignUpActivity.this, "Default patient metrics created.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PatientSignUpActivity.this, "Failed to create default patient metrics.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
