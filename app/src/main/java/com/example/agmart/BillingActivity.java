package com.example.agmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.adapters.ProductAdapter;
import com.example.agmart.database.BillDatabaseHelper;
import com.example.agmart.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1001;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> cartItems = new ArrayList<>();

    private ProductAdapter productsAdapter, cartAdapter;
    private TextView totalText;
    private BillDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        dbHelper = new BillDatabaseHelper(this);

        RecyclerView recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        RecyclerView recyclerViewCart = findViewById(R.id.recyclerViewCart);
        totalText = findViewById(R.id.textTotal);
        Button generateBtn = findViewById(R.id.generatePdfBtn);

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Correct: Using anonymous class for productsAdapter listener
        productsAdapter = new ProductAdapter(allProducts, this, false, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                addToCart(product);
            }

            @Override
            public void onQuantityChanged(Product product, int newQuantity) {
                // Not used here
            }
        });
        recyclerViewProducts.setAdapter(productsAdapter);

        cartAdapter = new ProductAdapter(cartItems, this, true, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Not used in cart mode
            }

            @Override
            public void onQuantityChanged(Product product, int newQuantity) {
                if (newQuantity <= 0) {
                    cartItems.remove(product);
                }
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }
        });
        recyclerViewCart.setAdapter(cartAdapter);

        loadProductsFromFirebase();

        generateBtn.setOnClickListener(v -> {
            if (needsStoragePermission()) {
                requestStoragePermission();
            } else {
                generatePDF();
            }
        });
    }

    private void addToCart(Product product) {
        for (Product p : cartItems) {
            if (p.id.equals(product.id)) {
                p.quantity++;
                cartAdapter.notifyDataSetChanged();
                updateTotal();
                return;
            }
        }
        Product newProduct = new Product(product.id, product.name, product.barcode, product.price, product.stockQty);
        newProduct.quantity = 1;
        cartItems.add(newProduct);
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }

    private void loadProductsFromFirebase() {
        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allProducts.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Product product = ds.getValue(Product.class);
                            if (product != null) {
                                allProducts.add(product);
                            }
                        }
                        productsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BillingActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotal() {
        double total = 0;
        for (Product p : cartItems) {
            total += p.price * p.quantity;
        }
        totalText.setText("Total: KRW " + total);
    }

    private boolean needsStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePDF();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePDF() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File pdfFolder = new File(downloadsFolder, "EDUGATE_Bills");
            if (!pdfFolder.exists()) pdfFolder.mkdirs();

            File pdfFile = new File(pdfFolder, "bill_" + System.currentTimeMillis() + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(new Paragraph("EDUGATE - BILL RECEIPT\n\n"));
            for (Product p : cartItems) {
                document.add(new Paragraph(p.name + " x" + p.quantity + " - KRW " + (p.price * p.quantity)));
            }
            document.add(new Paragraph("\n" + totalText.getText()));
            document.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(pdfFile));
            sendBroadcast(intent);

            dbHelper.insertBill(pdfFile.getAbsolutePath());

            Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Clear cart after saving
            cartItems.clear();
            cartAdapter.notifyDataSetChanged();
            updateTotal();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
