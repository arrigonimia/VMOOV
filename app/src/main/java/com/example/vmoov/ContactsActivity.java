package com.example.vmoov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private Button addcontact_Button;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("contacts").child(userId);

        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        // Create and set the adapter
        contactAdapter = new ContactAdapter(this, new ArrayList<>());
        recyclerViewContacts.setAdapter(contactAdapter);

        addcontact_Button = findViewById(R.id.button_prim);
        addcontact_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactsActivity.this, NewContactActivity.class);
                startActivity(intent);
            }
        });

        // Load contacts from Firebase
        loadContactsFromFirebase();




        Button myButton_metrics = findViewById(R.id.home);
        Button myButton_biofeedback = findViewById(R.id.button_biofeedback);
        Button myButton_sos = findViewById(R.id.sos);


        myButton_metrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ContactsActivity.this, MetricsActivity.class);
                startActivity(intent);
            }
        });

        myButton_biofeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ContactsActivity.this, BiofeedbackActivity.class);
                startActivity(intent);
            }
        });

        myButton_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ContactsActivity.this, SosActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadContactsFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Contacts> contactList = new ArrayList<>();
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    Contacts contact = contactSnapshot.getValue(Contacts.class);
                    contactList.add(contact);
                }

                contactAdapter.setContacts(contactList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
