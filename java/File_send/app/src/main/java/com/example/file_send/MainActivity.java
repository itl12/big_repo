package com.example.file_send;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private Handler mainHandler;
    private Button button;
    private TextView textView;

    private ActivityResultLauncher<Intent> filePickerLauncher;

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
        mainHandler = new Handler(Looper.getMainLooper());
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        String path;
        button.setOnClickListener(v -> openFilePicker());

        // Initialize the file picker launcher
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    // Get the URI(s) of the selected file(s)
                    List<Uri> uris = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            uris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        uris.add(result.getData().getData());
                    }

                    // Display the path(s) in the TextView
                    for (Uri uri : uris) {
                        textView.setText(uri.toString() + "\n");
                        ContentResolver contentResolver = getContentResolver();

                        try {
                            InputStream inputStream = contentResolver.openInputStream(uri);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            File file = new File(uri.getPath());
                            String fileName = file.getName();
                            System.out.println("File Name: " + fileName);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                // Process the line of text
                                System.out.println(line);
                            }
                        } catch (FileNotFoundException e) {
                            // Handle the exception
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {
                Toast.makeText(this, "File selection cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
        filePickerLauncher.launch(intent);
    }
}
