package com.thanhvan.bookstoremanager.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.CartItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartList;
    private Context context;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onQuantityChange(int position, int newQuantity);
        void onDeleteItem(int position);
        void onEditItem(int position);
    }

    public CartAdapter(List<CartItem> cartList, OnItemActionListener listener, Context context) {
        this.cartList = cartList;
        this.listener = listener;
        this.context = context;
    }

    public void updateList(List<CartItem> newList) {
        this.cartList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvTitle.setText(item.getProductTitle());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(currencyFormat.format(item.getProductPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getProductQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.sachbia1)
                .into(holder.ivProduct);

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteItem(holder.getAdapterPosition());
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                int newQuantity = item.getProductQuantity() + 1;
                listener.onQuantityChange(holder.getAdapterPosition(), newQuantity);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null && item.getProductQuantity() > 1) {
                int newQuantity = item.getProductQuantity() - 1;
                listener.onQuantityChange(holder.getAdapterPosition(), newQuantity);
            }
        });

        // Giữ lại listener này nếu bạn có dùng trong layout
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditItem(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivDelete; // <-- ivDelete ở đây
        TextView tvTitle, tvPrice, tvQuantity;
        View btnDecrease, btnIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.image_view_cart_product);
            ivDelete = itemView.findViewById(R.id.image_view_delete_cart_item);
            tvTitle = itemView.findViewById(R.id.text_view_cart_product_title);
            tvPrice = itemView.findViewById(R.id.text_view_cart_product_price);
            tvQuantity = itemView.findViewById(R.id.text_view_cart_quantity);
            btnDecrease = itemView.findViewById(R.id.button_decrease_cart_quantity);
            btnIncrease = itemView.findViewById(R.id.button_increase_cart_quantity);
        }
    }
}