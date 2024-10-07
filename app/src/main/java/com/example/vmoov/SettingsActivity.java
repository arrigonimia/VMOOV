package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    // Escala de fuente actual
    private float currentFontScale;
    private float maxFontScale = 2.0f;  // Escala máxima (aumenta el doble)
    private float minFontScale = 0.5f;  // Escala mínima (reduce a la mitad)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);  // Carga el layout de SettingsActivity

        // Obtener la escala de fuente actual desde SharedPreferences
        currentFontScale = FontSizeManager.getFontScale(this);

        // Referencias a los botones de aumentar y disminuir fuente
        ImageButton increaseFontButton = findViewById(R.id.increase_font_button);
        ImageButton decreaseFontButton = findViewById(R.id.decrease_font_button);

        // Listener para aumentar la escala de fuente
        increaseFontButton.setOnClickListener(v -> {
            if (currentFontScale < maxFontScale) {
                currentFontScale += 0.1f;  // Incrementar en 10%
                FontSizeManager.saveFontScale(this, currentFontScale);  // Guardar la nueva escala
                applyFontScaleToAllLayouts();  // Aplicar la escala a todos los TextView
            }
        });

        // Listener para disminuir la escala de fuente
        decreaseFontButton.setOnClickListener(v -> {
            if (currentFontScale > minFontScale) {
                currentFontScale -= 0.1f;  // Reducir en 10%
                FontSizeManager.saveFontScale(this, currentFontScale);  // Guardar la nueva escala
                applyFontScaleToAllLayouts();  // Aplicar la escala a todos los TextView
            }
        });
    }

    // Aplicar el factor de escala de fuente a todos los layouts
    private void applyFontScaleToAllLayouts() {
        ViewGroup rootView = findViewById(android.R.id.content);  // Vista raíz del layout actual
        FontSizeManager.applyFontSizeToAllViews(rootView, currentFontScale);  // Aplicar escala a todos los TextView

        // Recrear la actividad para reflejar los cambios de tamaño de fuente inmediatamente
        recreate();
    }
}
