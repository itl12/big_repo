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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
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
                                // Get the TextView to display the received data
                                TextView outputTextView = findViewById(R.id.output);

                                // Get an InputStream from the client socket
                                InputStream inputStream = clientSocket.getInputStream();

                                // Create a buffer to store the incoming data
                                byte[] buffer = new byte[1024];

                                // Get the number of files to be received
                                double numFiles = getNumFiles(clientSocket, outputTextView);
//                                mainHandler.post(()->{
//                                    outputTextView.setText("Number of files to be received: " + numFiles);
//                                });
                                        

                                // Continuously read data from the client
//                                while (true) {
//                                    // Read data from the InputStream into the buffer
//                                    int bytesRead = inputStream.read(buffer);
//
//                                    // If data was received, process it
//                                    if (bytesRead > 0) {
//                                        // Convert the buffer to a string
//                                        String data = new String(buffer, 0, bytesRead);
//
//                                        // Display the data in the TextView
//                                        mainHandler.post(() -> {
//                                            outputTextView.setText(data);
//                                        });
//                                    } else {
//                                        // If the client has closed the connection, break out of the loop
//                                        break;
//                                    }
//                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                                mainHandler.post(()->{
                                    outputTextView.setText(e.getMessage() + " offfff");
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
                            outputTextView.setText(e.getMessage() + "off");
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


    // Get number of files to be received
    private double getNumFiles(Socket clientSocket, TextView outputTextView) {

        // Specify the number of bytes to receive
        int numberOfBytesToReceive = 8; // Assuming 4 bytes for an integer

        try {
            // Create a DataInputStream to read binary data from the client
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            // Create a byte array to store the received data
            byte[] receivedData = new byte[numberOfBytesToReceive];

            // Read the specified number of bytes from the stream
            dataInputStream.readFully(receivedData);

            // Convert the received bytes to an integer (assuming 4 bytes for an integer)
            long receivedNumber = ByteBuffer.wrap(receivedData).getLong();

            // Process received number here
            mainHandler.post(() -> {
                outputTextView.append("\nReceived number: " + receivedNumber);
            });
            return receivedNumber;

        } catch (IOException e) {
            e.printStackTrace();
            mainHandler.post(() -> {
                outputTextView.setText(e.getMessage());
            });
            return -1;
        }
//        try {
//            // Get an InputStream from the client socket
//            InputStream inputStream = clientSocket.getInputStream();
//
//            // Create a buffer to store the incoming data
//            byte[] buffer = new byte[1024]; // 4 bytes for an integer
//
//            // Read data from the InputStream into the buffer
//            int bytesRead = inputStream.read(buffer);
//            if (bytesRead > 0) {
//                // Convert the buffer to a string
//                String data = new String(buffer, 0, bytesRead);
////                int number = ByteBuffer.wrap(buffer).getInt();
//                // Display the data in the TextView
//                mainHandler.post(() -> {
//                    outputTextView.setText(buffer.toString());
//                });
//
//            }
//
////                // Convert the buffer to an integer
////                int number = ByteBuffer.wrap(buffer).getInt();
////
////                // Display the integer in the TextView
////                mainHandler.post(() -> {
////                    outputTextView.setText(String.valueOf(number));
////                    outputTextView.setText("buffer" + buffer.toString());
////                });
//
//            return 1;
//        }catch (IOException e) {
//            e.printStackTrace();
//            return -1;
//        }
    }
}