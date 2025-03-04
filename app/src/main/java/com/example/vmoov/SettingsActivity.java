package com.example.vmoov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends BaseActivity {  // 🔹 Hereda de BaseActivity

    private static final String PREFS_NAME = "AppPreferences";
    private static final String FONT_SCALE_KEY = "fontScale";

    private final float maxFontScale = 1.5f;  // Tamaño máximo
    private final float minFontScale = 0.8f;  // Tamaño mínimo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Botón de editar información
        ImageButton editInfoButton = findViewById(R.id.edit_info_button);
        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditInfoActivity.class);
            startActivity(intent);
        });

        // Botón "Atrás" (vuelve a MenuActivity)
        CardView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón para aumentar el tamaño de fuente
        ImageButton increaseFontButton = findViewById(R.id.increase_font_button);
        increaseFontButton.setOnClickListener(v -> adjustFontScale(true));

        // Botón para disminuir el tamaño de fuente
        ImageButton decreaseFontButton = findViewById(R.id.decrease_font_button);
        decreaseFontButton.setOnClickListener(v -> adjustFontScale(false));
    }

    private void adjustFontScale(boolean increase) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float fontScale = prefs.getFloat(FONT_SCALE_KEY, 1.0f);

        if (increase) {
            if (fontScale < maxFontScale) {
                fontScale += 0.1f;
            } else {
                Toast.makeText(this, "Tamaño máximo alcanzado", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (fontScale > minFontScale) {
                fontScale -= 0.1f;
            } else {
                Toast.makeText(this, "Tamaño mínimo alcanzado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Guardar la nueva escala de fuente
        prefs.edit().putFloat(FONT_SCALE_KEY, fontScale).apply();

        // Reiniciar la actividad para aplicar cambios
        recreate();
    }
}
