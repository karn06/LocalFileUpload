package com.example.uploaddocuments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {


    Button button;
    String[] requiredPermissionn = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        requestPermission();
        button.setOnClickListener(v -> {
            CallFunction callFunction = new CallFunction();
            callFunction.show(getSupportFragmentManager(), "CallFunction");
        });

    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                   requiredPermissionn,
                    Constats.MY_STORAGE_PERMISSIONS_REQUEST);

        }
    }
}