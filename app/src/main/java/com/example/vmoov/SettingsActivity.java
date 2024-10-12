package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.ViewGroup;
import android.content.Intent;

public class SettingsActivity extends AppCompatActivity {

    // Escala de fuente actual
    private float currentFontScale;
    private final float maxFontScale = 2.0f;  // Escala máxima (200%)
    private final float minFontScale = 0.5f;  // Escala mínima (50%)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);  // Cargar el layout de SettingsActivity

        // Referencias a los botones de aumentar y disminuir fuente
        ImageButton increaseFontButton = findViewById(R.id.increase_font_button);
        ImageButton decreaseFontButton = findViewById(R.id.decrease_font_button);

        // Referencia al botón "Atrás"
        Button backButton = findViewById(R.id.back_button);

        // Listener para el botón "Atrás" que vuelve a MetricsActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MetricsActivity.class);
            startActivity(intent);
            finish();  // Finaliza SettingsActivity para que no se vuelva al presionar atrás
        });
    }

}
