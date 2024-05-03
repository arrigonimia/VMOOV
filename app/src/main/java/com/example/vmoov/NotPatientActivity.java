package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Button myButton_logout = findViewById(R.id.logout_button);

        myButton_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotPatientActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = databaseReference.child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);

                        // Now, you have the first and last name, set them to the TextView
                        String fullName = firstName + " " + lastName;
                        TextView userNameTextView = findViewById(R.id.user_name_textview);
                        userNameTextView.setText(fullName);

                        // Use the user ID to fetch patient names and populate the RecyclerView
                        fetchPatientNames(userId);
                    }
                    else {
                        Log.d("NotPatientActivity", "No data found in contacts node");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("NotPatientActivity", "Database error: " + databaseError.getMessage());
                }
            });

        }
    }

    private void fetchPatientNames(String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Log.d("NotPatientActivity", "Checking userSnapshot: " + userSnapshot.getKey());

                        // Iterate over the child nodes under the current userSnapshot
                        for (DataSnapshot contactSnapshot : userSnapshot.getChildren()) {
                            // Get the contact's email directly
                            String contactEmail = contactSnapshot.child("contactEmail").getValue(String.class);

                            // Check if the contact's email matches the currently logged-in user's email
                            if (contactEmail != null && contactEmail.equals(currentUser.getEmail())) {
                                Log.d("NotPatientActivity", "Match found for contactEmail: " + contactEmail);

                                // Pass the userId corresponding to the contact to fetchUserName
                                String contactUserId = userSnapshot.getKey();
                                patientIDs.add(contactUserId);
                                fetchUserName(contactUserId);
                            }
                        }
                    }
                } else {
                    Log.d("NotPatientActivity", "No data found in contacts node for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotPatientActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }


    private void fetchUserName(String contactUserId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(contactUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    // Now, you have the first and last name, add it to the list
                    String fullName = firstName + " " + lastName;
                    Log.d("NotPatientActivity", "Fetched user name: " + fullName);
                    patientNames.add(fullName);
                    patientsAdapter.notifyDataSetChanged();
                } else {
                    Log.d("NotPatientActivity", "No data found in users node for userId: " + contactUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotPatientActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(NotPatientActivity.this, PatientDisplayActivity.class);
        intent.putExtra("userId", patientIDs.get(position));
        startActivity(intent);

    }
}
