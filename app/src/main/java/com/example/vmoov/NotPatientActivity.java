package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotPatientActivity extends AppCompatActivity implements RecyclerViewInterface {

    private RecyclerView recyclerViewPatients;
    private PatientsAdapter patientsAdapter;
    private List<String> patientNames;
    private List<String> patientIDs;
    private TextView professionalNameTextView; // TextView para mostrar el nombre del profesional

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_patient);

        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(this));

        patientNames = new ArrayList<>();
        patientIDs = new ArrayList<>();
        patientsAdapter = new PatientsAdapter(this, patientNames, this);
        recyclerViewPatients.setAdapter(patientsAdapter);

        professionalNameTextView = findViewById(R.id.user_name_textview); // Referencia al TextView

        // Obtener el usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchProfessionalName(userId); // Obtener el nombre del profesional de salud
            fetchPatientNames(userId); // Iniciar la búsqueda de los pacientes vinculados
        }

        setupButtons();  // Configurar los botones
    }

    private void fetchProfessionalName(String userId) {
        // Buscar en 'users' para obtener el nombre y apellido del profesional de salud
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        String fullName = firstName + " " + lastName;
                        professionalNameTextView.setText(fullName); // Mostrar el nombre en pantalla
                    } else {
                        professionalNameTextView.setText("Nombre no disponible");
                    }
                } else {
                    professionalNameTextView.setText("No se encontró información del profesional");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotPatientActivity", "Error en la base de datos: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPatientNames(String userId) {
        // Obtener los pacientes vinculados bajo el profesional de salud
        DatabaseReference healthProfessionalRef = FirebaseDatabase.getInstance().getReference()
                .child("healthProfessionals").child(userId).child("patients");

        healthProfessionalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                        String patientId = patientSnapshot.getKey(); // Obtener el patientId
                        Log.d("NotPatientActivity", "Obtenido patientId: " + patientId);

                        // Buscar el nombre del paciente en 'users' utilizando el patientId
                        fetchUserName(patientId);
                    }
                } else {
                    Log.d("NotPatientActivity", "No se encontraron pacientes vinculados para el userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotPatientActivity", "Error en la base de datos: " + databaseError.getMessage());
            }
        });
    }

    private void fetchUserName(String patientId) {
        // Buscar en 'users' utilizando el patientId
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(patientId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        String fullName = firstName + " " + lastName;
                        Log.d("NotPatientActivity", "Nombre del paciente: " + fullName);
                        patientNames.add(fullName);
                        patientIDs.add(patientId);  // Añadir también el ID del paciente a la lista
                    } else {
                        Log.d("NotPatientActivity", "Nombre no disponible para patientId: " + patientId);
                        patientNames.add("Nombre no disponible");
                        patientIDs.add(patientId);  // Asegúrate de que el ID siempre se añada
                    }

                    // Notificar al adapter que los datos han cambiado
                    patientsAdapter.notifyDataSetChanged();
                } else {
                    Log.d("NotPatientActivity", "No se encontró el nodo del usuario para patientId: " + patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotPatientActivity", "Error en la base de datos: " + databaseError.getMessage());
            }
        });
    }

    private void setupButtons() {
        // Funcionalidad del botón de agregar nuevo contacto
        ImageButton addContactButton = findViewById(R.id.buttonAddContact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotPatientActivity.this, NewContactActivity.class); // Ir a NewContactActivity
                startActivity(intent);
            }
        });

        // Funcionalidad del botón de cerrar sesión
        ImageButton logOutButton = findViewById(R.id.buttonLogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(NotPatientActivity.this, MainActivity.class); // Ir a MainActivity
                startActivity(intent);
                finish(); // Finalizar esta actividad
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        // Log the size of the patientIDs list and the clicked position
        Log.d("NotPatientActivity", "Patient ID list size: " + patientIDs.size());
        Log.d("NotPatientActivity", "Clicked position: " + position);

        // Check if the list contains the requested position before accessing it
        if (position < patientIDs.size()) {
            // Al hacer clic en un paciente, mostrar la información
            Intent intent = new Intent(NotPatientActivity.this, PatientDisplayActivity.class);
            intent.putExtra("userId", patientIDs.get(position));  // Log the ID being passed
            Log.d("NotPatientActivity", "Opening PatientDisplayActivity with userId: " + patientIDs.get(position));
            startActivity(intent);
        } else {
            Log.e("NotPatientActivity", "Invalid position clicked: " + position);
        }
    }
}