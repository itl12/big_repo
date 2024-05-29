package com.example.kyau_file_share.phone_and_pc;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
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
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private TextView output;
    private ScrollView scrollView;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private List<Uri> uris;
    private Queue<Uri> fileQueue = new LinkedList<>();
    private Set<Uri> fileSet = new HashSet<>();
    private boolean is_sending = false;
    private long fileSize;

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
        output = findViewById(R.id.output);
        scrollView = findViewById(R.id.scrollView);
        uris = new ArrayList<>();

        // Set a listener to check the height of the TextView
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                int[] scrolViewLocation = new int[2];
                int[] button8Location = new int[2];
                scrollView.getLocationOnScreen(scrolViewLocation);
                button8.getLocationOnScreen(button8Location);
                int distance = button8Location[1] - scrolViewLocation[1];
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
//                    uris = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            if(fileSet.add(result.getData().getClipData().getItemAt(i).getUri())){
                                fileQueue.add(result.getData().getClipData().getItemAt(i).getUri());
                            };
                        }
                    } else if (result.getData().getData() != null) {
                        if(fileSet.add(result.getData().getData())){
                            fileQueue.add(result.getData().getData());
                        }
                    }

                    processQueue();
                }else{
                    Toast.makeText(Sending_process.this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }

        });


        button8.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        button9.setOnClickListener(v -> {
            openFilePicker();
        });
    } // onCreate




    // Functions initialization

    private void processQueue() {
        if(is_sending) return;
        if(fileQueue.isEmpty()) {
            runOnUiThread(()->{Toast.makeText(this, "All Files Send.", Toast.LENGTH_SHORT).show();});
            return;
        }
        is_sending = true;
        Uri fileUri = fileQueue.poll();
        sendFile(fileUri);
    }

    private void sendFilesize(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        try{
            // Get file size
            String[] projection = { OpenableColumns.SIZE };
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            fileSize = -1;
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (cursor.moveToFirst()) {
                    fileSize = cursor.getInt(sizeIndex);
                }
                cursor.close();
            }

            long finalFileSize = fileSize;
            runOnUiThread(()->{output.append("Sending file size: " + finalFileSize + " ");});

            ByteBuffer buffer = ByteBuffer.allocate(8); // 4 bytes for int
            buffer.putLong(fileSize);
            OutputStream outputStream = socket.getOutputStream();


            outputStream.write(buffer.array());
            outputStream.flush();
            runOnUiThread(()-> { output.append("done1 "); });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendFileData(Uri uri){
        try{
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            byte[] buffer = new byte[1024];
            int bytesRead;
            OutputStream outputStream = socket.getOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
            runOnUiThread(()->{output.append("\n file send!\n ");});
        }catch (Exception e){
                e.printStackTrace();
        }
    }

    private void sendFile(Uri uri){
        runOnUiThread(()->{output.append("Sending file: " + uri.toString() + " \n");});
        is_sending = false;
        Thread thread = new Thread(() -> {
            sendAck();
            sendFileName(uri);
            sendFilesize(uri);
            sendFileData(uri);


            processQueue();
        });
        thread.start();
    }

    private void sendFileName(Uri uri){


        ContentResolver contentResolver = getContentResolver();
        try{

            InputStream inputStream = contentResolver.openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileName;

            DocumentFile documentFile = DocumentFile.fromSingleUri(this.getApplicationContext(), uri);
            if (documentFile != null && documentFile.exists()) {
                fileName = documentFile.getName();
            } else {
                fileName = "";
            }

            runOnUiThread(()->{output.append("Sending file name: " + fileName + " \n");});
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(fileName.getBytes().length);  // 1 bytes for filename
            outputStream.write(fileName.getBytes());
            outputStream.flush();

        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(()->{output.append("Error: " + e.getMessage() + " \n");});
        }
    }

    private void sendAck(){
        try {
            // Generate 1024 bytes of data
            byte[] data = new byte[1024];
            Arrays.fill(data, (byte) 'A'); // Fill with 'A' characters

            // Get the output stream to send data to the client
            OutputStream outputStream = socket.getOutputStream();

            // Send the data
            outputStream.write(data);
            outputStream.flush(); // Flush the output stream
            runOnUiThread(()->{output.append("done0\n");});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void recvAck(){
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_16);
            }
            runOnUiThread(()->{output.append("Received ack: \n");});
        }catch (Exception e){
            e.printStackTrace();
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