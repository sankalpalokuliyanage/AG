package com.example.agmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.adapters.ProductAdapter;
import com.example.agmart.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Product> productList;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        adapter = new ProductAdapter(productList, this, false, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Example action: Show product name toast or navigate to details
                Toast.makeText(ProductManagementActivity.this, "Clicked: " + product.name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onQuantityChanged(Product product, int newQuantity) {
                // Not needed in product management screen (not cart)
            }
        });

        recyclerView.setAdapter(adapter);

        loadProducts();

        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(ProductManagementActivity.this, AddProductActivity.class));
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Product p = data.getValue(Product.class);
                            if (p != null) {
                                productList.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProductManagementActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
