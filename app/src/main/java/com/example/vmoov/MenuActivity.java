package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
public class MenuActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pruebas); // Asegúrate de que este es el nombre de tu archivo XML.

        // Asocia las tarjetas con sus IDs
        CardView cardLastSession = findViewById(R.id.card_last_session);
        CardView cardSummary = findViewById(R.id.card_summary);
        CardView cardStartGame = findViewById(R.id.card_start_game);
        CardView cardSettings = findViewById(R.id.card_settings);
        CardView cardLogout = findViewById(R.id.card_logout);
        CardView cardContact = findViewById(R.id.card_contact_hp);

        // Configura los clics para cada tarjeta
        cardLastSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, LastSessionActivity.class);
                startActivity(intent);
            }
        });

        cardSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, SummaryActivity.class);
                startActivity(intent);
            }
        });

        cardStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción de cerrar sesión
                logout();
            }
        });

        cardContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ContactHPActivity.class);
                startActivity(intent);
            }
        });
    }

    // Método para cerrar sesión
    private void logout() {
        // Aquí puedes limpiar datos de usuario, cerrar sesión en Firebase, etc.
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
