package com.example.vmoov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class ConnectionActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private static final String SHARED_PREFS = "user_prefs";  // Nombre de SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        // Obtener el userId y uniqueCode desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String userId = preferences.getString("userId", null);  // Recupera el userId
        int uniqueCode = preferences.getInt("uniqueCode", -1);  // Recupera el uniqueCode

        // Referencia al TextView donde se mostrará el código
        TextView codeTextView = findViewById(R.id.generated_code_text);

        // Verificar si el userId y el uniqueCode son válidos
        if (userId == null || userId.isEmpty()) {
            Log.e("ConnectionActivity", "No se encontró un ID de usuario válido en SharedPreferences");
            codeTextView.setText("Error: No se encontró el ID de usuario.");
            return;
        }

        if (uniqueCode == -1) {
            Log.e("ConnectionActivity", "No se encontró un uniqueCode válido en SharedPreferences");
            codeTextView.setText("Error: No se encontró el código único.");
            return;
        }

        // Mostrar el uniqueCode desde SharedPreferences directamente
        codeTextView.setText(String.valueOf(uniqueCode));

        // Configurar el botón "Comenzar" para volver a MetricsActivity
        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para navegar a MetricsActivity
                Intent intent = new Intent(ConnectionActivity.this, MetricsActivity.class);
                startActivity(intent);
                finish(); // Cierra esta actividad para evitar que el usuario regrese a ella
            }
        });

        // Si quieres obtener el uniqueCode desde Firebase para asegurarte de que esté sincronizado
        // también puedes hacerlo de la siguiente manera (opcional)
        /*mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("patients").child(userId).child("uniqueCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int updatedCode = dataSnapshot.getValue(Integer.class);
                    codeTextView.setText(String.valueOf(updatedCode));  // Convierte el entero a cadena
                } else {
                    codeTextView.setText("Error: No se pudo recuperar el código desde Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                codeTextView.setText("Error: " + databaseError.getMessage());
            }
        });*/
    }
}
