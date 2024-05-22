package com.example.kyau_file_share.phone_and_pc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kyau_file_share.R;
import com.example.kyau_file_share.Singleton;

import java.net.Socket;

public class Sending_process extends AppCompatActivity {

    private Socket socket;
    private Button button8;
    private Button button9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sending_process);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // variables initialization
        socket = Singleton.socket;
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);

        button8.setOnClickListener(v -> {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(Sending_process.this, Send_file.class);
            startActivity(intent);
        });
    }
}