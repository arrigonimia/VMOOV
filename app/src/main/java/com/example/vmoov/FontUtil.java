package com.example.vmoov;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontUtil {

    private static final String FONT_PREFS = "font_prefs";
    private static final String FONT_SCALE_KEY = "font_scale";

    // Tamaño de fuente por defecto
    private static final float DEFAULT_FONT_SCALE = 1.0f;

    // Obtener el tamaño de la escala de fuente desde SharedPreferences
    public static float getFontScale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FONT_PREFS, Context.MODE_PRIVATE);
        return prefs.getFloat(FONT_SCALE_KEY, DEFAULT_FONT_SCALE);
    }

    // Guardar el tamaño de la escala de fuente en SharedPreferences
    public static void saveFontScale(Context context, float fontScale) {
        SharedPreferences prefs = context.getSharedPreferences(FONT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(FONT_SCALE_KEY, fontScale);
        editor.apply();
    }

    // Aplicar el tamaño de fuente escalado a todas las vistas que contienen texto
    public static void applyFontSizeToAllViews(ViewGroup viewGroup, float fontScale) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textView.getTextSize() / viewGroup.getResources().getDisplayMetrics().scaledDensity * fontScale);
            } else if (view instanceof ViewGroup) {
                applyFontSizeToAllViews((ViewGroup) view, fontScale); // Aplicar recursivamente
            }
        }
    }
}
