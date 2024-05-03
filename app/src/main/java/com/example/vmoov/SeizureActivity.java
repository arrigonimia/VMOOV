package com.example.vmoov;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SeizureActivity extends AppCompatActivity {

    private int secondsPassed = 0;
    private TextView counterTextView;
    private Button startButton;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable counterRunnable = new Runnable() {
        @Override
        public void run() {
            secondsPassed++;
            updateCounterTextView();
            handler.postDelayed(this, 1000); // Update every second
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seizure); // Replace with your layout file name

        counterTextView = findViewById(R.id.counter);
        startButton = findViewById(R.id.okay_button);

        // Set initial state
        updateCounterTextView();

        // Start the seconds counter when the activity is created
        handler.post(counterRunnable);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop the counter when the button is clicked
                handler.removeCallbacks(counterRunnable);
                handleSeizureFinish();
                // Start MetricsActivity and pass the seconds as an extra
                Intent metricsIntent = new Intent(SeizureActivity.this, MetricsActivity.class);
                metricsIntent.putExtra("seconds_passed", secondsPassed);
                startActivity(metricsIntent);
            }
        });
    }

    private void updateCounterTextView() {
        counterTextView.setText(String.valueOf(secondsPassed));
    }

    private void handleSeizureFinish() {
        Float calibrate = 0f;
        Integer seizureCount = 0; // Set seizureCount to 0
        String seizureTimes = getSeizureTimes(); // Set seizureTimes to today's date
        Float seizureDuration = (float) secondsPassed;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Generate a unique ID for the new metrics entry
            String metricsId = FirebaseDatabase.getInstance().getReference("patientmetrics").child(userId).push().getKey();

            // Create a Metrics object
            PatientMetrics metrics = new PatientMetrics(calibrate, seizureCount, seizureTimes, seizureDuration);

            // Save the metrics data to Firebase
            FirebaseDatabase.getInstance().getReference("patientmetrics").child(userId).child(metricsId).setValue(metrics)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SeizureActivity.this, "Metrics data saved.", Toast.LENGTH_SHORT).show();
                            // Proceed with any other logic you need
                        } else {
                            Toast.makeText(SeizureActivity.this, "Failed to save metrics data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "Current user is null");
        }
    }
    private String getSeizureTimes() {
        // Declare a default value for seizureTimes in case retrieval fails
        String defaultValue = "";

        try {
            // Get the current date and time
            Date currentDate = new Date();

            // Format the date as a string using a specific format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = dateFormat.format(currentDate);

            if (formattedDate != null) {
                Log.d(TAG, "SeizureTimes value retrieved: " + formattedDate);
                return formattedDate;
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving seizureTimes value", e);
            return defaultValue;
        }
    }
}


