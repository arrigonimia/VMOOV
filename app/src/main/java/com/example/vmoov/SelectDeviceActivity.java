package com.example.vmoov;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Import statements

public class SelectDeviceActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        // Bluetooth Setup
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check Bluetooth Permission for API >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        BLUETOOTH_PERMISSION_REQUEST);
            } else {
                // Permission already granted, proceed with Bluetooth operation
                displayPairedDevices(bluetoothAdapter);
            }
        } else {
            // For API < 23, Bluetooth permission is granted at installation time
            displayPairedDevices(bluetoothAdapter);
        }


    }

    private void displayPairedDevices(BluetoothAdapter bluetoothAdapter) {
        // Get List of Paired Bluetooth Devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_PERMISSION_REQUEST);
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                // TODO: Uncomment the line below to create a DeviceInfoModel instance
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName, deviceHardwareAddress);
                // TODO: Add deviceInfoModel to deviceList
                deviceList.add(deviceInfoModel);
            }
            // Display paired device using recyclerView
            RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this, deviceList);
            recyclerView.setAdapter(deviceListAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            View view = findViewById(R.id.recyclerViewDevice);
            Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle the action if needed
                }
            });
            snackbar.show();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with Bluetooth operation
                displayPairedDevices(BluetoothAdapter.getDefaultAdapter());
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                View view = findViewById(R.id.recyclerViewDevice);
                Snackbar snackbar = Snackbar.make(view, "Bluetooth permission denied", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Handle the action if needed
                    }
                });
                snackbar.show();
            }
        }
    }
}
