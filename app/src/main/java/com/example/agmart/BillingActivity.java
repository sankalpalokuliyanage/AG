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

    private List<Product> billItems = new ArrayList<>();
    private ProductAdapter adapter;
    private TextView totalText;
    private BillDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        dbHelper = new BillDatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBill);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(billItems, this);
        recyclerView.setAdapter(adapter);

        totalText = findViewById(R.id.textTotal);

        loadProductsFromFirebase();  // Load products from Firebase realtime database

        Button generateBtn = findViewById(R.id.generatePdfBtn);
        generateBtn.setOnClickListener(v -> {
            if (needsStoragePermission()) {
                requestStoragePermission();
            } else {
                generatePDF();
            }
        });
    }

    private void loadProductsFromFirebase() {
        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        billItems.clear();
                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null) {
                                // Set default quantity to 1
                                product.quantity = 1;
                                billItems.add(product);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateTotal();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BillingActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean needsStoragePermission() {
        // For Android 9 (API 28) and below, request permission
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

    private void updateTotal() {
        double total = 0;
        for (Product p : billItems) {
            total += p.price * p.quantity;
        }
        totalText.setText("Total: KRW " + total);
    }

    private void generatePDF() {
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
            for (Product p : billItems) {
                document.add(new Paragraph(p.name + " x" + p.quantity + " - KRW " + (p.price * p.quantity)));
            }
            document.add(new Paragraph("\n" + totalText.getText()));
            document.close();

            // Notify media scanner so file is visible immediately
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(pdfFile));
            sendBroadcast(intent);

            // Save PDF path to SQLite database
            dbHelper.insertBill(pdfFile.getAbsolutePath());

            Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
