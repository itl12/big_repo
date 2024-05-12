package com.example.file_creating;




import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Request code for permission
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    // Declare the button and text view
    private Button button;
    private TextView textView;

    @SuppressLint("MissingInflatedId")
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

        // Initialize the button and text view
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        // Set a click listener for the button
        button.setOnClickListener(v -> {
            // Check if we have permission to write to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else {
                writeFile();
            }


        });


    }// End of onCreate()




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the request code matches our request code
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, write the file
                writeFile();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeFile(){
        if (isExternalStorageWritable() && checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Create a new directory
            File newDir = new File(Environment.getExternalStorageDirectory() + "/AAAAA");
            if (!newDir.exists()) {
                if (newDir.mkdirs()) {
                    textView.append("Folder created successfully");
                } else {
                    textView.append("Failed to create folder");
                }
            } else {
                textView.append("Folder already exists");
            }



            // Create a new file
            File file = new File(Environment.getExternalStorageDirectory(), "/AAAAA/test.txt");
            try
            {
                FileWriter writer = new FileWriter(file);

                writer.write("Hello, fucking World!\n");

                writer.close();

                textView.append("File written successfully\n");

            } catch (IOException e) {

                e.printStackTrace();

                textView.append("Error\n"+ e.getMessage());
            }
        }else{
            textView.append("\nExternal storage is not writable \n");
        }
    }

    public boolean checkPermission(String permission){
        int permissionCheck = this.checkSelfPermission(permission);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }


}