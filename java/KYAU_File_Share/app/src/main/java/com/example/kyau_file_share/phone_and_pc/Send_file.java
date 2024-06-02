package com.example.kyau_file_share.phone_and_pc;

import com.example.kyau_file_share.Singleton;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kyau_file_share.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class Send_file extends AppCompatActivity {

    private Button button5;
    private Button button6;
    private Button button7;
    private EditText ipInput;
    private Socket socket;




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        try{
//            Singleton.socket.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        button5.setEnabled(true);
        button6.setEnabled(true);
        button7.setText("Connect");
        button5.setText("Auto Connect");
        ipInput.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_file);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        ipInput = findViewById(R.id.ipInput);

        button5.setOnClickListener(v -> {
            startAutoConnect();
        });

        button6.setOnClickListener(v -> {

        });
        button7.setOnClickListener(v -> {
            button7Function();
        });



    } // onCreate


    // Functions definition

    private void startAutoConnect() {
        button5.setText("Connecting...");
        button5.setEnabled(false);
        button6.setEnabled(false);
        ipInput.setEnabled(false);
        button7.setText("Stop!");

        // TODO: AutoConnect
        Thread thread = new Thread(() -> {
            String ip = getIpAddress();
            Singleton.ip = ip;
            String strippedIp = ip.substring(0, ip.lastIndexOf(".") + 1);
            for (int i = 1; i < 255; i++) {
                try {
                    if (button7.getText() == "Connect")
                        break;
                    String newIp = strippedIp + i;
                    System.out.println(i + " " + newIp);
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(newIp, 8000), 50);
                    if (socket.isConnected()) {
                        showToast("Connected to " + newIp);
                        break;
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
            if (socket.isConnected()) {
                showToast("Connected");
                Singleton.socket = socket;
//                        runOnUiThread(() -> resetAllButton());
                Intent intent = new Intent(Send_file.this, Sending_process.class);
                startActivity(intent);
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> resetAllButton());
                } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
                    e.printStackTrace();
                }
            } else {
                showToast("Couldn't connect to " + ipInput.getText().toString());
                runOnUiThread(() -> resetAllButton());
            }
            if (!socket.isConnected()) {
                showToast("Couldn't connect to any device.");
                runOnUiThread(() -> resetAllButton());
            } else {
                Singleton.socket = socket;
            }
        });
        thread.start();

    } // startAutoConnect


    private void button7Function() {
        if (button7.getText() == "Stop!") {
            button5.setEnabled(true);
            button6.setEnabled(true);
            ipInput.setEnabled(true);
            button7.setText("Connect");
            button6.setText("Scan QR Code");
            button5.setText("Auto Connect");
        }else if(button7.getText() == "Connecting..."){
            resetAllButton();
        }
        else{
            if ( !ipInput.getText().toString().isEmpty())
            {
                button5.setEnabled(false);
                button6.setEnabled(false);
                ipInput.setEnabled(false);
                button7.setText("Connecting...");

                // TODO: Connect
                Thread thread = new Thread(() -> {
                    String ip = getIpAddress();
                    Singleton.ip = ip;
                    String strippedIp = ip.substring(0, ip.lastIndexOf(".") + 1);

                    while (button7.getText() == "Connecting...") {
                        try{
                            socket = new Socket();
                            socket.connect(new InetSocketAddress(strippedIp + ipInput.getText().toString(), 8000), 100);
                        }catch(Exception e){
//                            e.printStackTrace();
                        }
                        if (socket.isConnected())
                            break;
                    }
                    if (socket.isConnected()) {
                        showToast("Connected");
                        Singleton.socket = socket;
//                        runOnUiThread(() -> resetAllButton());
                        Intent intent = new Intent(Send_file.this, Sending_process.class);
                        startActivity(intent);
                        try {
                            Thread.sleep(2000);
                            runOnUiThread(() -> resetAllButton());
                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
                            e.printStackTrace();
                        }
                    } else {
                        showToast("Couldn't connect to " + ipInput.getText().toString());
                        runOnUiThread(() -> resetAllButton());
                    }
                });
                thread.start();
            }else{
                showToast("Please enter code first");
            }
        }
    }

    private void resetAllButton() {
        button5.setEnabled(true);
        button6.setEnabled(true);
        ipInput.setEnabled(true);
        button7.setText("Connect");
        button5.setText("Auto Connect");
    }



    // Get local ip Address
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

    private void showToast(String message) {
        runOnUiThread(()-> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }





}