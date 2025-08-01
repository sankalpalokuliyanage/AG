package com.example.agmart;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.agmart.models.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtName, edtBarcode, edtPrice, edtStock;
    private Button btnSave;
    private ProgressBar progressBar;

    private static final int REQUEST_PERMISSION_CODE = 101;

    private DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        edtName = findViewById(R.id.edtName);
        edtBarcode = findViewById(R.id.edtBarcode);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBarUpload);

        productRef = FirebaseDatabase.getInstance().getReference("products");

        Button scanbarcode = findViewById(R.id.scan);
        scanbarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ScanActivity.class));
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProductToFirebase();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        return !edtName.getText().toString().isEmpty() &&
                !edtBarcode.getText().toString().isEmpty() &&
                !edtPrice.getText().toString().isEmpty() &&
                !edtStock.getText().toString().isEmpty();
    }

    private void saveProductToFirebase() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        String id = productRef.push().getKey();
        Product p = new Product(
                id,
                edtName.getText().toString(),
                edtBarcode.getText().toString(),
                Double.parseDouble(edtPrice.getText().toString()),
                Integer.parseInt(edtStock.getText().toString())
        );

        productRef.child(id).setValue(p)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
