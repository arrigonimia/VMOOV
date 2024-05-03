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
    private EditText editText_gender;
    private EditText editText_email;
    private EditText editText_pass;
    private EditText editText_repass;
    private Button guardarButton;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int userType = 0; // Initialize user type to 0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase references
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

        editText_fname = findViewById(R.id.firstname_text);
        editText_lname = findViewById(R.id.lastname_text);
        editText_gender = findViewById(R.id.gender_text);
        editText_email = findViewById(R.id.email_text);
        editText_pass = findViewById(R.id.pass_text);
        editText_repass = findViewById(R.id.repass_text);
        guardarButton = findViewById(R.id.signUp_button);
        checkBox = findViewById(R.id.checkboxUserType);

        // Add an OnCheckedChangeListener to the checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the userType based on checkbox status
                userType = isChecked ? 1 : 0;
            }
        });

        // Enable or disable the sign-up button based on password validation
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check if passwords match and meet length criteria
                String password = editText_pass.getText().toString();
                String rePassword = editText_repass.getText().toString();

                boolean passwordsMatch = password.equals(rePassword);
                boolean passwordLengthValid = password.length() >= 6;

                // Enable or disable the sign-up button
                guardarButton.setEnabled(passwordsMatch && passwordLengthValid);

                // Provide real-time feedback to the user
                if (!passwordsMatch) {
                    editText_repass.setError("Passwords do not match");
                } else {
                    editText_repass.setError(null);
                }

                if (!passwordLengthValid) {
                    editText_pass.setError("Password is too short (min 6 characters)");
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
                    String First_name = editText_fname.getText().toString();
                    String Last_name = editText_lname.getText().toString();
                    String Gender = editText_gender.getText().toString();
                    String Email = editText_email.getText().toString();
                    String Password = editText_pass.getText().toString();
                    String Repassword = editText_repass.getText().toString();

                    // Create a new user in Firebase Authentication
                    mAuth.createUserWithEmailAndPassword(Email, Password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful() && Password.equals(Repassword)) {
                                        String userId = mAuth.getCurrentUser().getUid(); // Get the UID

                                        User user = new User(First_name, Last_name, Email, Password, Gender, userType);

                                        // Save the user data
                                        mDatabase.child("users").child(userId).setValue(user);

                                        if (userType == 1) {
                                            // User is a patient
                                            Intent intent = new Intent(SignUpActivity.this, PatientSignUpActivity.class);
                                            intent.putExtra("userId", userId); // Pass the UID to the next activity
                                            startActivity(intent);
                                        } else {
                                            // User is not a patient
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
    }
}