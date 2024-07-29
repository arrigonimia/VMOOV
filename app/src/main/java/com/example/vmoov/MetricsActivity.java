package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MetricsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        // Definir botones

        Button myButton_logout = findViewById(R.id.logout_button);
        Button buttonConnect = findViewById(R.id.buttonConnect);

        // Asignar acción solo al botón "Conectar Casco"
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

        // Otros botones no hacen nada
        myButton_logout.setOnClickListener(null);
    }}
