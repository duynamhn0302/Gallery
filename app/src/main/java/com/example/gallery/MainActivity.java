package com.example.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    private String [] REQUIRE_PERMISSIONS = new String []{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    PreviewView previewView;
    CameraSelector cameraSelector;
    Preview preview;
    boolean back = true;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ImageView gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
        cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        ImageButton switch_camera = findViewById(R.id.switch_camera);
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (back){
                    cameraSelector =
                            new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
                    back = false;
                    setCameraProviderListener();
                }
                else {
                    cameraSelector =
                            new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    back = true;
                    setCameraProviderListener();
                }
            }
        });
        previewView = findViewById(R.id.preview_view);
        setCameraProviderListener();

    }
    private void setCameraProviderListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future
                // This should never be reached
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void bindPreview(ProcessCameraProvider cameraProvider) {

        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).
                setTargetRotation(previewView.getDisplay().getRotation())
                .build();


        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector,preview);


    }


}
