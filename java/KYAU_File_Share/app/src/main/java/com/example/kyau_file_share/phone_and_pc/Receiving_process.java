package com.example.kyau_file_share.phone_and_pc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kyau_file_share.R;
import com.example.kyau_file_share.Singleton;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Receiving_process extends AppCompatActivity {

    private Button button12;
    private ScrollView scrollView;
    private TextView textView;
    private ProgressBar progressBar;
    private Socket socket;
    private String filename;
    private long filesize;
    private int fileCount = 1;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receiving_process);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        button12 = findViewById(R.id.button12);
        scrollView = findViewById(R.id.scrollView2);
        textView = findViewById(R.id.textView4);
        progressBar = findViewById(R.id.progressBar2);
        socket = Singleton.socket;


        // Acquire wake lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::FileTransferWakeLock");
        wakeLock.acquire(30*60*1000L /*10 minutes*/);

        // Acquire wifi lock
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyApp::FileTransferWifiLock");
        wifiLock.acquire();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try{
                    Singleton.socket.close();
                    runOnUiThread(() -> { Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();});
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] scrollViewLocation = new int[2];
                int[] button12Location = new int[2];
                scrollView.getLocationOnScreen(scrollViewLocation);
                button12.getLocationOnScreen(button12Location);
                int distance = button12Location[1] - scrollViewLocation[1];
                ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
                layoutParams.height = distance - 40;
                scrollView.setLayoutParams(layoutParams);
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        button12.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        // Start receiving Files
        Thread thread = new Thread(()->{
            while (Singleton.socket.isConnected()){

                int result = sendAck();     if (result == -1){return;}
                result = recvFilename();    if (result == -1){return;}
                result = sendAck();         if(result == -1){return;}
                result = recvFilesize();    if(result == -1){return;}
                result = sendAck();         if(result == -1){return;}
                result = recvFiledata();    if(result == -1){return;}

            }
            appendTextToTextView("All files received.\n", Color.GRAY, 18);
        });
        thread.start();


        Intent serviceIntent = new Intent(this, FileTransferService.class);
        startForegroundService(serviceIntent);

    }// onCreate

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLocks();
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    private int recvFiledata() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            if (file.exists()) {
                file.delete();
            }

            InputStream inputStream = socket.getInputStream();

            // Create a FileOutputStream to save the received file
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            // Read data from the input stream and write to file
            byte[] buffer = new byte[1024000];
            int bytesRead;
            long total = 0;
            long left = filesize-total;
            while (left > 0 ) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                total += bytesRead;
                left -= bytesRead;
                fileOutputStream.write(buffer, 0, bytesRead);
                int progress = (int) (total * 100 / filesize);
                runOnUiThread(() -> progressBar.setProgress(progress));
            }

//            appendTextToTextView("File received successfully.");
            appendTextToTextView("Receive success!\n", Color.GRAY, 18);
        }catch (Exception e){
            e.printStackTrace();
            appendTextToTextView(e.toString() , Color.RED, 20);
            appendTextToTextView("Error: Connection lost!78" , Color.RED, 20);
            return -1;
        };
        return 1;
    }
    private int recvFilesize() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            // Read the file size (8 bytes, unsigned long)
            filesize = dataInputStream.readLong();  // Read 8 bytes as a long integer
            runOnUiThread(()->{ progressBar.setMax( 100 );});

//            appendTextToTextView("Received filesize: " + filesize);

        } catch (IOException e) {
            e.printStackTrace();
            appendTextToTextView("Error: Connection lost!1" , Color.RED, 20);
            return -1;
        }
        return 1;
    }

    private int recvFilename() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            // Read the length of the filename (4 bytes, unsigned int)
            int filenameLength = dataInputStream.readInt();

            // Read the filename based on the length
            byte[] filenameBytes = new byte[filenameLength];
            dataInputStream.readFully(filenameBytes);

            // Convert the byte array to a string
            filename = new String(filenameBytes);

//            appendTextToTextView("Received filename:" + filename);
            appendTextToTextView(fileCount + ". " + filename + " is now receiving.", Color.BLUE, 18);
            fileCount++;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    };

    private int sendAck() {
        try {
            byte[] data = new byte[1024];
            Arrays.fill(data, (byte) 'A');
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
//            runOnUiThread(() -> output.append("Ack sent\n"));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            appendTextToTextView("Error: Connection lost!2" , Color.RED, 20);
            runOnUiThread(() -> button12.setText("Disconnected!"));
            runOnUiThread(()-> button12.setEnabled(false));
            if(Singleton.socket.isConnected()){
                try {
                    Singleton.socket.close();
                    return -1;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return -1;
                }
            }
            return -1;
        }
    }

    private int recvAck() {
        try {
            byte[] buffer = new byte[1024];
            InputStream inputStream = socket.getInputStream();
            int bytesRead = inputStream.read(buffer);
            if (bytesRead != -1) {
                String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_16);
//                runOnUiThread(() -> output.append("Received ack: " + "\n"));
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button12.setText("Disconnected!"));
            runOnUiThread(()-> button12.setEnabled(false));
            if(Singleton.socket.isConnected()){
                try {
                    Singleton.socket.close();
                    return -1;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return -1;
                }
            }
            return -1;
        }
    }

    private void appendTextToTextView(String newText) {
        appendTextToTextView(newText, Color.BLACK, 18); // Default color and font size
    }
    private void appendTextToTextView(String newText, int color, float size) {
        runOnUiThread(()->{
            SpannableString spannableString = new SpannableString(newText + "\n");
            spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
            spannableString.setSpan(new AbsoluteSizeSpan((int) size, true), 0, spannableString.length(), 0);
            textView.append(spannableString);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }



}