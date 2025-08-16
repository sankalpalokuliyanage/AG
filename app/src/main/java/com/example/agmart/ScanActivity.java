package com.example.agmart;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.agmart.models.Product;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.*;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class ScanActivity extends AppCompatActivity {

    private PreviewView previewView;
    private final BarcodeScanner scanner = BarcodeScanning.getClient();
    private boolean isScanned = false; // ✅ Prevent multiple triggers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        previewView = findViewById(R.id.previewView);

        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    Image mediaImage = imageProxy.getImage();

                    if (mediaImage != null && !isScanned) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                        scanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        String rawValue = barcode.getRawValue();
                                        if (rawValue != null && !isScanned) {
                                            isScanned = true; // ✅ block multiple scans
                                            Log.d("Scanner", "Barcode scanned: " + rawValue);
                                            fetchProductFromFirebase(rawValue);
                                            break;
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Scanner", "Error scanning barcode", e);
                                    imageProxy.close();
                                })
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void fetchProductFromFirebase(String barcode) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("products");

        dbRef.orderByChild("barcode").equalTo(barcode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot productSnap : snapshot.getChildren()) {
                                Product product = productSnap.getValue(Product.class);
                                if (product != null) {
                                    Intent intent = new Intent(ScanActivity.this, BillingActivity.class);
                                    intent.putExtra("product_name", product.name);
                                    intent.putExtra("product_price", product.price);
                                    intent.putExtra("product_stock", product.stockQty);

                                    startActivity(intent);
                                    finish();
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(ScanActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                            isScanned = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ScanActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error: " + error.getMessage());
                        isScanned = false;
                    }
                });
    }

}
