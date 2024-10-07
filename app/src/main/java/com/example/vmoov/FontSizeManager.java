package com.example.vmoov;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
public class FontSizeManager {

    private static final String PREFERENCES_FILE = "font_size_preferences";
    private static final String FONT_SCALE_KEY = "font_scale";  // Escala de fuente
    private static final float DEFAULT_SCALE = 1.0f;  // Escala de fuente predeterminada (sin cambio)

    // Guardar el factor de escala en SharedPreferences
    public static void saveFontScale(Context context, float scale) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(FONT_SCALE_KEY, scale);
        editor.apply();
    }

    // Obtener el factor de escala desde SharedPreferences
    public static float getFontScale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return prefs.getFloat(FONT_SCALE_KEY, DEFAULT_SCALE);  // Escala predeterminada = 1.0 (sin cambios)
    }

    // Aplicar el tamaño de fuente con escala proporcional a todos los TextView en un ViewGroup
    public static void applyFontSizeToAllViews(ViewGroup viewGroup, float scale) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                float originalSize = textView.getTextSize();  // Obtener el tamaño actual en píxeles
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalSize * scale);  // Aplicar el factor de escala
            } else if (view instanceof ViewGroup) {
                applyFontSizeToAllViews((ViewGroup) view, scale);  // Aplicar recursivamente
            }
        }
    }
}
