package com.example.file_creating;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    EditText folderName;
    Button createFolder;
    String FolderName;
    private  static final int PERMISSION_REQUEST_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        folderName = findViewById(R.id.folderName);
        createFolder = findViewById(R.id.createButton);
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FolderName = folderName.getText().toString().trim();
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                    createDirectory(FolderName);

                }else
                {

                    askPermission();
                }

            }
        });

    }

    private void askPermission() {

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                createDirectory(FolderName);
            }else
            {
                Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createDirectory(String folderName) {

        File file = new File(Environment.getExternalStorageDirectory(),folderName);

        if (!file.exists()){

            file.mkdir();

            Toast.makeText(MainActivity.this,"Successful",Toast.LENGTH_SHORT).show();
        }else
        {

            Toast.makeText(MainActivity.this,"Folder Already Exists",Toast.LENGTH_SHORT).show();


        }


    }
}