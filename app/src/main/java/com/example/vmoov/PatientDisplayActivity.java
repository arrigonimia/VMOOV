package com.example.vmoov;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.view.View;
import android.content.Intent;


public class PatientDisplayActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private RecyclerView recyclerViewMetrics;
    private MetricsAdapter metricsAdapter;
    private List<Metric> metricsList;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_display);

        backButton = findViewById(R.id.back_button);

        // Set onClickListener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to NotPatientActivity
                Intent intent = new Intent(PatientDisplayActivity.this, NotPatientActivity.class);
                startActivity(intent);
                finish();  // Close the current activity
            }
        });

        // Obtener el userId desde el Intent
        String userId = getIntent().getStringExtra("userId");

        // Inicializar el TextView
        userNameTextView = findViewById(R.id.user_name);
        recyclerViewMetrics = findViewById(R.id.recyclerViewMetrics);

        // Configurar RecyclerView
        recyclerViewMetrics.setLayoutManager(new LinearLayoutManager(this));
        metricsList = new ArrayList<>();
        metricsAdapter = new MetricsAdapter(metricsList);
        recyclerViewMetrics.setAdapter(metricsAdapter);

        // Obtener referencia a Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = databaseReference.child("users").child(userId);
        DatabaseReference metricsRef = databaseReference.child("patientmetrics").child(userId).child("gameplayData");

        // Cargar nombre y apellido del paciente
        loadPatientName(userRef);

        // Cargar las métricas de las partidas
        loadPatientMetrics(metricsRef);
    }



    private void loadPatientName(DatabaseReference userRef) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        String fullName = firstName + " " + lastName;
                        userNameTextView.setText(fullName);
                    } else {
                        Log.e("PatientDisplayActivity", "First name or last name is null");
                    }
                } else {
                    Log.e("PatientDisplayActivity", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PatientDisplayActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void loadPatientMetrics(DatabaseReference metricsRef) {
        metricsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    String gameDate = gameSnapshot.getKey(); // Get the date or key of the game
                    int trueCount = 0;
                    double gameDuration = 0;
                    int stepCount = 0;

                    for (DataSnapshot stepSnapshot : gameSnapshot.child("steps").getChildren()) {
                        Boolean result = stepSnapshot.child("result").getValue(Boolean.class);
                        Double time = stepSnapshot.child("time").getValue(Double.class);

                        if (result != null && result) {
                            trueCount++;
                        }

                        if (time != null) {
                            gameDuration += time;
                            stepCount++;
                        }
                    }

                    double averageTime = (stepCount > 0) ? gameDuration / stepCount : 0;
                    metricsList.add(new Metric(gameDate, trueCount, averageTime, gameDuration, stepCount));
                }

                metricsAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PatientDisplayActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

}
