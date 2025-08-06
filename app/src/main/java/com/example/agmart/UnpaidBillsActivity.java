package com.example.agmart;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.adapters.UnpaidBillsAdapter;
import com.example.agmart.models.BillRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UnpaidBillsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UnpaidBillsAdapter adapter;
    private List<BillRecord> unpaidBills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unpaid_bills);

        recyclerView = findViewById(R.id.recyclerViewUnpaid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UnpaidBillsAdapter(unpaidBills, this);
        recyclerView.setAdapter(adapter);

        loadUnpaidBills();

    }


    private void loadUnpaidBills() {
        FirebaseDatabase.getInstance().getReference("bills")
                .orderByChild("paid")
                .equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        unpaidBills.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BillRecord record = ds.getValue(BillRecord.class);
                            if (record != null) unpaidBills.add(record);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UnpaidBillsActivity.this, "Failed to load unpaid bills", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}