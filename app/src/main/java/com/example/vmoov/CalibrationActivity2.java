package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.graphics.Color;
import android.util.Log;
import static android.content.ContentValues.TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class CalibrationActivity2 extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration2);

        statusText = findViewById(R.id.status_text);

        // Display "Calibrating now..." and change the text to "Done" with a green background after a delay
        calibratingNow();

        // Delay for a few seconds and then navigate back to MainActivity
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendSignalToArduino("1");
                navigateToMainActivity();
            }
        }, 3000); // Delay for 3 seconds
    }

    private void calibratingNow() {
        statusText.setText("Calibrating now...");
        statusText.setBackgroundColor(Color.YELLOW); // Set a yellow background (you can customize it)
        // You can also use a Drawable resource as the background.

        // Delay for a short moment (e.g., 1 second)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // After a delay, change the text to "Done" with a green background
                statusText.setText("Done");
                statusText.setBackgroundColor(Color.GREEN); // Set a green background (you can customize it)
            }
        }, 60000); // Delay for 1 minute
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(CalibrationActivity2.this, MetricsActivity.class);
        startActivity(intent);
    }
    private void sendSignalToArduino(String signal) {
        // Check if the ConnectedThread is initialized
        if (MetricsActivity.connectedThread != null) {
            // Send the signal to the Arduino device
            MetricsActivity.connectedThread.write(signal);
            Log.d(TAG, "Sent signal to Arduino: " + signal);
        } else {
            // Handle the case where the Bluetooth connection is not yet established
            Log.e(TAG, "Bluetooth connection is not yet established");
        }
    }

}
