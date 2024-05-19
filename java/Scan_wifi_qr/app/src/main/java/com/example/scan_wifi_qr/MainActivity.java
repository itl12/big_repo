package com.example.scan_wifi_qr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private WifiManager wifiManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraPermission = result.getOrDefault(Manifest.permission.CAMERA, false);
                Boolean locationPermission = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (cameraPermission != null && cameraPermission && locationPermission != null && locationPermission) {
                    startQrCodeScanner();
                } else {
                    textView.setText("Permissions are required to scan QR codes and connect to Wi-Fi.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        button.setOnClickListener(v -> checkPermissionsAndStartScanner());

        checkPermissionsAndStartScanner();
    }

    private void checkPermissionsAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startQrCodeScanner();
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void startQrCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a Wi-Fi QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                textView.setText("Cancelled");
            } else {
                textView.setText("Scanned: " + result.getContents());
                handleQrCodeResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleQrCodeResult(String qrCodeContents) {
        // Example format: WIFI:T:WPA;S:MySSID;P:MyPassword;;
        String[] qrCodeParts = qrCodeContents.split(";");
        String ssid = null;
        String password = null;

        for (String part : qrCodeParts) {
            if (part.startsWith("S:")) {
                ssid = part.substring(2);
            } else if (part.startsWith("P:")) {
                password = part.substring(2);
            }
        }

        if (ssid != null && password != null) {
            connectToWifi(ssid, password);
        } else {
            textView.setText("Invalid Wi-Fi QR Code");
        }
    }

    private void connectToWifi(String ssid, String password) {
        WifiNetworkSpecifier wifiNetworkSpecifier =
                new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(password)
                        .build();

        NetworkRequest networkRequest =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(wifiNetworkSpecifier)
                        .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                runOnUiThread(() -> textView.setText("Connected to Wi-Fi: " + ssid));
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                runOnUiThread(() -> textView.setText("Failed to connect to Wi-Fi: " + ssid));
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runOnUiThread(() -> textView.setText("Lost connection to Wi-Fi: " + ssid));
            }
        };

        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}
