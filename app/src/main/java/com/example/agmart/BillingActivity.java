package com.example.agmart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.adapters.ProductAdapter;
import com.example.agmart.models.Product;

import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {
    List<Product> billItems = new ArrayList<>();
    ProductAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBill);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(billItems, this);
        recyclerView.setAdapter(adapter);

        // Get product from intent
        String name = getIntent().getStringExtra("product_name");
        double price = getIntent().getDoubleExtra("product_price", 0.0);
        int stock = getIntent().getIntExtra("product_stock", 0);





        Button generateBtn = findViewById(R.id.generatePdfBtn);
        generateBtn.setOnClickListener(v -> {
            // TODO: implement PDF generation
        });
    }
}
