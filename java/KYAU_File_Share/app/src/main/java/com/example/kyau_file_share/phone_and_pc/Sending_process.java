package com.example.kyau_file_share.phone_and_pc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event here
               try{
                   Singleton.socket.close();
               }catch(Exception e){}

                finish();
               if(Singleton.socket!=null)
                   Toast.makeText(Sending_process.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
        // variables initialization
        socket = Singleton.socket;
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);

        button8.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }
}