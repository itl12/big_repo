package com.example.kyau_file_share.phone_and_pc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.fonts.Font;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.kyau_file_share.R;
import com.example.kyau_file_share.Singleton;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Sending_process extends AppCompatActivity {

    private Socket socket;
    private Button button8;
    private Button button9;
    private ProgressBar progressBar;
    private TextView output;
    private ScrollView scrollView;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private List<Uri> uris;
    private Queue<Uri> fileQueue = new LinkedList<>();
    private Set<Uri> fileSet = new HashSet<>();
    private boolean is_sending = false;
    private long fileSize;
    private int fileCount = 1;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
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
                // Handle the back button event here
                try {
                    Singleton.socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                finish();
                if (Singleton.socket != null) {
                    Toast.makeText(Sending_process.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // variables initialization
        socket = Singleton.socket;
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        progressBar = findViewById(R.id.progressBar);
        output = findViewById(R.id.output);
        scrollView = findViewById(R.id.scrollView);
        uris = new ArrayList<>();

        // Set a listener to check the height of the TextView
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] scrollViewLocation = new int[2];
                int[] button8Location = new int[2];
                scrollView.getLocationOnScreen(scrollViewLocation);
                button8.getLocationOnScreen(button8Location);
                int distance = button8Location[1] - scrollViewLocation[1];
                ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
                layoutParams.height = distance - 40;
                scrollView.setLayoutParams(layoutParams);
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });



        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    // Get the URI(s) of the selected file(s)
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            if (fileSet.add(result.getData().getClipData().getItemAt(i).getUri())) {
                                fileQueue.add(result.getData().getClipData().getItemAt(i).getUri());
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        if (fileSet.add(result.getData().getData())) {
                            fileQueue.add(result.getData().getData());
                        }
                    }

                    processQueue();
                } else {
                    Toast.makeText(Sending_process.this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }

        });

        button8.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        button9.setOnClickListener(v -> openFilePicker());


        Intent serviceIntent = new Intent(this, FileTransferService.class);
        startForegroundService(serviceIntent);

    }

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
    private void appendTextToTextView(String newText) {
        appendTextToTextView(newText, Color.BLACK, 18); // Default color and font size
    }
    private void appendTextToTextView(String newText, int color, float size) {
        runOnUiThread(()->{
            SpannableString spannableString = new SpannableString(newText + "\n");
            spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
            spannableString.setSpan(new AbsoluteSizeSpan((int) size, true), 0, spannableString.length(), 0);
            output.append(spannableString);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void processQueue() {
        if (is_sending) return;
        if (fileQueue.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "All Files Sent.", Toast.LENGTH_SHORT).show());
            appendTextToTextView("All Files Sent. You can choose more files to send.", Color.GRAY, 18);
            return;
        }
        is_sending = true;
        Uri fileUri = fileQueue.poll();
        sendFile(fileUri);
    }

    private int sendFilesize(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        try {
            // Get file size
            String[] projection = { OpenableColumns.SIZE };
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            fileSize = -1;
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (cursor.moveToFirst()) {
                    fileSize = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
            runOnUiThread(()->{ progressBar.setMax( 100 );});

            long finalFileSize = fileSize;
//            runOnUiThread(() -> output.append("Sending file size: " + finalFileSize + " "));

            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(fileSize);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buffer.array());
            outputStream.flush();
//            runOnUiThread(() -> output.append("done\n"));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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



    private void sendFile(Uri uri) {
//        runOnUiThread(() -> output.append("Sending file: " + uri.toString() + " \n"));
        Thread thread = new Thread(() -> {
            int result = 0;
            result = recvAck();
            if(result == -1) return;

            result = sendFileName(uri);
            if(result == -1) return;

            result = sendFilesize(uri);
            if(result == -1) return;

            result = sendFileData(uri);
            if(result == -1) return;

            result = recvAck();
            if(result == -1) return;

            is_sending = false;
            processQueue();
        });
        thread.start();
    }

    private int sendFileData(Uri uri) {
        InputStream inputStream = null;
        try {
            ContentResolver contentResolver = getContentResolver();
            inputStream = contentResolver.openInputStream(uri);

            long totalSend = 0;
            byte[] buffer;
            int bytesRead;
            OutputStream outputStream = socket.getOutputStream();

            while (totalSend < fileSize) {
                long remaining = fileSize - totalSend;
                long chunkSize = remaining < 1024000 ? remaining : 1024000;
                buffer = new byte[(int) chunkSize];

                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
                totalSend += bytesRead;
                int progress = (int) (totalSend * 100 / fileSize);
                runOnUiThread(() -> progressBar.setProgress(progress));
            }

            appendTextToTextView("Sent success!\n", Color.GRAY, 18);
//            runOnUiThread(() -> output.append("\nFile sent!\n"));
            return 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            runOnUiThread(() -> output.append("\nFile not found: " + e.getMessage() + "\n"));
            appendTextToTextView("File not found: " + e.getMessage(), Color.RED, 18);
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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
        } catch (IOException e) {
            e.printStackTrace();
//            runOnUiThread(() -> output.append("\nIO error: " + e.getMessage() + "\n"));
//            appendTextToTextView("IO error: " + e.getMessage(), Color.RED, 18);
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
//                runOnUiThread(() -> output.append("\nError closing stream: " + e.getMessage() + "\n"));
                appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
                runOnUiThread(() -> button8.setText("Disconnected!"));
                runOnUiThread(()-> button8.setEnabled(false));
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
    }

    private int sendFileName(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        try {
            DocumentFile documentFile = DocumentFile.fromSingleUri(this.getApplicationContext(), uri);
            String fileName = (documentFile != null && documentFile.exists()) ? documentFile.getName() : "";

//            runOnUiThread(() -> output.append("Sending file name: " + fileName + " \n"));
            appendTextToTextView(fileCount + ". " + fileName + " is now sending.", Color.BLUE, 18);
            fileCount++;

            OutputStream outputStream = socket.getOutputStream();
            byte[] fileNameBytes = fileName.getBytes();
            outputStream.write(fileNameBytes.length);  // 1 byte for filename length
            outputStream.write(fileNameBytes);
            outputStream.flush();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
//            runOnUiThread(() -> output.append("Error: " + e.getMessage() + " \n"));
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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
            appendTextToTextView("Error: Connection lost!" , Color.RED, 20);
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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
            runOnUiThread(() -> button8.setText("Disconnected!"));
            runOnUiThread(()-> button8.setEnabled(false));
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

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
        filePickerLauncher.launch(intent);
    }
}
