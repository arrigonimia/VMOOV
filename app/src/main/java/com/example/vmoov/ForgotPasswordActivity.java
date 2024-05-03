package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailResetText;
    private Button resetPasswordButton;
    private Button backButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        emailResetText = findViewById(R.id.email_reset_text);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        backButton = findViewById(R.id.back_button);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailResetText.getText().toString();

                if (emailAddress.isEmpty()) {
                    // Handle the case where the email address is empty
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                } else {
                    // Send a password reset email
                    sendPasswordResetEmail(emailAddress);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the main activity
                startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
            }
        });
    }

    private void sendPasswordResetEmail(String emailAddress) {
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent. Check your email.", Toast.LENGTH_SHORT).show();
                            // Navigate to the ResetCodeActivity
                            startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
                        } else {
                            // Handle the case where sending the email fails
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
