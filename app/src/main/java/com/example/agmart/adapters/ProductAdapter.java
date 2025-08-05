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
        void onDeleteProduct(Product product);
    }

    private final List<Product> productList;
    private final Context context;
    private final boolean isCartMode;
    private final int itemLayoutResId;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> productList, Context context, boolean isCartMode, int itemLayoutResId, OnProductClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.isCartMode = isCartMode;
        this.itemLayoutResId = itemLayoutResId;
        this.listener = listener;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textQuantity, textStock;
        Button btnAdd, btnPlus, btnMinus, btnDelete;

        public ProductViewHolder(@NonNull View itemView, boolean isCartMode) {
            super(itemView);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textProductPrice);
            textStock = itemView.findViewById(R.id.textProductStock); // Only exists in item_product.xml

            if (isCartMode) {
                textQuantity = itemView.findViewById(R.id.textQuantity);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            } else {
                btnAdd = itemView.findViewById(R.id.btnAdd);
            }
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(itemLayoutResId, parent, false);
        return new ProductViewHolder(view, isCartMode);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textName.setText(product.name);
        holder.textPrice.setText("KRW " + product.price);

        if (!isCartMode) {
            if (holder.textStock != null) {
                holder.textStock.setText("Stock: " + product.stockQty);
            }
            if (holder.btnAdd != null) {
                holder.btnAdd.setOnClickListener(v -> {
                    if (listener != null) listener.onProductClick(product);
                });
            }
        } else {
            // Cart mode logic
            if (holder.textQuantity != null) {
                holder.textQuantity.setText(String.valueOf(product.quantity));
            }

            if (holder.btnPlus != null) {
                holder.btnPlus.setOnClickListener(v -> {
                    int newQty = product.quantity + 1;
                    product.quantity = newQty;
                    holder.textQuantity.setText(String.valueOf(newQty));
                    if (listener != null) listener.onQuantityChanged(product, newQty);
                });
            }

            if (holder.btnMinus != null) {
                holder.btnMinus.setOnClickListener(v -> {
                    if (product.quantity > 1) {
                        int newQty = product.quantity - 1;
                        product.quantity = newQty;
                        holder.textQuantity.setText(String.valueOf(newQty));
                        if (listener != null) listener.onQuantityChanged(product, newQty);
                    }
                });
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteProduct(product);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
