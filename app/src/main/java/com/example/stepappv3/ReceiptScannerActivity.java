package com.example.stepappv3;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stepappv3.receipt.GeminiReceiptParser;

import java.io.IOException;

public class ReceiptScannerActivity extends AppCompatActivity {

    private ImageView ivReceipt;
    private TextView tvResult;
    private ProgressBar progressBar;

    private Uri imageUri;

    // Permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            });

    // Camera launcher
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ivReceipt.setImageBitmap(bitmap);
                        processWithGemini(bitmap);
                    } catch (IOException e) {
                        tvResult.setText("Error loading image: " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            ivReceipt.setImageBitmap(bitmap);
                            processWithGemini(bitmap);
                        } catch (IOException e) {
                            tvResult.setText("Error loading image: " + e.getMessage());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_scanner);

        ivReceipt = findViewById(R.id.ivReceiptPreview);
        tvResult = findViewById(R.id.tvResult);
        progressBar = findViewById(R.id.progressBar);

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());

        Button btnTestReceipt = findViewById(R.id.btnTestReceipt);
        btnTestReceipt.setOnClickListener(v -> openGallery());
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        // Insert image entry so full-size photo is saved
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "receipt_photo");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );

        if (imageUri == null) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void processWithGemini(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("Processing...");

        String apiKey = "AIzaSyCS9ARUfPowg8xjiQndSxCEmuxaFKrXQrs";
        GeminiReceiptParser parser = new GeminiReceiptParser(apiKey);

        parser.parseReceipt(bitmap)
                .thenAccept(result -> {
                    runOnUiThread(() -> {
                        tvResult.setText(result);
                        progressBar.setVisibility(View.GONE);
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        tvResult.setText("Error: " + throwable.getMessage());
                        progressBar.setVisibility(View.GONE);
                    });
                    return null;
                });
    }
}