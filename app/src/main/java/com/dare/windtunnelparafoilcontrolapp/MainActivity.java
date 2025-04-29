package com.dare.windtunnelparafoilcontrolapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ComponentCaller;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.dare.windtunnelparafoilcontrolapp.communication.bluetooth.ServoBluetooth;
import com.dare.windtunnelparafoilcontrolapp.utils.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dare.windtunnelparafoilcontrolapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    ServoBluetooth servoBluetooth;

    private final int PERMISSION_REQUEST_BLUETOOTH = 1;
    private final int ENABLE_REQUEST_BLUETOOTH = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        checkBleHardware();
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission(); // startBluetooth will be called in onRequestPermissionResult callback
        } else {
            startBluetooth();
        }

    }




    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Util.hasPermission(this, Manifest.permission.BLUETOOTH_SCAN) &&
                    Util.hasPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        }
        else {
            return Util.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    Util.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Util.requestPermissions(this, PERMISSION_REQUEST_BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT);
        }
        else {
            Util.requestPermissions(this, PERMISSION_REQUEST_BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkBleHardware() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("MainActivity", "BLE not supported");
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetooth() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) { // turn on bluetooth if it's off
            // servoBluetooth will then be set up in onActivityResult callback
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_REQUEST_BLUETOOTH);
        }
        else {
            servoBluetooth = new ServoBluetooth(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            // Note, permissions are granted sequentially, so we cannot start bluetooth until all permissions are granted
            if (hasBluetoothPermission()) startBluetooth(); // initialize bluetooth communciation

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if (requestCode == ENABLE_REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                // Bluetooth has been enabled
                Log.d("MainActivity", "Bluetooth enabled by user");
                servoBluetooth = new ServoBluetooth(getApplicationContext()); // Initialize Bluetooth communication
            } else {
                // User denied to turn on Bluetooth
                Log.e("MainActivity", "Bluetooth not enabled, quitting");
                finish(); // Close the app if Bluetooth is required
            }
        }

    }
}