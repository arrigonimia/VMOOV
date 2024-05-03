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

public class ReportActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewContacts2;
    private ContactAdapter contactAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("contacts").child(userId);

        recyclerViewContacts2 = findViewById(R.id.recyclerViewContacts2);
        recyclerViewContacts2.setLayoutManager(new LinearLayoutManager(this));

        // Create and set the adapter
        contactAdapter = new ContactAdapter(this, new ArrayList<>());
        recyclerViewContacts2.setAdapter(contactAdapter);

        loadContactsFromFirebase();
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