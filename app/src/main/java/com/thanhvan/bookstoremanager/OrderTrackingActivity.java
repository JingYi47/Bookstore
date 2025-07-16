package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.model.OrderItem;
import com.thanhvan.bookstoremanager.sqlite.OrderDao;
import com.thanhvan.bookstoremanager.sqlite.OrderItemDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderTrackingActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView orderCodeTextView;
    private TextView orderDateTextView;
    private LinearLayout firstItemPreviewLayout;
    private ImageView itemThumbnailImageView;
    private TextView itemTitleTextView;
    private TextView itemQuantityPriceTextView;
    private Button viewInvoiceButton;
    private Button receiveOrderButton;

    private OrderDao orderDao;
    private OrderItemDao orderItemDao;
    private int currentOrderId = -1;
    private String currentOrderCode = null;
    private String currentOrderStatus = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderDao = new OrderDao(this);
        orderItemDao = new OrderItemDao(this);

        initViews();
        setupListeners();

        Intent intent = getIntent();
        if (intent != null) {
            currentOrderId = intent.getIntExtra("order_id", -1);
            currentOrderCode = intent.getStringExtra("order_code");

            if (currentOrderId != -1 || currentOrderCode != null) {
                loadOrderDetails();
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin đơn hàng để theo dõi.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orderDao != null) orderDao.open();
        if (orderItemDao != null) orderItemDao.open();
        if (currentOrderId != -1 || currentOrderCode != null) {
            loadOrderDetails();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orderDao != null) orderDao.close();
        if (orderItemDao != null) orderItemDao.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderDao != null) orderDao.close();
        if (orderItemDao != null) orderItemDao.close();
    }

    private void initViews() {
        backButton = findViewById(R.id.button_tracking_back);
        orderCodeTextView = findViewById(R.id.text_view_tracking_order_code);
        orderDateTextView = findViewById(R.id.text_view_tracking_order_date);
        firstItemPreviewLayout = findViewById(R.id.layout_tracking_first_item_preview);
        itemThumbnailImageView = findViewById(R.id.image_view_tracking_item_thumbnail);
        itemTitleTextView = findViewById(R.id.text_view_tracking_item_title);
        itemQuantityPriceTextView = findViewById(R.id.text_view_tracking_item_quantity_price);
        viewInvoiceButton = findViewById(R.id.button_view_invoice);
        receiveOrderButton = findViewById(R.id.button_receive_order);

        receiveOrderButton.setVisibility(View.GONE);
        receiveOrderButton.setEnabled(false);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        viewInvoiceButton.setOnClickListener(v -> {
            Intent invoiceIntent = new Intent(OrderTrackingActivity.this, OrderConfirmationActivity.class);
            invoiceIntent.putExtra("order_id", currentOrderId);
            invoiceIntent.putExtra("order_code", currentOrderCode);
            startActivity(invoiceIntent);
        });

        receiveOrderButton.setOnClickListener(v -> {
            if (currentOrderId != -1) {
                boolean success = orderDao.updateOrderStatus(currentOrderId, "Đã hoàn thành");
                if (success) {
                    Toast.makeText(this, "Bạn đã xác nhận nhận đơn hàng. Trạng thái đã được cập nhật.", Toast.LENGTH_LONG).show();
                    currentOrderStatus = "Đã hoàn thành";
                    updateReceiveButtonState(currentOrderStatus);
                } else {
                    Toast.makeText(this, "Không thể cập nhật trạng thái đơn hàng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không tìm thấy ID đơn hàng để cập nhật.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        Order order = null;
        if (currentOrderId != -1) {
            order = orderDao.getOrderById(currentOrderId);
        } else if (currentOrderCode != null) {
            order = orderDao.getOrderByOrderCode(currentOrderCode);
        }

        if (order != null) {
            currentOrderCode = order.getOrderCode();
            currentOrderStatus = order.getStatus();

            orderCodeTextView.setText("Mã đơn hàng: " + order.getOrderCode());
            orderDateTextView.setText("Ngày đặt: " + new SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault()).format(new Date(order.getOrderDate())));

            List<OrderItem> orderItems = orderItemDao.getOrderItemsByOrderId(order.getId());
            if (!orderItems.isEmpty()) {
                OrderItem firstItem = orderItems.get(0);
                itemTitleTextView.setText(firstItem.getProductName());
                itemQuantityPriceTextView.setText(
                        String.format(Locale.getDefault(), "%d sản phẩm - %,.0fđ",
                                firstItem.getProductQuantity(), firstItem.getProductPrice() * firstItem.getProductQuantity()));
                if (firstItem.getProductImageUrl() != null && !firstItem.getProductImageUrl().isEmpty()) {
                    Glide.with(this).load(firstItem.getProductImageUrl()).placeholder(R.drawable.sachbia1).error(R.drawable.sachbia1).into(itemThumbnailImageView);
                } else {
                    itemThumbnailImageView.setImageResource(R.drawable.sachbia1);
                }
                firstItemPreviewLayout.setVisibility(View.VISIBLE);
            } else {
                firstItemPreviewLayout.setVisibility(View.GONE);
            }

            updateReceiveButtonState(currentOrderStatus);

        } else {
            Toast.makeText(this, "Không tìm thấy chi tiết đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateReceiveButtonState(String status) {
        if ("Đang xử lý".equals(status) || "Đang chuẩn bị".equals(status) || "Đang giao hàng".equals(status)) {
            receiveOrderButton.setVisibility(View.VISIBLE);
            receiveOrderButton.setEnabled(true);
        } else {
            receiveOrderButton.setVisibility(View.GONE);
            receiveOrderButton.setEnabled(false);
        }
    }

    private String getShippingNameFromAddress(String fullAddress) {
        if (fullAddress != null && fullAddress.contains(" (") && fullAddress.endsWith(")")) {
            try {
                int startIndex = fullAddress.lastIndexOf(" (") + 2;
                int endIndex = fullAddress.lastIndexOf(" - ");
                if (startIndex < endIndex) {
                    return fullAddress.substring(startIndex, endIndex);
                }
            } catch (Exception e) {
                Log.e("OrderTracking", "Error parsing shipping name: " + e.getMessage());
            }
        }
        return "N/A";
    }

    private String getShippingPhoneFromAddress(String fullAddress) {
        if (fullAddress != null && fullAddress.contains(" - ") && fullAddress.endsWith(")")) {
            try {
                int startIndex = fullAddress.lastIndexOf(" - ") + 3;
                int endIndex = fullAddress.lastIndexOf(")");
                if (startIndex < endIndex) {
                    return fullAddress.substring(startIndex, endIndex);
                }
            } catch (Exception e) {
                Log.e("OrderTracking", "Error parsing shipping phone: " + e.getMessage());
            }
        }
        return "N/A";
    }

    private String getShippingFullAddress(String fullAddress) {
        if (fullAddress != null && fullAddress.contains(" (") && fullAddress.endsWith(")")) {
            return fullAddress.substring(0, fullAddress.lastIndexOf(" ("));
        }
        return fullAddress;
    }
}