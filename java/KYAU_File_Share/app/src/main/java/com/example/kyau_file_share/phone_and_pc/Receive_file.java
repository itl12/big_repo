package com.example.kyau_file_share.phone_and_pc;

import com.example.kyau_file_share.Singleton;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kyau_file_share.R;

import java.io.IOException;
import java.net.ServerSocket;

public class Receive_file extends AppCompatActivity {

    private Button button10;
    private Button button11;
    private TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receive_file);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        button10 = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);
        textView3 = findViewById(R.id.textView3);


        button10.setOnClickListener(v -> {

        });

        button11.setOnClickListener(v -> {
            if (!isWifiOnAndConnected()) {
                // Wifi is either off or not connected
                Toast.makeText(this, "Wifi is not connected or disabled.", Toast.LENGTH_SHORT).show();
            }else{
                if (button11.getText().toString() != "Stop"){

                    Thread thread = new Thread(()->{
                        try {
                            Singleton.ip = Send_file.getIpAddress();
                            String strippedIp = Singleton.ip.substring(Singleton.ip.lastIndexOf(".") + 1, Singleton.ip.length());
                            runOnUiThread(()->{
                                textView3.setText("Pin : " + strippedIp);
                                button11.setText("Stop");
                                button10.setEnabled(false);
                            });

                            Singleton.serverSocket = new ServerSocket(8000);
                            Singleton.socket = Singleton.serverSocket.accept();
                            if (Singleton.socket != null) {
                                try{
                                    Singleton.serverSocket.close();
                                    runOnUiThread(()->{ Toast.makeText(Receive_file.this, "Connected.", Toast.LENGTH_SHORT).show();});
                                }catch (IOException e){
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(Receive_file.this, Receiving_process.class);
                                startActivity(intent);
//                                finish();
                                runOnUiThread(()->{
                                    try{
                                        Thread.sleep(1000);
                                        button11.setText("Direct Server");
                                        button10.setEnabled(true);
                                        textView3.setText("");
                                    }catch (InterruptedException e){
                                        e.printStackTrace();
                                    }
                                });
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                }else{
                    button11.setText("Direct Server");
                    button10.setEnabled(true);
                    textView3.setText("");
                    if(Singleton.serverSocket != null){
                        try{
                            Singleton.serverSocket.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(Singleton.socket != null){
                        try{
                            Singleton.socket.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        });




    } // onCreate

    private boolean isWifiOnAndConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (networkCapabilities != null) {
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return false;
    }


}

