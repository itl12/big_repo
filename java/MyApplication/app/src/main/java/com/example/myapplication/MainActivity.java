

package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;
    private static final int ARP_CHECK_INTERVAL = 10000; // 10 seconds

    private Handler mainHandler;
    private LocalOnlyHotspotReservation hotspotReservation;
    private WifiManager wifiManager;
    private TextView outputTextView;
    private TextView ipAddressTextView;
    private Button button;
    private ImageView qrCodeImageView;
    private List<String> previousConnectedDevices;


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
        ipAddressTextView = findViewById(R.id.ip_address);
        button = findViewById(R.id.button);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        qrCodeImageView = findViewById(R.id.qr_code_image);
        previousConnectedDevices = new ArrayList<>();

        // Check and request necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        // Set up button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager != null ) {
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

                // Generate QR code
                String qrCodeContent = "WIFI:T:WPA;S:" + softApConfig.getSsid() + ";P:" + softApConfig.getPassphrase() + ";;";
                generateQRCode(qrCodeContent);
//                displayLocalHotspotIpAddress();
                displayConnectedDevicesIpAddress();
                mainHandler.post(arpTableCheckRunnable);
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

    private void generateQRCode(String content) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
             BitMatrix bitMatrix = barcodeEncoder.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "QR Code generation failed", e);
        }
    }
    private Runnable arpTableCheckRunnable = new Runnable() {
        @Override
        public void run() {
            List<String> currentConnectedDevices = getConnectedDevices();
            if (!currentConnectedDevices.equals(previousConnectedDevices)) {
                StringBuilder sb = new StringBuilder();
                for (String ipAddress : currentConnectedDevices) {
                    sb.append(ipAddress).append("\n");
                }
                ipAddressTextView.append("Connected Devices IP Addresses:\n" + sb.toString());
                previousConnectedDevices = currentConnectedDevices;
            }
            mainHandler.postDelayed(this, ARP_CHECK_INTERVAL);
        }
    };
    private void displayLocalHotspotIpAddress() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {

            // Register network callback to listen for Wifi network changes
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    checkLocalHotspotIp(network);
                }
            };
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build(), networkCallback);
        }
    }
    private void displayConnectedDevicesIpAddress() {
        List<String> connectedDevices = getConnectedDevices();
        if (!connectedDevices.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String ipAddress : connectedDevices) {
                sb.append(ipAddress).append("\n");
            }
            ipAddressTextView.append("Connected Devices IP Addresses:\n" + sb.toString());
        } else {
            ipAddressTextView.append("Connected Devices IP Addresses: Not Available");
        }
    }
    private List<String> getConnectedDevices() {
        List<String> connectedDevices = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" +");
                if (parts.length >= 4) {
                    String ip = parts[0];
                    String mac = parts[3];
                    connectedDevices.add(ip);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading ARP table", e);
        }
        return connectedDevices;
    }
    // Function to check IP address on available network
    private void checkLocalHotspotIp(Network network) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            if (linkProperties != null) {
                List<InetAddress> addresses = linkProperties.getLinkAddresses().stream()
                        .map(linkAddress -> linkAddress.getAddress())
                        .filter(address -> address instanceof java.net.Inet4Address)
                        .collect(java.util.stream.Collectors.toList());
                for (InetAddress address : addresses) {
                    String ipAddress = address.getHostAddress();
                    ipAddressTextView.setText("Local Hotspot IP Address: " + ipAddress);
                    return;
                }
            }
        }
        // If no IP found, update text view
        ipAddressTextView.setText("Local Hotspot IP Address: Not Available");
    }

    // Unregister callback in onDestroy (assuming it's in an Activity)

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
