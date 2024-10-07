package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText editText_user;
    private EditText editText_pass;
    private Button guardarButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location location;
    private FirebaseAuth mAuth;
    private Button forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button SignUp_button = findViewById(R.id.smallSignUp_button);

        editText_user = findViewById(R.id.user_text);
        editText_pass = findViewById(R.id.pass_text);
        guardarButton = findViewById(R.id.button_prim);
        forgotPassword = findViewById(R.id.forgot_password_button);

        if (guardarButton != null) {
            guardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Usuario_Ingresado = editText_user.getText().toString();
                    String Password_Ingresada = editText_pass.getText().toString();

                    mAuth.signInWithEmailAndPassword(Usuario_Ingresado, Password_Ingresada)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

                                        userRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    int userType = dataSnapshot.child("userType").getValue(Integer.class);

                                                    if (userType == 0) {
                                                        Intent intent = new Intent(MainActivity.this, MetricsActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        Intent intent = new Intent(MainActivity.this, NotPatientActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // Handle any errors that may occur during the read operation
                                                Toast.makeText(MainActivity.this, "Failed to read user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        updateUI(null, true); // Authentication failed
                                    }
                                }
                            });
                }
            });
        }

        SignUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                // Here you have the current location, and you can proceed to use it.
            }
        };
    }

    private void updateUI(FirebaseUser user, boolean authFailed) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, MetricsActivity.class);
            startActivity(intent);
        } else {
            if (authFailed) {
                Toast.makeText(MainActivity.this, "Authentication failed, please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
