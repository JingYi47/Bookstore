package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private ImageView backButtonConfirmation;
    private TextView transactionCodeTextView;
    private TextView orderTimeTextView;
    private LinearLayout productDetailsContainer;
    private TextView summaryPriceTextView;
    private TextView summaryDiscountTextView;
    private TextView summaryTotalTextView;
    private TextView paymentMethodTextView;
    private TextView shippingNameTextView;
    private TextView shippingPhoneTextView;
    private TextView shippingAddressTextView;
    private Button trackOrderButton;

    private OrderDao orderDao;
    private OrderItemDao orderItemDao;

    private int currentOrderId = -1;
    private String currentOrderCode = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        orderDao = new OrderDao(this);
        orderItemDao = new OrderItemDao(this);

        initViews();
        Intent intent = getIntent();
        if (intent != null) {
            currentOrderId = intent.getIntExtra("order_id", -1);
            currentOrderCode = intent.getStringExtra("order_code");

            if (currentOrderId != -1 || currentOrderCode != null) {
                loadOrderDetailsFromDatabase(currentOrderId, currentOrderCode);
            } else {
                populateOrderDetailsFromIntentExtras(intent);
            }
        }
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orderDao != null) orderDao.open();
        if (orderItemDao != null) orderItemDao.open();
        if (currentOrderId != -1 || currentOrderCode != null) {
            loadOrderDetailsFromDatabase(currentOrderId, currentOrderCode);
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
        backButtonConfirmation = findViewById(R.id.button_back_confirmation);
        transactionCodeTextView = findViewById(R.id.text_view_transaction_code);
        orderTimeTextView = findViewById(R.id.text_view_order_time);
        productDetailsContainer = findViewById(R.id.layout_product_details_container);
        summaryPriceTextView = findViewById(R.id.text_view_summary_price);
        summaryDiscountTextView = findViewById(R.id.text_view_summary_discount);
        summaryTotalTextView = findViewById(R.id.text_view_summary_total);
        paymentMethodTextView = findViewById(R.id.text_view_payment_method);
        shippingNameTextView = findViewById(R.id.text_view_shipping_name);
        shippingPhoneTextView = findViewById(R.id.text_view_shipping_phone);
        shippingAddressTextView = findViewById(R.id.text_view_shipping_address);
        trackOrderButton = findViewById(R.id.button_track_order);
    }

    private void loadOrderDetailsFromDatabase(int orderId, String orderCode) {
        Order order = null;
        if (orderId != -1) {
            order = orderDao.getOrderById(orderId);
        } else if (orderCode != null) {
            order = orderDao.getOrderByOrderCode(orderCode);
        }

        if (order != null) {
            List<OrderItem> orderItems = orderItemDao.getOrderItemsByOrderId(order.getId());

            transactionCodeTextView.setText(order.getOrderCode());
            orderTimeTextView.setText(new SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault()).format(new Date(order.getOrderDate())));

            double subtotalPrice = 0;
            for(OrderItem item : orderItems) {
                subtotalPrice += item.getProductPrice() * item.getProductQuantity();
            }

            double discountPercentage = 0.0;

            double discountAmount = subtotalPrice * discountPercentage;
            double finalTotal = subtotalPrice - discountAmount;

            summaryPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", subtotalPrice));
            summaryDiscountTextView.setText(String.format(Locale.getDefault(), "-%,.0fđ", discountAmount));
            summaryTotalTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", finalTotal));
            paymentMethodTextView.setText("Thanh toán tiền mặt");
            shippingNameTextView.setText(getShippingNameFromAddress(order.getShippingAddress()));
            shippingPhoneTextView.setText(getShippingPhoneFromAddress(order.getShippingAddress()));
            shippingAddressTextView.setText(getShippingFullAddress(order.getShippingAddress()));

            productDetailsContainer.removeAllViews();
            if (orderItems != null && !orderItems.isEmpty()) {
                for (OrderItem item : orderItems) {
                    addProductItemToContainer(item.getProductName(), item.getProductPrice(), item.getProductQuantity(), item.getProductImageUrl());
                }
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void populateOrderDetailsFromIntentExtras(Intent intent) {
        transactionCodeTextView.setText(intent.getStringExtra("transaction_code"));
        orderTimeTextView.setText(intent.getStringExtra("order_time"));

        ArrayList<String> itemTitles = intent.getStringArrayListExtra("item_titles");
        double[] itemPrices = intent.getDoubleArrayExtra("item_prices");
        int[] itemQuantities = intent.getIntArrayExtra("item_quantities");
        ArrayList<String> itemImageUrls = intent.getStringArrayListExtra("item_image_urls");

        double subtotalPrice = intent.getDoubleExtra("subtotal_price", 0.0);
        double discountAmount = intent.getDoubleExtra("discount_amount", 0.0);
        double finalTotalAmount = intent.getDoubleExtra("final_total_amount", 0.0);

        summaryPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", subtotalPrice));
        summaryDiscountTextView.setText(String.format(Locale.getDefault(), "-%,.0fđ", discountAmount));
        summaryTotalTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", finalTotalAmount));
        paymentMethodTextView.setText(intent.getStringExtra("payment_method"));
        shippingNameTextView.setText(intent.getStringExtra("shipping_name"));
        shippingPhoneTextView.setText(intent.getStringExtra("shipping_phone"));
        shippingAddressTextView.setText(intent.getStringExtra("shipping_address_full"));

        productDetailsContainer.removeAllViews();

        if (itemTitles != null && itemPrices != null && itemQuantities != null) {
            for (int i = 0; i < itemTitles.size(); i++) {
                String title = itemTitles.get(i);
                double price = itemPrices[i];
                int quantity = itemQuantities[i];
                String imageUrl = (itemImageUrls != null && i < itemImageUrls.size()) ? itemImageUrls.get(i) : null;
                addProductItemToContainer(title, price, quantity, imageUrl);
            }
        }
    }

    private void addProductItemToContainer(String title, double price, int quantity, String imageUrl) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View productItemView = inflater.inflate(R.layout.item_order_product, productDetailsContainer, false);

        ImageView productImageView = productItemView.findViewById(R.id.image_view_product_image);
        TextView productTitleTextView = productItemView.findViewById(R.id.text_view_product_title);
        TextView productPriceQuantityTextView = productItemView.findViewById(R.id.text_view_product_price_quantity);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.sachbia1).error(R.drawable.sachbia1).into(productImageView);
        } else {
            productImageView.setImageResource(R.drawable.sachbia1);
        }

        productTitleTextView.setText(title);
        productPriceQuantityTextView.setText(String.format(Locale.getDefault(), "%,.0fđ x%d", price, quantity));

        productDetailsContainer.addView(productItemView);
    }

    private void setupListeners() {
        backButtonConfirmation.setOnClickListener(v -> onBackPressed());

        trackOrderButton.setOnClickListener(v -> {
            if (currentOrderId != -1 || currentOrderCode != null) {
                Intent trackingIntent = new Intent(OrderConfirmationActivity.this, OrderTrackingActivity.class);
                trackingIntent.putExtra("order_id", currentOrderId);
                trackingIntent.putExtra("order_code", currentOrderCode);
                startActivity(trackingIntent);
            } else {
                Toast.makeText(this, "Không có thông tin đơn hàng để theo dõi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                Log.e("OrderConfirm", "Error parsing shipping name: " + e.getMessage());
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
                Log.e("OrderConfirm", "Error parsing shipping phone: " + e.getMessage());
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