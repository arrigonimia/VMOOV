package com.example.vmoov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationSuccessActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private static final String SHARED_PREFS = "user_prefs";  // Nombre de SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_success);

        // Obtener el userId desde el Intent
        String userId = getIntent().getStringExtra("userId");

        // Verificar si el userId es válido
        if (userId == null || userId.isEmpty()) {
            // Manejar el error si no se encuentra el userId
            return;
        }

        // Guardar el userId en SharedPreferences
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", userId);  // Guarda el userId
        editor.apply();

        // Inicializar la referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referencia al TextView donde se mostrará el código
        TextView codeTextView = findViewById(R.id.generated_code_text);

        // Obtener el código único desde la base de datos
        mDatabase.child("patients").child(userId).child("uniqueCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int generatedCode = dataSnapshot.getValue(Integer.class);

                    // Mostrar el código en el TextView
                    codeTextView.setText("" + generatedCode);

                    // Guardar el uniqueCode en SharedPreferences
                    editor.putInt("uniqueCode", generatedCode);  // Guarda el uniqueCode
                    editor.apply();
                } else {
                    codeTextView.setText("Error: No se pudo recuperar el código.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                codeTextView.setText("Error: " + databaseError.getMessage());
            }
        });

        // Configurar el botón para ir a MetricsActivity
        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationSuccessActivity.this, MetricsActivity.class);
                startActivity(intent);
                finish(); // Cierra esta actividad para que no regrese al presionar "atrás"
            }
        });
    }
}
