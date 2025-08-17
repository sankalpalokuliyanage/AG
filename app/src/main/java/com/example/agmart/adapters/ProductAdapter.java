package com.example.agmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agmart.R;
import com.example.agmart.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onQuantityChanged(Product product, int newQuantity);
        void onDeleteProduct(Product product);
    }



    private final List<Product> fullList;      // all products, never changed
    private final List<Product> productList;   // filtered/displayed list
    private Context context;

    private OnProductClickListener listener;


    private final boolean isCartMode;
    private final int itemLayoutResId;





    public ProductAdapter(List<Product> productList, Context context, boolean isCartMode, int itemLayoutResId, OnProductClickListener listener) {
        this.fullList = new ArrayList<>(productList);
        this.productList = productList;
        this.context = context;
        this.isCartMode = isCartMode;
        this.itemLayoutResId = itemLayoutResId;
        this.listener = listener;
    }


    // Getter for full list (needed for filtering)
    public List<Product> getFullList() {
        return fullList;
    }

    // Update the displayed list and notify
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }


    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textStock, textCode;

        EditText textQuantity;
        Button btnAdd, btnPlus, btnMinus, btnDelete, btnEdit;

        public ProductViewHolder(@NonNull View itemView, boolean isCartMode) {
            super(itemView);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textProductPrice);
            textQuantity = itemView.findViewById(R.id.editQuantity);
            textStock = itemView.findViewById(R.id.textProductStock); // Only exists in item_product.xml
            textCode = itemView.findViewById(R.id.textProductCode);

            if (isCartMode) {

                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            } else {
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
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
        holder.textCode.setText(product.barcode);

        if (!isCartMode) {
            if (holder.textStock != null) {
                holder.textStock.setText("Stock: " + product.stockQty);
            }
            if (holder.btnAdd != null) {
                holder.btnAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductClick(product);
                    }
                });
            }
            if (holder.btnEdit != null) {
                holder.btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductClick(product); // reuse as "edit"
                    }
                });
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteProduct(product);
                    }
                });
            }
        } else {
            // Cart mode: show quantity and handle buttons
            if (holder.textQuantity != null) {
                holder.textQuantity.setText(String.valueOf(product.quantity));
            }

            if (holder.btnPlus != null) {
                holder.btnPlus.setOnClickListener(v -> {
                    int newQuantity = product.quantity + 1;
                    product.quantity = newQuantity;
                    if (holder.textQuantity != null) {
                        holder.textQuantity.setText(String.valueOf(newQuantity));
                    }
                    if (listener != null) {
                        listener.onQuantityChanged(product, newQuantity);
                    }
                });
            }

            if (holder.btnMinus != null) {
                holder.btnMinus.setOnClickListener(v -> {
                    if (product.quantity > 1) {
                        int newQuantity = product.quantity - 1;
                        product.quantity = newQuantity;
                        if (holder.textQuantity != null) {
                            holder.textQuantity.setText(String.valueOf(newQuantity));
                        }
                        if (listener != null) {
                            listener.onQuantityChanged(product, newQuantity);
                        }
                    }
                });
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteProduct(product);
                    }
                });
            }

        }
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }


}
