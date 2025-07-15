package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        initViews();
        Intent intent = getIntent();
        if (intent != null) {
            populateOrderDetails(intent);
        }
        setupListeners();
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

    private void populateOrderDetails(Intent intent) {
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
            Glide.with(this).load(imageUrl).placeholder(R.drawable.sachbia1).into(productImageView);
        } else {
            productImageView.setImageResource(R.drawable.sachbia1);
        }

        productTitleTextView.setText(title);
        productPriceQuantityTextView.setText(String.format(Locale.getDefault(), "%,.0fđ x%d", price, quantity));

        productDetailsContainer.addView(productItemView);
    }

    private void setupListeners() {
        backButtonConfirmation.setOnClickListener(v -> navigateToHome());

        // Sửa lại chức năng của nút này để quay về trang chủ
        trackOrderButton.setOnClickListener(v -> navigateToHome());
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
        navigateToHome();
    }
}