package com.thanhvan.bookstoremanager.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.OrderConfirmationActivity;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.model.OrderItem;
import com.thanhvan.bookstoremanager.sqlite.OrderItemDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private OrderItemDao orderItemDao;
    private OnOrderActionListener listener;
    private boolean isAdminView = false; //


    public interface OnOrderActionListener {
        void onViewDetails(Order order); // Xem chi tiết hóa đơn
        void onChangeStatus(Order order, String newStatus);
    }

    // CONSTRUCTOR 1: Cho User (chỉ cần list và context, listener sẽ là null, isAdminView là false)
    public OrderHistoryAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
        this.orderItemDao = new OrderItemDao(context);
        this.listener = null;
        this.isAdminView = false; // Đây là chế độ xem của người dùng
    }

    // CONSTRUCTOR 2: Cho Admin (có listener và cờ isAdminView)
    public OrderHistoryAdapter(List<Order> orderList, Context context, OnOrderActionListener listener, boolean isAdminView) {
        this.orderList = orderList;
        this.context = context;
        this.orderItemDao = new OrderItemDao(context);
        this.listener = listener;
        this.isAdminView = isAdminView; // Đặt biến isAdminView
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_order_history_customer.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history_customer, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderCodeTextView.setText("Mã đơn hàng: " + order.getOrderCode());
        holder.orderDateTextView.setText("Ngày đặt: " + new SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault()).format(new Date(order.getOrderDate())));
        holder.totalItemsCountTextView.setText("Tổng số sản phẩm: " + order.getTotalQuantity());
        holder.totalOrderAmountTextView.setText(String.format(Locale.getDefault(), "Tổng tiền: %,.0fđ", order.getTotalAmount()));

        // HIỂN THỊ HOẶC ẨN EMAIL NGƯỜI DÙNG DỰA VÀO isAdminView
        if (isAdminView) {
            holder.userEmailTextView.setText(order.getUserEmail());
            holder.userEmailTextView.setVisibility(View.VISIBLE);
        } else {
            holder.userEmailTextView.setVisibility(View.GONE);
        }

        holder.orderStatusTextView.setText(order.getStatus());
        // Logic đổi màu nền trạng thái tùy thuộc vào màu bạn định nghĩa hoặc mã hex
        switch (order.getStatus()) {
            case "Đang xử lý":
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Đang chuẩn bị":
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Đang giao hàng":
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Đã hoàn thành":
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "Đã hủy":
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                holder.orderStatusTextView.setBackgroundResource(R.drawable.bg_status_pending);
                break;
        }

        List<OrderItem> orderItems = orderItemDao.getOrderItemsByOrderId(order.getId());
        if (!orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            holder.itemTitleTextView.setText(firstItem.getProductName());
            holder.itemQuantityPriceTextView.setText(
                    String.format(Locale.getDefault(), "%d sản phẩm - %,.0fđ",
                            firstItem.getProductQuantity(), firstItem.getProductPrice()));
            if (firstItem.getProductImageUrl() != null && !firstItem.getProductImageUrl().isEmpty()) {
                Glide.with(context).load(firstItem.getProductImageUrl()).placeholder(R.drawable.sachbia1).error(R.drawable.sachbia1).into(holder.itemThumbnailImageView);
            } else {
                holder.itemThumbnailImageView.setImageResource(R.drawable.sachbia1);
            }
            holder.layoutFirstItemPreview.setVisibility(View.VISIBLE);
        } else {
            holder.layoutFirstItemPreview.setVisibility(View.GONE);
        }

        // GỌI LISTENER KHI NHẤN NÚT XEM CHI TIẾT ĐƠN HÀNG
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(order);
            } else {

                Intent intent = new Intent(context, OrderConfirmationActivity.class);
                intent.putExtra("order_id", order.getId());
                intent.putExtra("order_code", order.getOrderCode());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderCodeTextView;
        TextView orderStatusTextView;
        TextView orderDateTextView;
        TextView userEmailTextView;
        ImageView itemThumbnailImageView;
        TextView itemTitleTextView;
        TextView itemQuantityPriceTextView;
        TextView totalItemsCountTextView;
        TextView totalOrderAmountTextView;
        Button viewDetailsButton;
        LinearLayout layoutFirstItemPreview;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderCodeTextView = itemView.findViewById(R.id.text_view_order_code);
            orderStatusTextView = itemView.findViewById(R.id.text_view_order_status);
            orderDateTextView = itemView.findViewById(R.id.text_view_order_date);
            userEmailTextView = itemView.findViewById(R.id.text_view_user_email_for_admin);
            itemThumbnailImageView = itemView.findViewById(R.id.image_view_item_thumbnail);
            itemTitleTextView = itemView.findViewById(R.id.text_view_item_title);
            itemQuantityPriceTextView = itemView.findViewById(R.id.text_view_item_quantity_price);
            totalItemsCountTextView = itemView.findViewById(R.id.text_view_total_items_count);
            totalOrderAmountTextView = itemView.findViewById(R.id.text_view_total_order_amount);
            viewDetailsButton = itemView.findViewById(R.id.button_view_order_details);
            layoutFirstItemPreview = itemView.findViewById(R.id.layout_first_item_preview);
        }
    }
}