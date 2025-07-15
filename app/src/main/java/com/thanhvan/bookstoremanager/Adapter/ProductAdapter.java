package com.thanhvan.bookstoremanager.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.CategoryDao;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnItemActionListener listener;
    private Context context;
    private CategoryDao categoryDao;
    private int itemLayoutResId;

    public interface OnItemActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onItemClick(int position);
    }

    public ProductAdapter(Context context, int itemLayoutResId, List<Product> productList, OnItemActionListener listener) {
        this.context = context;
        this.itemLayoutResId = itemLayoutResId;
        this.productList = productList;
        this.listener = listener;
        this.categoryDao = new CategoryDao(context);
        this.categoryDao.open();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getTitle());
        holder.tvProductCategory.setText("Thể loại: " + product.getCategory());
        holder.tvProductRating.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));
        holder.tvCurrentPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getPrice()));
        holder.tvOriginalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getOriginalPrice()));
        holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.sachbia1)
                    .error(R.drawable.sachbia1)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.sachbia1);
        }

        if (holder.ivEditProduct != null) {
            holder.ivEditProduct.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(position);
                }
            });
        }
        if (holder.ivDeleteProduct != null) {
            holder.ivDeleteProduct.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivEditProduct, ivDeleteProduct;
        TextView tvProductName, tvProductCategory, tvProductRating, tvCurrentPrice, tvOriginalPrice, tvProductFeatured;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCategory = itemView.findViewById(R.id.tv_product_category);
            tvProductRating = itemView.findViewById(R.id.tv_product_rating);
            tvCurrentPrice = itemView.findViewById(R.id.tv_current_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvProductFeatured = itemView.findViewById(R.id.tv_product_featured);
            ivEditProduct = itemView.findViewById(R.id.iv_edit_product);
            ivDeleteProduct = itemView.findViewById(R.id.iv_delete_product);
        }
    }

    public void closeCategoryDao() {
        if (categoryDao != null) {
            categoryDao.close();
        }
    }
}