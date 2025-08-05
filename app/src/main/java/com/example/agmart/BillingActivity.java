package com.example.agmart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {

    private List<Product> billItems = new ArrayList<>();
    private ProductAdapter adapter;
    private TextView totalText;
    private static final int STORAGE_PERMISSION_CODE = 1001;

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

        // Example: manually adding products for demo
        billItems.add(new Product("1", "Apple", 1000, 2));
        billItems.add(new Product("2", "Banana", 500, 3));
        adapter.notifyDataSetChanged();
        updateTotal();

        Button generateBtn = findViewById(R.id.generatePdfBtn);
        generateBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                generatePDF();
            }
        });
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
            File pdfFolder = new File(Environment.getExternalStorageDirectory(), "BillingPDFs");
            if (!pdfFolder.exists()) pdfFolder.mkdirs();

            String filename = "bill_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(pdfFolder, filename);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(new Paragraph("EDUGATE - BILL RECEIPT\n\n"));
            for (Product p : billItems) {
                document.add(new Paragraph(p.name + " x" + p.quantity + " - KRW " + (p.price * p.quantity)));
            }
            document.add(new Paragraph("\n" + totalText.getText()));
            document.close();

            // Save to SQLite
            dbHelper.insertBill(pdfFile.getAbsolutePath());

            Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePDF();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
