package com.example.vmoov;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.core.content.FileProvider;
import android.os.Environment;
import java.io.File;
public class SosActivity extends AppCompatActivity {

    final static int REQUEST_CODE = 1232;
    private static final int MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 456;
    private Button callButton;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform the user that your app will not show notifications.
                }
            });
    private Button locationButton;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location location;
    private String full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Intent intent = getIntent();
        String value1 = intent.getStringExtra("key_value_1");
        String value2 = intent.getStringExtra("key_value_2");
        String value3 = intent.getStringExtra("key_value_3");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        requestStoragePermission();

        Button myButton_contact = findViewById(R.id.button_contacts);
        Button myButton_biofeedback = findViewById(R.id.button_biofeedback);
        Button myButton_metrics = findViewById(R.id.home);
        Button myButton_location = findViewById(R.id.location_button);
        //Button myButton_report = findViewById(R.id.report_button);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = databaseReference.child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);

                        full_name = firstName + " " + lastName;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // error handling
                }
            });

            String patientId = currentUser.getUid();

            DatabaseReference userPatientRef = databaseReference.child("patients").child(patientId);

            userPatientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        String blood_type = dataSnapshot.child("blood_type").getValue(String.class);
                        String epiTherapy = dataSnapshot.child("epiTherapy").getValue(String.class);
                        String pathologies = dataSnapshot.child("pathologies").getValue(String.class);

                        String content1 = "Blood Type: " + blood_type;
                        String content2 = "Pharmacological Treatment: " + epiTherapy;
                        String content3 = "Other pathologies: " + pathologies;

                        String[] lines = {
                                "EPICare",
                                " ",
                                " ",
                                "Patient: " + full_name,
                                content1,
                                content2,
                                content3,
                                " ",
                                " ",
                                "REPORT",
                                " ",
                                "Date of the last seizure: " + value3,
                                "Average number of seizures per month: " + value1,
                                "Average seizure duration: " + value2
                        };

                        PdfGenerator.createPdf(lines);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // error handling
                }
            });

        } else {
            // User not authenticated, handle error ...
        }

        Button generatePdfButton = findViewById(R.id.report_button);
        generatePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  sendEmail();

            }
        });

        myButton_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SosActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

        myButton_biofeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SosActivity.this, BiofeedbackActivity.class);
                startActivity(intent);
            }
        });

        myButton_metrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SosActivity.this, MetricsActivity.class);
                startActivity(intent);
            }
        });

        callButton = findViewById(R.id.call_button);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialEmergencyNumber();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        myButton_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SosActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SosActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    obtenerUbicacion();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, enviar el SMS
                Toast.makeText(SosActivity.this, "Permiso para enviar SMS.", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(SosActivity.this, "Permiso para enviar SMS denegado.", Toast.LENGTH_SHORT).show();
            }
        }

        // Verificar si el usuario otorgó el permiso
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario otorgó el permiso, puedes realizar la operación que lo requiere
                String[] lines = {
                        "EPICare",
                        " ",
                        " ",
                };

                PdfGenerator.createPdf(lines);

                sendEmail();

            } else {
                // El usuario no otorgó el permiso, puedes mostrar un mensaje o realizar otra acción
                //Toast.makeText(SosActivity.this, "Permiso para enviar gmail denegado.", Toast.LENGTH_SHORT).show();
                String result = "permiso denegado";
            }
        }
    }

    private void dialEmergencyNumber() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get the authenticated user's ID
            String authenticatedUserId = currentUser.getUid();

            // Assuming you have a Firebase database reference
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Reference to the "contacts" node for the authenticated user
            DatabaseReference userContactsRef = databaseReference.child("contacts").child(authenticatedUserId);

            // Query to fetch the emergency contact with emergencyContactFlag = 1
            Query emergencyContactQuery = userContactsRef.orderByChild("emergencyContactFlag").equalTo(1);

            emergencyContactQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean phoneNumberFound = false;
                    for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                        // Get the phone number of the emergency contact
                        String phoneNumber = contactSnapshot.child("phoneNumber").getValue(String.class);

                        if (phoneNumber != null) {
                            phoneNumberFound = true;
                            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(dialIntent);
                            break; // Exit the loop once a phone number is found
                        }
                    }

                    if (!phoneNumberFound) {
                        Toast.makeText(SosActivity.this, "Emergency phone number not available", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error ...
                }
            });
        } else {
            // User not authenticated, handle error ...
        }
    }



    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitud = location.getLatitude();
                            double longitud = location.getLongitude();
                            SendGoogleMapstoContact(latitud, longitud);
                        }
                    }
                });
    }

    public void SendGoogleMapstoContact(double latitude, double longitude) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get the authenticated user's ID
            String authenticatedUserId = currentUser.getUid();

            // Assuming you have a Firebase database reference
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Reference to the "contacts" node for the authenticated user
            DatabaseReference userContactsRef = databaseReference.child("contacts").child(authenticatedUserId);

            // Query to fetch the emergency contact with emergencyContactFlag = 1
            Query emergencyContactQuery = userContactsRef.orderByChild("emergencyContactFlag").equalTo(1);

            emergencyContactQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                        // Get the phone number of the emergency contact
                        String phoneNumber = contactSnapshot.child("phoneNumber").getValue(String.class);

                        if (phoneNumber != null) {
                            // Create a Uri that represents the location in geo format
                            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);

                            // Create a Google Maps link with the location
                            String googleMapsLink = "https://www.google.com/maps?q=" + latitude + "," + longitude;

                            // Open WhatsApp and create a message with the Google Maps link
                            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                            whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode("Mira mi ubicación en Google Maps: " + googleMapsLink)));
                            startActivity(whatsappIntent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error ...
                }
            });
        } else {
            // User not authenticated, handle error ...
        }

    }

    private void sendEmail() {
        // Obtener la ruta del archivo PDF que se creó
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "EPICareReport.pdf");

        // Crear una Uri segura utilizando FileProvider
        Uri pdfUri = FileProvider.getUriForFile(this, "com.example.vmoov.fileprovider", pdfFile);

        // Crear un intent para enviar el correo electrónico a través de Gmail
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");

        // Configurar la dirección de correo electrónico del destinatario
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"usuario@gmail.com"}); // Reemplaza con la dirección del usuario específico

        // Configurar el asunto y el texto del correo (opcional)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte EPICare");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola, le adjunto el reporte de EPICare. Saludos.");

        // Adjuntar el archivo PDF al intent
        emailIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);

        // Conceder permisos de lectura al receptor de la intención
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Establecer el paquete de la aplicación de Gmail para garantizar que se abra con Gmail
        emailIntent.setPackage("com.google.android.gm");

        // Iniciar la actividad para enviar correo electrónico
        startActivity(emailIntent);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Solicitar permisos al usuario
            ActivityCompat.requestPermissions(SosActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

}
