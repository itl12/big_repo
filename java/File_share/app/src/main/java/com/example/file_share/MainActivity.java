package com.example.file_share;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
        Socket[] clientSocket = {null};
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
//                            try {















//                                serverSocket[0] = new ServerSocket(8080); // Replace 8080 with the desired port number
//
//                                // Get the IP address and port number
//                                String ipAddress = getIpAddress();
//                                int portNumber = serverSocket[0].getLocalPort();
//
//                                // Format the information
//                                String serverInfo = "Server is running at " + ipAddress + ":" + portNumber;
//
//                                mainHandler.post(()->{
//                                    outputTextView.setText("Server is on" + serverInfo);
//                                    button.setText("Stop");
//                                });
//
//                                // Wait for a client to connect
//                                clientSocket[0] = serverSocket[0].accept();
//
//                                // Get the client's IP address
//                                String clientIpAddress = clientSocket[0].getInetAddress().getHostAddress();
//
//                                // Display the connected client's IP address
//                                mainHandler.post(() -> {
//                                    outputTextView.setText("Connected to client: " + clientIpAddress);
//                                });
//
//                                // Handle communication with the client (e.g., sending and receiving data)
//
//
//                                // Get the number of files to be received
//                                int numFiles = getNumFiles(clientSocket[0], outputTextView);
//                                sendAcknowledge(clientSocket[0]);
////
////                                while(numFiles > 0){
////                                    receiveFile(clientSocket[0], outputTextView);
////                                    numFiles--;
////                                }
//                                receiveFile(clientSocket[0], outputTextView);

//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                mainHandler.post(()->{
//                                    outputTextView.append(e.getMessage() + " offfff");
//                                });
//                            }
                        }
                    }).start();

                }else{
                    clicked[0] = false;
                    // Stop the TCP server
                    try {
                        clientSocket[0].close();
                        serverSocket[0].close();
                        mainHandler.post(()->{
                            outputTextView.setText("all closed successfully");
                        });
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
    private int getNumFiles(Socket clientSocket, TextView outputTextView) {

        // Specify the number of bytes to receive
        int numberOfBytesToReceive = 4; // Assuming 4 bytes for an integer

        try {
            // Create a DataInputStream to read binary data from the client
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            // Create a byte array to store the received data
            byte[] receivedData = new byte[numberOfBytesToReceive];

            // Read the specified number of bytes from the stream
            dataInputStream.readFully(receivedData);

            // Convert the received bytes to an integer (assuming 4 bytes for an integer)
            int receivedNumber = ByteBuffer.wrap(receivedData).getInt();

            // Process received number here
            mainHandler.post(() -> {
                outputTextView.append("\nReceived number: " + receivedNumber);
            });
            return receivedNumber;

        } catch (IOException e) {
            e.printStackTrace();
            mainHandler.post(() -> {
                outputTextView.append(e.getMessage());
            });
            return -1;
        }
    } // end of getNumFiles function
    private void receiveFile(Socket clientSocket, TextView outputTextView) throws IOException {

        // Specify the number of bytes to receive ( filesize )
        int numberOfBytesToReceive = 8;
        long receivedNumber = 0;
        long totalBytesReceived = 0;

        try {
            // Create a DataInputStream to read binary data from the client
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            // Create a byte array to store the received data
            byte[] receivedData = new byte[numberOfBytesToReceive];

            // Read the specified number of bytes from the stream
            dataInputStream.readFully(receivedData);

            // Convert the received bytes to an integer (assuming 4 bytes for an integer)
            receivedNumber = ByteBuffer.wrap(receivedData).getLong();

            // Process received number here
            long finalReceivedNumber = receivedNumber;
            mainHandler.post(() -> {
                outputTextView.append("\nReceived number of bytes to receive: " + finalReceivedNumber);
            });

        } catch (IOException e) {
            e.printStackTrace();
            mainHandler.post(() -> {
                outputTextView.append(e.getMessage());
            });
        }

        sendAcknowledge(clientSocket);

        // receive file name
        try{
            byte[] fileNameBytes = new byte[1024];
            int bytesRead = clientSocket.getInputStream().read(fileNameBytes);
            String fileName = new String(fileNameBytes, 0, bytesRead);

            mainHandler.post(() -> {
                outputTextView.append("Received file name: " + fileName);
            });

        }catch (Exception e){
            e.printStackTrace();
            mainHandler.post(() -> {
                outputTextView.append(e.getMessage());
            });
        }
        sendAcknowledge(clientSocket);



//        while (totalBytesReceived < receivedNumber) {
//            int chunk = 1024;
//            byte[] buffer = new byte[chunk];
//
//        }


        // Get the root directory of your app's private internal storage
        File internalStorageDir = getFilesDir();

// Append the desired directory structure
        File myAppDataDir = new File(internalStorageDir, "Android/MyAppData");

// Create the directory if it doesn't exist
        if (!myAppDataDir.exists()) {
            myAppDataDir.mkdirs(); // Create directories if they don't exist
        }

// Create a file within the "MyAppData" directory
        File myFile = new File(myAppDataDir, "example.txt");

        try {
            // Write data to the file
            FileOutputStream outputStream = new FileOutputStream(myFile);
            String fileContents = "Hello, world!";
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            mainHandler.post(() -> {
                outputTextView.append("File written successfully.");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }




    }// end of receive function


    private void sendAcknowledge(Socket clientSocket) throws IOException {
        try {
            // Generate 1024 bytes of data
            byte[] data = new byte[1024];
            Arrays.fill(data, (byte) 'A'); // Fill with 'A' characters

            // Get the output stream to send data to the client
            OutputStream outputStream = clientSocket.getOutputStream();

            // Send the data
            outputStream.write(data);
            outputStream.flush(); // Flush the output stream
        }catch (Exception e){
            e.printStackTrace();
        }
    } // end of sendAcknowledge function
}