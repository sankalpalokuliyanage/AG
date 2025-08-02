package com.example.agmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

public class MainActivity extends AppCompatActivity {



    private TextView totalProductsCount;
    private CardView cardProductManagement, cardBillingSystem, cardInventoryControl, cardSalesReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        totalProductsCount = findViewById(R.id.totalProductsCount);

        // Card Views
        cardProductManagement = findViewById(R.id.cardProductManagement);
        cardBillingSystem = findViewById(R.id.cardBillingSystem);
        cardInventoryControl = findViewById(R.id.cardInventoryControl);
        cardSalesReport = findViewById(R.id.cardSalesReport);

        // Fetch product count from Firebase
        FirebaseDatabase.getInstance().getReference("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        totalProductsCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error loading product count", Toast.LENGTH_SHORT).show();
                    }
                });

        // Navigation handlers
        cardProductManagement.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProductManagementActivity.class)));

//        cardBillingSystem.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, BillingActivity.class)));
//
//        cardInventoryControl.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, InventoryActivity.class)));
//
//        cardSalesReport.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, SalesReportActivity.class)));
    }
}