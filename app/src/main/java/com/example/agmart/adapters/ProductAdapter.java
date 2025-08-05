package com.example.agmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.R;
import com.example.agmart.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onQuantityChanged(Product product, int newQuantity);
    }

    private final List<Product> productList;
    private final Context context;
    private final boolean isCartMode;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> productList, Context context, boolean isCartMode, OnProductClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.isCartMode = isCartMode;
        this.listener = listener;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textQuantity;
        Button btnAdd, btnPlus, btnMinus;

        public ProductViewHolder(@NonNull View itemView, boolean isCartMode) {
            super(itemView);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textProductPrice);
            if (isCartMode) {
                textQuantity = itemView.findViewById(R.id.textQuantity);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnMinus = itemView.findViewById(R.id.btnMinus);
            } else {
                btnAdd = itemView.findViewById(R.id.btnAdd);
            }
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isCartMode) {
            view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_product_list, parent, false);
        }
        return new ProductViewHolder(view, isCartMode);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textName.setText(product.name);
        holder.textPrice.setText("KRW " + product.price);

        if (isCartMode) {
            holder.textQuantity.setText(String.valueOf(product.quantity));

            holder.btnPlus.setOnClickListener(v -> {
                int newQty = product.quantity + 1;
                product.quantity = newQty;
                holder.textQuantity.setText(String.valueOf(newQty));
                if (listener != null) listener.onQuantityChanged(product, newQty);
            });

            holder.btnMinus.setOnClickListener(v -> {
                if (product.quantity > 1) {
                    int newQty = product.quantity - 1;
                    product.quantity = newQty;
                    holder.textQuantity.setText(String.valueOf(newQty));
                    if (listener != null) listener.onQuantityChanged(product, newQty);
                }
            });

        } else {
            holder.btnAdd.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
