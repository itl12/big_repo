package com.example.kyau_file_share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kyau_file_share.phone_and_pc.PhoneAndComputerHome;
import com.example.kyau_file_share.phone_and_pc.Receive_file;
import com.example.kyau_file_share.phone_and_pc.Send_file;
import com.example.kyau_file_share.phone_and_pc.Sending_process;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button2;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        button2.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhoneAndComputerHome.class);
            startActivity(intent);
        });

        // Acquire wake lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release wake lock to avoid battery drain
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}