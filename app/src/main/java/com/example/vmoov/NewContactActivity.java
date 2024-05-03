package com.example.vmoov;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewContactActivity extends AppCompatActivity {

    private EditText editText_name;
    private EditText editText_relation;
    private EditText editText_phnumber;
    private EditText editText_email;
    private Button guardarButton;
    private DatabaseReference mDatabase;
    private CheckBox checkBox;
    private CheckBox emergencyContactCheckbox;
    private FirebaseAuth mAuth;
    private int alertFlag = 0;
    private int emergencyContactFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcontact);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editText_name = findViewById(R.id.name_text);
        editText_relation = findViewById(R.id.relation_text);
        editText_phnumber = findViewById(R.id.phnumber_text);
        editText_email = findViewById(R.id.email_text);

        guardarButton = findViewById(R.id.save_button);

        if (guardarButton != null) {
            guardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String NameContact_in = editText_name.getText().toString();
                    String Relationship_in = editText_relation.getText().toString();
                    String PhoneNumber_in = editText_phnumber.getText().toString();
                    String Email_in = editText_email.getText().toString();

                    // Inside the onClick method of guardarButton
                    String userId = mAuth.getCurrentUser().getUid();

                    // Generate a unique ID for the new contact
                    String contactId = mDatabase.child("contacts").child(userId).push().getKey();
                    // Get the state of the first checkbox
                    alertFlag = checkBox.isChecked() ? 1 : 0;
                    // Get the state of the second checkbox
                    emergencyContactFlag = emergencyContactCheckbox.isChecked() ? 1 : 0;

                    // Create a new Contacts object
                    Contacts contact = new Contacts(contactId, NameContact_in, Relationship_in, PhoneNumber_in, Email_in, alertFlag, emergencyContactFlag);

                    // Save the contact data to Firebase
                    mDatabase.child("contacts").child(userId).child(contactId).setValue(contact).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(NewContactActivity.this, "Patient data saved.", Toast.LENGTH_SHORT).show();
                                // Proceed to the main activity or any other activity
                                Intent intent = new Intent(NewContactActivity.this, ContactsActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(NewContactActivity.this, "Failed to save patient data.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

        Button Back = findViewById(R.id.back_button);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewContactActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

        checkBox = findViewById(R.id.checkbox_alert);

        // Add an OnCheckedChangeListener to the checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the userType based on checkbox status
                alertFlag = isChecked ? 1 : 0;
            }
        });

        emergencyContactCheckbox = findViewById(R.id.checkbox_emergencyContact); // Assuming you have this ID in your XML

        emergencyContactCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                emergencyContactFlag = isChecked ? 1 : 0;
            }
        });

    }

}