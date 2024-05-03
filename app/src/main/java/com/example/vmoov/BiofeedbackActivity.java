package com.example.vmoov;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BiofeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biofeedback);

        Button myButton_metrics = findViewById(R.id.home);
        Button myButton_contacts = findViewById(R.id.button_contacts);
        Button myButton_sos = findViewById(R.id.sos);


        myButton_metrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(BiofeedbackActivity.this, MetricsActivity.class);
                startActivity(intent);
            }
        });

        myButton_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(BiofeedbackActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

        myButton_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(BiofeedbackActivity.this, SosActivity.class);
                startActivity(intent);
            }
        });

    }
}