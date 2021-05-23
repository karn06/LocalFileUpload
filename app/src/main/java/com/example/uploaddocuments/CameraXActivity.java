package com.example.uploaddocuments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class CameraXActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_GET_SELFIE = 1888;
    public static final String EXTRA_SELFIE = "extra-selfie";
    public static final String EXTRA_SELFIE_STATUS = "extra-selfie-status";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    TextView butCapture;
    TextView textView;
    public static final String INTENT_KEY_FRONT = "front";

    //private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_x);
        previewView = findViewById(R.id.preview_view);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        butCapture = findViewById(R.id.but_click);
        butCapture.setOnClickListener(v -> onClick());
        textView = findViewById(R.id.tv_no_permissions);


        AndPermission.with(CameraXActivity.this)
                .runtime()
                .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    textView.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);
                    butCapture.setVisibility(View.VISIBLE);
                    cameraProviderFuture.addListener(() -> {
                        try {
                            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                            bindPreview(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                            // No errors need to be handled for this Future
                            // This should never be reached
                        }
                    }, ContextCompat.getMainExecutor(this));
                })
                .onDenied(permissions -> {
                    // At least one permission is denied
                    textView.setVisibility(View.VISIBLE);
                    butCapture.setVisibility(View.GONE);
                    previewView.setVisibility(View.GONE);
                    Toast.makeText(this, "You Need To Provide Necessary Permissions First", Toast.LENGTH_SHORT).show();
                    this.finish();
                })
                .start();

    }


    private void onClick() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (image == null || imageCapture == null)
            this.finish();

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(image)
                .build();
        butCapture.setEnabled(false);
        File finalImage = image;
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(CameraXActivity.this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent data = new Intent();
                data.putExtra(EXTRA_SELFIE, finalImage.getPath());
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraXActivity.this, "Error Saving Image", Toast.LENGTH_SHORT).show();
                butCapture.setEnabled(true);
            }
        });
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetName("Preview")
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        //FIXME LENS_FACING_BACK does not work for emulator
        boolean showFrontCamera = getIntent().getBooleanExtra(INTENT_KEY_FRONT, false);
        CameraSelector cameraSelector =
                new CameraSelector.Builder()
                        .requireLensFacing(showFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                        .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

}
