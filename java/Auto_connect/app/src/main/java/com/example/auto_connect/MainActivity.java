package com.example.auto_connect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private Socket socket;

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
        textView = findViewById(R.id.textView);

        button.setOnClickListener(v -> {
            // start client activity

            Thread thread = new Thread(() -> {

                String ip = "192.168.0.";

                for(int i = -155; i < 255; i++){
                    try{

                        socket = new Socket();
                        socket.connect(new InetSocketAddress(ip + i, 8000), 50 );


                        runOnUiThread(() -> {textView.append("Connected\n");});
                        break;
                    }catch(Exception e){
                        e.printStackTrace();
//                        runOnUiThread(() -> {textView.append("Error "+ e.getMessage()+ "\n");});

                    };
                };
                try{
                    socket.close();
                    runOnUiThread(() -> {textView.append("Disconnected");});
                }catch(Exception e){
                    e.printStackTrace();
                };

            });
            thread.start();



        });




    }
}

