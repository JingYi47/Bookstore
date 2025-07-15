package com.thanhvan.bookstoremanager.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.model.OrderItem;
import com.thanhvan.bookstoremanager.sqlite.OrderItemDao;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private OnOrderActionListener listener;
    private OrderItemDao orderItemDao;


    public interface OnOrderActionListener {
        void onTrackOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.orderItemDao = new OrderItemDao(context);
        this.orderItemDao.open();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvUserEmail.setText(order.getUserEmail());
        holder.tvOrderCode.setText(order.getOrderCode());

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.tvTotalAmount.setText(formatter.format(order.getTotalAmount()) + "vnd");
        holder.tvTotalQuantity.setText("(" + order.getTotalQuantity() + " sản phẩm)");

        List<OrderItem> orderItems = orderItemDao.getOrderItemsByOrderId(order.getId());
        if (!orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            holder.tvFirstProductName.setText(firstItem.getProductName());
            Glide.with(context)
                    .load(firstItem.getProductImageUrl())
                    .placeholder(R.drawable.sachbia1)
                    .error(R.drawable.eror)
                    .into(holder.ivFirstProductImage);
        } else {
            holder.tvFirstProductName.setText("Không có sản phẩm");
            holder.ivFirstProductImage.setImageResource(R.drawable.sachbia1);
        }

        holder.btnTrackOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackOrderClick(order);
            }
        });

        if ("Đang xử lý".equals(order.getStatus())) {
            holder.btnTrackOrder.setText("Theo dõi đơn hàng");
            holder.btnTrackOrder.setVisibility(View.VISIBLE);
        } else if ("Hoàn thành".equals(order.getStatus())) {
            holder.btnTrackOrder.setText("Xem chi tiết");
            holder.btnTrackOrder.setVisibility(View.VISIBLE);
        } else {
            holder.btnTrackOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvTotalAmount, tvOrderCode, tvTotalQuantity, tvFirstProductName;
        ImageView ivFirstProductImage;
        Button btnTrackOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvOrderCode = itemView.findViewById(R.id.tv_order_code);
            tvTotalQuantity = itemView.findViewById(R.id.tv_total_quantity);
            tvFirstProductName = itemView.findViewById(R.id.tv_first_product_name);
            ivFirstProductImage = itemView.findViewById(R.id.iv_first_product_image);
            btnTrackOrder = itemView.findViewById(R.id.btn_track_order);
        }
    }

    public void closeDao() {
        if (orderItemDao != null) {
            orderItemDao.close();
        }
    }
}