package com.example.vmoov;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.location.LocationServices;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;

public class MetricsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location location;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform the user that your app will not show notifications.
                }
            });

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private DatabaseReference databaseReference;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private String averageMonth;
    private String averageTime;
    private String latestDate;
    private List<String> xValues = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        imageView = findViewById(R.id.imageView3);
        askNotificationPermission();

        // Fetch current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate);

        // Calculate start date for three months ago
        calendar.add(Calendar.MONTH, -3);
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set the day to the first day of the month
        Date startDate = calendar.getTime();
        String startDateString = dateFormat.format(startDate);

        // Reference to "patientmetrics" node
        DatabaseReference metricsRef = databaseReference.child("patientmetrics").child(currentUser.getUid());

        // Map to store counts for each month
        Map<Integer, Integer> monthCounts = new HashMap<>();

        // Fetch data for the past three months
        metricsRef.orderByChild("seizureTimes")
                .startAt(startDateString)
                .endAt(currentDateString)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Use a Map to store counts for each month
                        Map<Integer, Integer> monthCounts = new HashMap<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Check if the snapshot has the required fields
                            if (snapshot.hasChild("seizureTimes")) {
                                PatientMetrics metric = snapshot.getValue(PatientMetrics.class);
                                if (metric != null) {
                                    try {
                                        Date seizureTime = dateFormat.parse(metric.getSeizureTimes());

                                        // Check if the seizure month is within the range of the past three months
                                        calendar.setTime(seizureTime);
                                        int seizureMonth = calendar.get(Calendar.MONTH);

                                        // Update counts in the map
                                        monthCounts.put(seizureMonth, monthCounts.getOrDefault(seizureMonth, 0) + 1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        // Handle parsing error
                                    }
                                }
                            }
                        }

                        // Log the month counts
                        for (Map.Entry<Integer, Integer> entry : monthCounts.entrySet()) {
                            Log.d("SeizureData", "Month " + entry.getKey() + ": " + entry.getValue());
                        }

                        // Convert map entries to a list and sort them
                        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(monthCounts.entrySet());
                        Collections.sort(sortedEntries, (e1, e2) -> e1.getKey().compareTo(e2.getKey()));

                        // Update bar chart entries based on the sortedEntries list
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
                        }

                        BarChart barChart = findViewById(R.id.barChart);
                        BarDataSet dataSet = new BarDataSet(entries, "");
                        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

                        BarData barData = new BarData(dataSet);
                        barChart.setData(barData);
                        barChart.invalidate();

                        YAxis yAxis = barChart.getAxisLeft();
                        yAxis.setAxisMinimum(0);
                        yAxis.setAxisMaximum(100);

                        barChart.getDescription().setText("Seizures of the past three months");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error Handling
                    }
                });


        String savedImageUri = getSharedPreferences("MyPreferences", MODE_PRIVATE)
                .getString("userImageUri", null);

        // Set the saved image URI to the ImageView
        if (savedImageUri != null) {
            Picasso.get().load(savedImageUri).into(imageView);
        }

        Button myButton_contact = findViewById(R.id.button_contacts);
        Button myButton_biofeedback = findViewById(R.id.button_biofeedback);
        Button myButton_sos = findViewById(R.id.sos);
        Button myButton_logout = findViewById(R.id.logout_button);
        Button calibration = findViewById(R.id.calibration);
        final Button buttonConnect = findViewById(R.id.buttonConnect);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        FirebaseMessaging.getInstance().subscribeToTopic("News")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscribed to 'News' topic successfully");
                        } else {
                            Log.e(TAG, "Failed to subscribe to 'News' topic", task.getException());
                        }
                    }
                });


        // If a Bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a Bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("ResourceAsColor")
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                buttonConnect.setText("Connected");
                                buttonConnect.setBackgroundColor(R.color.green);
                                buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                buttonConnect.setText("Failed");
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        updateSensorValue(arduinoMsg); // Call method to update sensor value
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MetricsActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });


        myButton_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        myButton_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

        myButton_biofeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, BiofeedbackActivity.class);
                startActivity(intent);
            }
        });

        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, CalibrationActivity.class);
                startActivity(intent);
            }
        });

        myButton_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MetricsActivity.this, SosActivity.class);
                intent.putExtra("key_value_1", averageMonth);
                intent.putExtra("key_value_2", averageTime);
                intent.putExtra("key_value_3", latestDate);
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

                        String fullName = firstName + " " + lastName;
                        TextView userNameTextView = findViewById(R.id.user_name_textview);
                        userNameTextView.setText(fullName);

                        checkAndSetCalibrationButton();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // error handling
                }
            });

            // Reference to "patientmetrics" node
            DatabaseReference patientMetricsRef = FirebaseDatabase.getInstance().getReference("patientmetrics").child(userId);

            patientMetricsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Iterable<DataSnapshot> metricSnapshots = dataSnapshot.getChildren();
                        if (metricSnapshots.iterator().hasNext()) {
                            float totalDuration = 0;
                            int nonZeroCount = 0;
                            Map<Integer, Integer> monthCount = new HashMap<>();
                            DataSnapshot latestMetricSnapshot = null;

                            for (DataSnapshot metricSnapshot : metricSnapshots) {
                                String currentSeizureTimes = metricSnapshot.child("seizureTimes").getValue(String.class);
                                Float seizureDuration = metricSnapshot.child("seizureDuration").getValue(Float.class);

                                if (seizureDuration != null && seizureDuration > 0) {
                                    totalDuration += seizureDuration;
                                    nonZeroCount++;
                                }

                                if (latestMetricSnapshot == null || isNewerSeizureTimes(currentSeizureTimes, latestMetricSnapshot.child("seizureTimes").getValue(String.class))) {
                                    latestMetricSnapshot = metricSnapshot;
                                }

                                if (metricSnapshot.hasChild("seizureTimes")) {
                                    try {
                                        Date seizureTime = dateFormat.parse(metricSnapshot.child("seizureTimes").getValue(String.class));
                                        calendar.setTime(seizureTime);
                                        int seizureMonth = calendar.get(Calendar.MONTH);
                                        monthCount.put(seizureMonth, monthCount.getOrDefault(seizureMonth, 0) + 1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            int totalSeizureCount = 0;
                            int totalMonthCount = monthCount.size();

                            for (int count : monthCount.values()) {
                                totalSeizureCount += count;
                            }

                            float averageSeizuresPerMonth = (totalMonthCount > 0) ? (float) totalSeizureCount / totalMonthCount : 0;

                            Log.d(TAG, "Total Seizure Count: " + totalSeizureCount);
                            Log.d(TAG, "Total Month Count: " + totalMonthCount);
                            Log.d(TAG, "Average Seizures per Month: " + averageSeizuresPerMonth);

                            averageMonth = String.format("%.2f", averageSeizuresPerMonth);

                            TextView seizureMonthTextView = findViewById(R.id.seizure_month);
                            seizureMonthTextView.setText(averageMonth);

                            float averageDuration = (nonZeroCount > 0) ? totalDuration / nonZeroCount : 0;
                            averageTime = String.format("%.2f",averageDuration);
                            TextView averageDurationTextView = findViewById(R.id.duration_value);
                            averageDurationTextView.setText(averageTime);

                            if (latestMetricSnapshot != null) {
                                String lastDate = latestMetricSnapshot.child("seizureTimes").getValue(String.class);
                                Integer lastCount = latestMetricSnapshot.child("seizureCount").getValue(Integer.class);

                                if (lastCount != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date lastSeizureDate = sdf.parse(lastDate);
                                        Date currentDate = new Date();

                                        long diffInMillies = currentDate.getTime() - lastSeizureDate.getTime();
                                        long daysDifference = diffInMillies / (24 * 60 * 60 * 1000);

                                        latestDate = String.valueOf(lastSeizureDate);

                                        Log.d(TAG, "Last Seizure Date: " + lastSeizureDate);
                                        Log.d(TAG, "Current Date: " + currentDate);
                                        Log.d(TAG, "Days Difference: " + daysDifference);

                                        TextView lastSeizureTextView = findViewById(R.id.days);
                                        lastSeizureTextView.setText(String.valueOf(daysDifference));

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Error parsing date");
                                    }
                                } else {
                                    Log.e(TAG, "lastCount is null");
                                }
                            } else {
                                Log.e(TAG, "latestMetricSnapshot is null");
                            }
                        } else {
                            Log.e(TAG, "No child nodes under 'patientmetrics'");
                        }
                    } else {
                        Log.e(TAG, "DataSnapshot does not exist");
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors ...
                }
            });
        }



            imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);

            // Save the image URI to SharedPreferences
            saveImageUriToSharedPreferences(imageUri.toString());
        }
    }


    private void saveImageUriToSharedPreferences(String imageUri) {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userImageUri", imageUri);
        editor.apply();
    }

    private void updateSensorValue(String sensorValue) {
        // Check the sensor value and display a Toast
        if (sensorValue.trim().equals("0")) {
            handleSeizurePredicted();
        } else if (sensorValue.trim().equals("1")) {
            handleSeizureDetected();
        }
        else if (sensorValue.trim().equals("2")) {
            handleCalibration();
        }

    }

    private boolean isNewerSeizureTimes(String newSeizureTimes, String oldSeizureTimes) {
        if (newSeizureTimes == null || oldSeizureTimes == null) {
            return false; // or handle the null values as appropriate for your use case
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date newDate = dateFormat.parse(newSeizureTimes);
            Date oldDate = dateFormat.parse(oldSeizureTimes);


            // Compare the dates
            return newDate.after(oldDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing seizureTimes", e);
            return false; // Handle parsing error
        }
    }

    private void checkAndSetCalibrationButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PatientMetrics/" + userId + "/calibrate");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Calibration node exists, read the calibration status
                        Float calibrateStatus = dataSnapshot.getValue(Float.class);
                        // Now, set the button visibility accordingly
                        setButtonVisibility(calibrateStatus);
                    } else {
                        // The PatientMetrics node for the user does not exist
                        // Create a new PatientMetrics object with default values
                        PatientMetrics defaultMetrics = new PatientMetrics(0.0f, 0, "", 0.0f);

                        // Save the default metrics to the database
                        DatabaseReference metricsReference = FirebaseDatabase.getInstance().getReference("PatientMetrics/" + userId);
                        metricsReference.setValue(defaultMetrics);

                        // Now, the node exists with default values, set the button visibility accordingly
                        setButtonVisibility(defaultMetrics.getCalibrate());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        }
    }

    private void setButtonVisibility(Float calibrateStatus) {
        runOnUiThread(() -> {
            Button myButtonCalibration = findViewById(R.id.calibration);
            myButtonCalibration.setVisibility((calibrateStatus != null && calibrateStatus == 1) ? View.GONE : View.VISIBLE);

            if (calibrateStatus == null || calibrateStatus != 0.0f) {
                myButtonCalibration.callOnClick();
            }
        });
    }

    private void handleSeizurePredicted() {
        Toast.makeText(MetricsActivity.this, "Seizure Predicted", Toast.LENGTH_SHORT).show();
        makeNotification();
    }
    public void makeNotification(){
        Log.d("NotificationTest", "makeNotification called");

        String channelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),channelID);
        builder.setSmallIcon(R.drawable.logovmoov)
                .setContentTitle("Seizure predicted")
                .setContentText("You might experience a seizure in the next 30 minutes. Please take preventive measures.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        Intent intent = new Intent(getApplicationContext(), MetricsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);
            if (notificationChannel == null){
                Log.d("NotificationTest", "Creating Notification Channel");
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Channel Name", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Log.d("NotificationTest", "Sending notification");
        notificationManager.notify(/*notificationId*/ 1, builder.build());
    }
    private void handleCalibration(){
        Toast.makeText(MetricsActivity.this, "Calibration Finished", Toast.LENGTH_SHORT).show();
    }
    private void handleSeizureDetected() {
        Toast.makeText(MetricsActivity.this, "Seizure Detected", Toast.LENGTH_SHORT).show();
        obtenerUbicacion(); //Obtiene la ubicación y la manda por sms a sus contactos de emergencia
        sendSms(); //Envía sms alerta a sus contactos de emrgencia
        Intent intent = new Intent(MetricsActivity.this, SeizureActivity.class);
        startActivity(intent);
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties, the method below may not work for different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until a termination character is reached.
                    Then send the whole String message to the GUI Handler.
                     */
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.e("Arduino Message", readMessage);
                    handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); // converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        super.onBackPressed();
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void sendSms() {
        String phoneNumber = "1158948455";  // Reemplaza con el número de teléfono al que deseas enviar el SMS
        String message = "Se detectó que el usuario está sufriendo una crisis epiléptica.";

        try {
            // Obtener la instancia de SmsManager
            SmsManager smsManager = SmsManager.getDefault();

            // Enviar el SMS
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Mostrar un mensaje de éxito
            Toast.makeText(MetricsActivity.this, "SMS enviado correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Mostrar un mensaje en caso de error
            Toast.makeText(MetricsActivity.this, "Error al enviar el SMS.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, enviar el SMS
                sendSms();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(MetricsActivity.this, "Permiso para enviar SMS denegado.", Toast.LENGTH_SHORT).show();
            }
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

            // Query to fetch the emergency contact with alertFlag = 1
            Query alertContactQuery = userContactsRef.orderByChild("alertFlag").equalTo(1);

            alertContactQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                        // Get the phone number of the emergency contact
                        String phoneNumber = contactSnapshot.child("phoneNumber").getValue(String.class);
                        String message = "Se detectó que el usuario está sufriendo una crisis epiléptica.";

                        //String message = "Se detectó que el usuario está sufriendo una crisis epiléptica (prueba)";

                        if (phoneNumber != null) {
                            // Create a Uri that represents the location in geo format
                            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);

                            // Create a Google Maps link with the location
                            String messagee = "Crisis epiléptica en https://www.google.com/maps?q=" + latitude + "," + longitude;

                            try {
                                // Obtener la instancia de SmsManager
                                SmsManager smsManager = SmsManager.getDefault();

                                PendingIntent sentIntent = PendingIntent.getBroadcast(MetricsActivity.this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
                                PendingIntent deliveredIntent = PendingIntent.getBroadcast(MetricsActivity.this, 0, new Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE);

                                // Enviar el SMS
                                //smsManager.sendTextMessage(phoneNumber, null, messagee, null, null);
                                smsManager.sendTextMessage(phoneNumber, null, messagee, sentIntent, deliveredIntent);

                                // Mostrar un mensaje de éxito
                                Toast.makeText(MetricsActivity.this, "SMS enviado", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                // Mostrar un mensaje en caso de error
                                Toast.makeText(MetricsActivity.this, "Error al enviar el SMS.", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
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
}