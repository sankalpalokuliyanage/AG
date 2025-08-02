package com.example.agmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.R;
import com.example.agmart.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<Product> productList;
    private final Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textProductPrice);
            textStock = itemView.findViewById(R.id.textProductStock);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textName.setText(product.name);
        holder.textPrice.setText("KRW " + product.price);
        holder.textStock.setText("Stock: " + product.stockQty);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
