package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cerrar la sesión del usuario
        FirebaseAuth.getInstance().signOut();

        // Redirigir al usuario a la pantalla de inicio de sesión
        Intent intent = new Intent(LogoutActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finalizar la actividad actual para que no quede en el back stack
    }
}
