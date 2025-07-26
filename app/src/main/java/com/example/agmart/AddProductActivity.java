package com.example.agmart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agmart.models.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;

import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtName, edtBarcode, edtPrice, edtStock;
    private ImageView imgProduct;
    private Button btnSave;
    private Uri imageUri;
    private ProgressBar progressBar;

    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference productRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        edtName = findViewById(R.id.edtName);
        edtBarcode = findViewById(R.id.edtBarcode);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        imgProduct = findViewById(R.id.imgProduct);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBarUpload);

        productRef = FirebaseDatabase.getInstance().getReference("products");
        storageRef = FirebaseStorage.getInstance().getReference("product_images");

        imgProduct.setOnClickListener(v -> chooseImage());

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadImageAndSaveProduct();
            }
        });
    }

    private boolean validateInputs() {
        if (edtName.getText().toString().isEmpty() ||
                edtBarcode.getText().toString().isEmpty() ||
                edtPrice.getText().toString().isEmpty() ||
                edtStock.getText().toString().isEmpty() ||
                imageUri == null) {

            Toast.makeText(this, "Fill all fields and select image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProduct.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSaveProduct() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        final String imageName = UUID.randomUUID().toString();
        StorageReference imgRef = storageRef.child(imageName);

        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveProduct(uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProduct(String imageUrl) {
        String id = productRef.push().getKey();
        Product p = new Product(
                id,
                edtName.getText().toString(),
                edtBarcode.getText().toString(),
                Double.parseDouble(edtPrice.getText().toString()),
                Integer.parseInt(edtStock.getText().toString()),
                imageUrl
        );

        productRef.child(id).setValue(p)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
                    finish(); // go back
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
