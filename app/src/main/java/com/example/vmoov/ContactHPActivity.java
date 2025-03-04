package com.example.vmoov;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ContactHPActivity extends BaseActivity {

    private TextView nameTextView, emailTextView, phoneTextView;
    private TableLayout scheduleTable;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private CardView backButton;

    private static final String TAG = "ContactHPActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_hp);

        // Vincular elementos del layout
        nameTextView = findViewById(R.id.textViewName);
        emailTextView = findViewById(R.id.textViewEmail);
        phoneTextView = findViewById(R.id.textViewPhone);
        scheduleTable = findViewById(R.id.scheduleTable);
        backButton = findViewById(R.id.back_card);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Configurar botón de regreso
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ContactHPActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        if (currentUserId == null) {
            showToast("Usuario no autenticado.");
            return;
        }

        findHealthProfessional(currentUserId);
    }

    private void findHealthProfessional(String patientUserId) {
        mDatabase.child("healthProfessionals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot professionalsSnapshot = task.getResult();
                boolean found = false;

                for (DataSnapshot professional : professionalsSnapshot.getChildren()) {
                    DataSnapshot patientsSnapshot = professional.child("patients");

                    if (patientsSnapshot.hasChild(patientUserId)) {
                        String professionalUserId = professional.getKey();
                        fetchProfessionalDetails(professionalUserId);
                        fetchProfessionalSchedule(professionalUserId);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    showToast("No se encontró un profesional de salud asociado.");
                }
            } else {
                showToast("Error al buscar el profesional de salud.");
            }
        });
    }

    private void fetchProfessionalDetails(String professionalUserId) {
        mDatabase.child("users").child(professionalUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot userSnapshot = task.getResult();

                String firstName = userSnapshot.child("firstName").getValue(String.class);
                String lastName = userSnapshot.child("lastName").getValue(String.class);
                String email = userSnapshot.child("email").getValue(String.class);
                String phone = userSnapshot.child("phone").getValue(String.class);

                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                nameTextView.setText(!fullName.trim().isEmpty() ? fullName : "Nombre no disponible");
                emailTextView.setText(email != null && !email.isEmpty() ? email : "Email no disponible");
                phoneTextView.setText(phone != null && !phone.isEmpty() ? phone : "Teléfono no disponible");
            }
        });
    }

    private void fetchProfessionalSchedule(String professionalUserId) {
        mDatabase.child("healthProfessionals").child(professionalUserId).child("centers").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DataSnapshot centersSnapshot = task.getResult();
                        scheduleTable.removeAllViews();

                        if (!centersSnapshot.exists()) {
                            scheduleTable.setVisibility(View.GONE);
                            return;
                        }

                        scheduleTable.setVisibility(View.VISIBLE);

                        for (DataSnapshot center : centersSnapshot.getChildren()) {
                            String centerName = center.child("name").getValue(String.class);
                            String days = center.child("days").getValue(String.class);
                            String hours = center.child("hours").getValue(String.class);

                            if (centerName != null && days != null && hours != null) {
                                addScheduleRow(centerName, days, hours);
                            }
                        }
                    }
                });
    }

    private void addScheduleRow(String center, String days, String hours) {
        // Fila para el nombre del centro
        TableRow centerRow = new TableRow(this);
        TextView centerText = createStyledTextView(center, 1, R.color.blue, true);
        centerText.setTextAppearance(R.style.cuerpobold);
        centerText.setTypeface(null, android.graphics.Typeface.BOLD);
        centerRow.addView(centerText);
        scheduleTable.addView(centerRow);

        // Formatear días y horarios en una sola columna
        String formattedDays = "Días: " + formatDays(days);
        String formattedHours = "Horario: de " + hours.replace("-", " a ");

// Fila de días
        TableRow daysRow = new TableRow(this);
        TextView daysText = createStyledTextView(formattedDays, 1, R.color.violet, false);
        daysText.setTextAppearance(R.style.cuerpo); // Aplicar estilo cuerpo
        daysText.setTextColor(getResources().getColor(R.color.violet)); // Forzar violeta
        daysRow.addView(daysText);
        scheduleTable.addView(daysRow);

// Fila de horarios
        TableRow hoursRow = new TableRow(this);
        TextView hoursText = createStyledTextView(formattedHours, 1, R.color.violet, false);
        hoursText.setTextAppearance(R.style.cuerpo); // Aplicar estilo cuerpo
        hoursText.setTextColor(getResources().getColor(R.color.violet)); // Forzar violeta
        hoursRow.addView(hoursText);
        scheduleTable.addView(hoursRow);

    }


    private String formatDays(String days) {
        return days.replace("Lun", "Lunes")
                .replace("Mar", "Martes")
                .replace("Mié", "Miércoles")
                .replace("Jue", "Jueves")
                .replace("Vie", "Viernes")
                .replace("Sáb", "Sábado")
                .replace("Dom", "Domingo")
                .replace("-", ", "); // Reemplaza separadores
    }

    private TextView createStyledTextView(String text, float weight, int color, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(0, 6, 0, 6);
        textView.setSingleLine(false);
        textView.setEllipsize(null);
        textView.setMinLines(1);
        textView.setMaxLines(3);
        textView.setTextColor(getResources().getColor(color));
        textView.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.verdana)); // Fuente personalizada
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
        return textView;
    }

    private void showToast(String message) {
        Toast.makeText(ContactHPActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
