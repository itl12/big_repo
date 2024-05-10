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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

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

        final boolean[] clicked = {false};
        ServerSocket[] serverSocket = {null};
        TextView outputTextView = findViewById(R.id.output);
        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!clicked[0]){
                    clicked[0] = true;

                    // Start the TCP server in a new thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                serverSocket[0] = new ServerSocket(8080); // Replace 8080 with the desired port number

                                // Get the IP address and port number
                                String ipAddress = getIpAddress();
                                int portNumber = serverSocket[0].getLocalPort();

                                // Format the information
                                String serverInfo = "Server is running at " + ipAddress + ":" + portNumber;

                                mainHandler.post(()->{
                                    outputTextView.setText("Server is on" + serverInfo);
                                    button.setText("Stop");
                                });

                                // Wait for a client to connect
                                Socket clientSocket = serverSocket[0].accept();

                                // Get the client's IP address
                                String clientIpAddress = clientSocket.getInetAddress().getHostAddress();

                                // Display the connected client's IP address
                                mainHandler.post(() -> {
                                    outputTextView.setText("Connected to client: " + clientIpAddress);
                                });

                                // Handle communication with the client (e.g., sending and receiving data)
                                // ...

                            } catch (IOException e) {
                                e.printStackTrace();
                                mainHandler.post(()->{
                                    outputTextView.setText(e.getMessage());
                                });
                            }
                        }
                    }).start();
                }else{
                    clicked[0] = false;
                    // Stop the TCP server
                    try {
                        serverSocket[0].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mainHandler.post(()->{
                            outputTextView.setText(e.getMessage());
                        });
                    }
                    outputTextView.setText("Server is off");
                    button.setText("Start");
                }

            }
        });




    }

    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}