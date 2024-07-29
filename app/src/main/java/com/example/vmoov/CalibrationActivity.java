package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends AppCompatActivity {

//    private float calibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        Button yesButton = findViewById(R.id.location_button_yes);
        Button noButton = findViewById(R.id.location_button_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                calibration = 1; // Set calibration status to true
//
//                // Save calibration status to Firebase
//                saveCalibrationStatusToFirebase();

                Intent intent = new Intent(CalibrationActivity.this, MetricsActivity.class);
                startActivity(intent);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User clicked "No," go back to MetricsActivity
                Intent intent = new Intent(CalibrationActivity.this, MetricsActivity.class);
                startActivity(intent);
            }
        });
    }

//    // Save calibration status to Firebase
//    private void saveCalibrationStatusToFirebase() {
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseUser user = auth.getCurrentUser();
//
//        if (user != null) {
//            String userId = user.getUid();
//
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PatientMetrics/" + userId);
//
//            // Create a default PatientMetrics object
//            PatientMetrics patientMetrics = new PatientMetrics();
//
//            // Update the calibrate value
//            patientMetrics.setCalibrate(calibrate);
//
//            // Save the updated PatientMetrics object to Firebase
//            databaseReference.setValue(patientMetrics);
//        }
//    }
}
