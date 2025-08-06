package com.example.agmart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.FileProvider;

public class BillingActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1001;

    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> cartItems = new ArrayList<>();

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
        EditText editTextSearch = findViewById(R.id.editTextSearch);

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapters with full product list and cart
        productsAdapter = new ProductAdapter(allProducts, this, false, R.layout.item_product_list, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                addToCart(product);
            }

            @Override
            public void onQuantityChanged(Product product, int newQuantity) {
                // Not used in product list
            }

            @Override
            public void onDeleteProduct(Product product) {
                // No delete in product list
            }
        });
        recyclerViewProducts.setAdapter(productsAdapter);

        cartAdapter = new ProductAdapter(cartItems, this, true, R.layout.item_cart_product, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Not used in cart
            }

            @Override
            public void onQuantityChanged(Product product, int newQuantity) {
                if (newQuantity <= 0) {
                    cartItems.remove(product);
                }
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDeleteProduct(Product product) {
                cartItems.remove(product);
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }
        });
        recyclerViewCart.setAdapter(cartAdapter);

        // Load products from Firebase
        loadProductsFromFirebase();

        // Setup search filter
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productsAdapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Generate PDF button click
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
                        productsAdapter.updateFullProductList(allProducts);
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
        totalText.setText("KRW " + total);
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

    private void openPdf(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    "com.example.agmart.fileprovider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No app available to open PDF", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void printPDF(File pdfFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            try {
                PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(this, pdfFile.getAbsolutePath());
                printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error printing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Printing not supported on this device", Toast.LENGTH_LONG).show();
        }
    }

    private void generatePDF() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFolder = new File(downloadsFolder, "Bills");
            if (!pdfFolder.exists()) pdfFolder.mkdirs();

            File pdfFile = new File(pdfFolder, "bill_" + System.currentTimeMillis() + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Load Sinhala font (from assets)
            BaseFont sinhalaBaseFont = BaseFont.createFont("assets/fonts/NotoSansSinhala-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font sinhalaFont = new Font(sinhalaBaseFont, 12);
            Font sinhalaBoldFont = new Font(sinhalaBaseFont, 14, Font.BOLD);

            // Add logo image
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("AG MART BILL RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n")); // Spacer

            // Create table: Item, Qty, Price, Subtotal
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{3f, 1f, 2f, 2f});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Item", "Qty", "Price", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Table data rows
            for (Product p : cartItems) {
                table.addCell(new PdfPCell(new Phrase(p.name, sinhalaFont)));

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(p.quantity), headerFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase("₩ " + p.price, headerFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);

                PdfPCell subtotalCell = new PdfPCell(new Phrase("₩ " + (p.price * p.quantity), headerFont));
                subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(subtotalCell);
            }

            document.add(table);

            // Total
            Paragraph total = new Paragraph("Total: " + totalText.getText(), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Footer message
            Paragraph thanks = new Paragraph("\nThank You and Come Again", titleFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            // Store info
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Paragraph storeInfo = new Paragraph(
                    "AG MART\nHealth Food Shop\nPhone: 010-7348-0850\n\n", infoFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            // Developer info
            Font devFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            Paragraph devInfo = new Paragraph(
                    "Developed by: Sankalpa Lokuliyanage\nContact: 010-4832-0845", devFont);
            devInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(devInfo);

            document.close();

            // Notify media scanner
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(pdfFile));
            sendBroadcast(intent);

            // Save path in DB
            dbHelper.insertBill(pdfFile.getAbsolutePath());

            Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            openPdf(pdfFile); // Open PDF immediately
            printPDF(pdfFile);

            // Clear cart
            cartItems.clear();
            cartAdapter.notifyDataSetChanged();
            updateTotal();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
