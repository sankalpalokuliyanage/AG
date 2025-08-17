package com.example.agmart;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agmart.models.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtName, edtBarcode, edtPrice, edtStock;
    private Button btnSave, btnBack;
    private ProgressBar progressBar;

    private DatabaseReference productRef;

    private boolean isEditMode = false;
    private String productId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize UI
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtBarcode = findViewById(R.id.edtBarcode);
        edtStock = findViewById(R.id.edtStock);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBarUpload);

        // Firebase reference
        productRef = FirebaseDatabase.getInstance().getReference("products");

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Check if edit mode
        if (getIntent().hasExtra("id")) {
            isEditMode = true;
            productId = getIntent().getStringExtra("id");

            edtName.setText(getIntent().getStringExtra("name"));
            edtBarcode.setText(getIntent().getStringExtra("barcode"));
            edtPrice.setText(String.valueOf(getIntent().getDoubleExtra("price", 0)));
            edtStock.setText(String.valueOf(getIntent().getIntExtra("stockQty", 0)));

            btnSave.setText("Update Product");
        }

        // Save button logic
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateProduct();
                } else {
                    saveNewProduct();
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        return !edtName.getText().toString().isEmpty()
                && !edtBarcode.getText().toString().isEmpty()
                && !edtPrice.getText().toString().isEmpty()
                && !edtStock.getText().toString().isEmpty();
    }

    // Add product
    private void saveNewProduct() {
        progressBar.setVisibility(View.VISIBLE);

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
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Update product
    private void updateProduct() {
        progressBar.setVisibility(View.VISIBLE);

        Product p = new Product(
                productId,
                edtName.getText().toString(),
                edtBarcode.getText().toString(),
                Double.parseDouble(edtPrice.getText().toString()),
                Integer.parseInt(edtStock.getText().toString())
        );

        productRef.child(productId).setValue(p)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Product updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
