package com.example.vmoov;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthProfessionalInfoActivity extends BaseActivity {

    private LinearLayout centersContainer;
    private CardView addCenterButton, saveButton, backButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String professionalId;
    private String[] centerNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_professional_info);

        // Cargar nombres de centros desde strings.xml
        centerNames = getResources().getStringArray(R.array.center_names);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        professionalId = mAuth.getCurrentUser().getUid();

        // Vincular elementos del layout
        centersContainer = findViewById(R.id.centersContainer);
        addCenterButton = findViewById(R.id.addCenterButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // Configurar botón "Volver"
        backButton.setOnClickListener(v -> navigateToNotPatient());

        // Agregar un centro inicial
        addCenter();

        // Listeners
        addCenterButton.setOnClickListener(v -> addCenter());
        saveButton.setOnClickListener(v -> saveData());
    }

    private void addCenter() {
        View centerView = LayoutInflater.from(this).inflate(R.layout.item_center, centersContainer, false);

        // Configurar Spinner con una opción inicial no seleccionable
        Spinner spinnerCenter = centerView.findViewById(R.id.spinnerCenterName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, centerNames);
        spinnerCenter.setAdapter(adapter);
        spinnerCenter.setSelection(0); // Establecer la opción inicial como seleccionada

        // Configurar botones de selección de días
        Map<TextView, Boolean> daySelection = new HashMap<>();
        int[] dayIds = {R.id.btnMon, R.id.btnTue, R.id.btnWed, R.id.btnThu, R.id.btnFri, R.id.btnSat, R.id.btnSun};
        for (int id : dayIds) {
            TextView day = centerView.findViewById(id);
            daySelection.put(day, false);
            day.setOnClickListener(v -> toggleDaySelection(day, daySelection));
        }

        // Configurar selección de horarios
        TextView startTime = centerView.findViewById(R.id.startTime);
        TextView endTime = centerView.findViewById(R.id.endTime);
        startTime.setOnClickListener(v -> selectTime(startTime));
        endTime.setOnClickListener(v -> selectTime(endTime));

        // Botón para eliminar centro
        CardView removeButton = centerView.findViewById(R.id.removeCenterButton);
        removeButton.setOnClickListener(v -> centersContainer.removeView(centerView));

        // Agregar la vista del centro al contenedor
        centersContainer.addView(centerView);
    }

    private void toggleDaySelection(TextView day, Map<TextView, Boolean> daySelection) {
        boolean selected = !daySelection.get(day);
        daySelection.put(day, selected);

        @ColorInt int selectedColor = getResources().getColor(R.color.violet, getTheme());
        @ColorInt int unselectedColor = getResources().getColor(R.color.white, getTheme());
        @ColorInt int textColorSelected = getResources().getColor(R.color.white, getTheme());
        @ColorInt int textColorUnselected = getResources().getColor(R.color.violet, getTheme());

        day.setBackgroundColor(selected ? selectedColor : unselectedColor);
        day.setTextColor(selected ? textColorSelected : textColorUnselected);
    }

    private void selectTime(TextView timeText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> timeText.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void saveData() {
        int count = centersContainer.getChildCount();
        if (count == 0) {
            Toast.makeText(this, getString(R.string.error_add_center), Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference professionalRef = mDatabase.child("healthProfessionals").child(professionalId).child("centers");
        professionalRef.removeValue();

        for (int i = 0; i < count; i++) {
            View centerView = centersContainer.getChildAt(i);
            Spinner spinner = centerView.findViewById(R.id.spinnerCenterName);
            String centerName = spinner.getSelectedItem().toString();

            // Validar que no se seleccione la pista
            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, getString(R.string.select_valid_center), Toast.LENGTH_SHORT).show();
                return; // No guardar si hay una selección inválida
            }

            String startTime = ((TextView) centerView.findViewById(R.id.startTime)).getText().toString();
            String endTime = ((TextView) centerView.findViewById(R.id.endTime)).getText().toString();

            // Obtener los días seleccionados
            List<String> selectedDays = new ArrayList<>();
            int[] dayIds = {R.id.btnMon, R.id.btnTue, R.id.btnWed, R.id.btnThu, R.id.btnFri, R.id.btnSat, R.id.btnSun};
            for (int id : dayIds) {
                TextView day = centerView.findViewById(id);
                if (day.getCurrentTextColor() == getResources().getColor(R.color.white, getTheme())) {
                    selectedDays.add(day.getText().toString());
                }
            }

            // Guardar en Firebase
            professionalRef.push().setValue(Map.of(
                    "name", centerName,
                    "days", String.join("-", selectedDays),
                    "hours", startTime + " - " + endTime
            ));
        }

        Toast.makeText(this, getString(R.string.success_save), Toast.LENGTH_SHORT).show();

        // 🔹 Ahora SIEMPRE lleva a NotPatientActivity al guardar
        navigateToNotPatient();
    }

    private void navigateToNotPatient() {
        Intent intent = new Intent(this, NotPatientActivity.class);
        startActivity(intent);
        finish();
    }
}
