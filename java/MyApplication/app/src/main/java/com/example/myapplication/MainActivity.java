package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;

    private Handler mainHandler;
    private LocalOnlyHotspotReservation hotspotReservation;
    private WifiManager wifiManager;
    private TextView outputTextView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize variables
        mainHandler = new Handler(Looper.getMainLooper());
        outputTextView = findViewById(R.id.output);
        button = findViewById(R.id.button);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Check and request necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        // Set up button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager != null) {
                    startLocalOnlyHotspot();
                } else {
                    Toast.makeText(MainActivity.this, "WifiManager not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startLocalOnlyHotspot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        wifiManager.startLocalOnlyHotspot(new LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotReservation = reservation;
                SoftApConfiguration softApConfig = hotspotReservation.getSoftApConfiguration();
                Log.i(TAG, "Local Only Hotspot started: SSID = " + softApConfig.getSsid() + " Password = " + softApConfig.getPassphrase());
                Toast.makeText(MainActivity.this, "Hotspot started: SSID = " + softApConfig.getSsid(), Toast.LENGTH_SHORT).show();
                outputTextView.setText("SSID: " + softApConfig.getSsid() + "\nPassword: " + softApConfig.getPassphrase());
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.i(TAG, "Local Only Hotspot stopped");
                Toast.makeText(MainActivity.this, "Hotspot stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.e(TAG, "Local Only Hotspot failed to start: " + reason);
                Toast.makeText(MainActivity.this, "Hotspot failed to start", Toast.LENGTH_SHORT).show();
            }
        }, new Handler(Looper.getMainLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hotspotReservation != null) {
            hotspotReservation.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
