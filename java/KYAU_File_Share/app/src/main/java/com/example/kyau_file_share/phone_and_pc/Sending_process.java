package com.example.kyau_file_share.phone_and_pc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.kyau_file_share.R;
import com.example.kyau_file_share.Singleton;

import java.net.Socket;
import java.util.ArrayList;
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
            Toast.makeText(this, "All Files Send.", Toast.LENGTH_SHORT).show();
            return;
        }
        is_sending = true;
        Uri fileUri = fileQueue.poll();
        sendFile(fileUri);
    }

    private void sendFile(Uri uri){
        output.append("Sending file: " + uri.toString() + " \n");
        is_sending = false;
        processQueue();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
        filePickerLauncher.launch(intent);
    }
}