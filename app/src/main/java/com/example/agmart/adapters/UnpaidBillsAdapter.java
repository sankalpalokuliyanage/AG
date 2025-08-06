package com.example.agmart.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.R;
import com.example.agmart.models.BillRecord;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.List;

public class UnpaidBillsAdapter extends RecyclerView.Adapter<UnpaidBillsAdapter.BillViewHolder> {

    private final List<BillRecord> billList;
    private final Context context;

    public UnpaidBillsAdapter(List<BillRecord> billList, Context context) {
        this.billList = billList;
        this.context = context;
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPhone, textPaidStatus;
        Button btnMarkPaid, btnOpenPdf;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCustomerName);
            textPhone = itemView.findViewById(R.id.textCustomerPhone);
            textPaidStatus = itemView.findViewById(R.id.textPaidStatus);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
            btnOpenPdf = itemView.findViewById(R.id.btnOpenPdf);
        }
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_unpaid_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        BillRecord bill = billList.get(position);

        holder.textName.setText(bill.customerName);
        holder.textPhone.setText(bill.customerPhone);
        holder.textPaidStatus.setText(bill.paid ? "Paid" : "Unpaid");

        holder.btnMarkPaid.setOnClickListener(v -> {
            // Update paid status in Firebase
            FirebaseDatabase.getInstance().getReference("bills")
                    .child(bill.customerPhone)
                    .child("paid")
                    .setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Marked as paid", Toast.LENGTH_SHORT).show();
                        bill.paid = true;
                        holder.textPaidStatus.setText("Paid");
                        // Optionally remove from list & notify adapter
                        billList.remove(bill);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, billList.size());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to mark paid: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        holder.btnOpenPdf.setOnClickListener(v -> {
            if (bill.pdfPath == null || bill.pdfPath.isEmpty()) {
                Toast.makeText(context, "PDF not available", Toast.LENGTH_SHORT).show();
                return;
            }
            File pdfFile = new File(bill.pdfPath);
            if (!pdfFile.exists()) {
                Toast.makeText(context, "PDF file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri pdfUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No app available to open PDF", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }
}
