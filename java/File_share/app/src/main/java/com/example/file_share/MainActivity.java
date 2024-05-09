package com.example.file_share;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {

    // Handler for main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


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

        Button button = this.findViewById(R.id.button);
        TextView outputTextView = findViewById(R.id.output);
        final Boolean[] clicked = {false};









        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!clicked[0]){
                    clicked[0] = true;
                }

                // Start the TCP server in a new thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServerSocket serverSocket = new ServerSocket(8080); // Replace 8080 with the desired port number


                            mainHandler.post(()->{
                                outputTextView.setText("Server is on");
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            mainHandler.post(()->{
                                outputTextView.setText(e.getMessage());
                            });
                        }
                    }
                }).start();
            }
        });
    }
}