package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher; // NEW IMPORT
import androidx.activity.result.contract.ActivityResultContracts; // NEW IMPORT

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private ImageView backButton;
    private RecyclerView cartItemsRecyclerView;
    private TextView addMoreToCartTextView;
    private LinearLayout paymentMethodLayout;
    private TextView selectedPaymentMethodTextView;
    private LinearLayout shippingAddressLayout;
    private TextView selectedShippingAddressTextView;
    private LinearLayout promoCodeLayout;
    private TextView appliedPromoTextView;
    private TextView subtotalPriceTextView;
    private TextView discountAmountTextView;
    private TextView finalTotalAmountTextView;
    private Button placeOrderButton;

    private CartAdapter cartAdapter;
    private List<CartItem> cartList;

    // Khai báo ActivityResultLauncher
    private ActivityResultLauncher<Intent> paymentMethodLauncher;
    private ActivityResultLauncher<Intent> shippingAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo các ActivityResultLauncher
        paymentMethodLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String selectedMethod = data.getStringExtra("selected_payment_method");
                            if (selectedMethod != null) {
                                selectedPaymentMethodTextView.setText(selectedMethod);
                            }
                        }
                    }
                }
        );

        shippingAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String selectedAddress = data.getStringExtra("selected_shipping_address");
                            if (selectedAddress != null) {
                                selectedShippingAddressTextView.setText(selectedAddress);
                            }
                        }
                    }
                }
        );

        // Ánh xạ các View
        backButton = findViewById(R.id.button_cart_back);
        cartItemsRecyclerView = findViewById(R.id.recycler_view_cart_items);
        addMoreToCartTextView = findViewById(R.id.text_view_add_more_to_cart);
        paymentMethodLayout = findViewById(R.id.layout_payment_method);
        selectedPaymentMethodTextView = findViewById(R.id.text_view_selected_payment_method);
        shippingAddressLayout = findViewById(R.id.layout_shipping_address);
        selectedShippingAddressTextView = findViewById(R.id.text_view_selected_shipping_address);
        promoCodeLayout = findViewById(R.id.layout_promo_code);
        appliedPromoTextView = findViewById(R.id.text_view_applied_promo);
        subtotalPriceTextView = findViewById(R.id.text_view_subtotal_price);
        discountAmountTextView = findViewById(R.id.text_view_discount_amount);
        finalTotalAmountTextView = findViewById(R.id.text_view_final_total_amount);
        placeOrderButton = findViewById(R.id.button_place_order);

        // Lấy dữ liệu sản phẩm vừa thêm từ ProductDetailActivity (hoặc từ SQLite sau này)
        cartList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("added_product_title");
            int price = intent.getIntExtra("added_product_price", 0);
            int quantity = intent.getIntExtra("added_product_quantity", 0);
            int imageResId = intent.getIntExtra("added_product_image", 0);

            if (title != null && quantity > 0) {
                cartList.add(new CartItem(title, "Tiểu thuyết tình cảm", price, quantity, imageResId));
            }
        }
        // Thêm các sản phẩm mẫu nếu giỏ hàng trống hoặc để test
        if (cartList.isEmpty()) {
            cartList.add(new CartItem("J.D. Robb Passions in Death", "Tiểu thuyết tình cảm", 45000, 1, R.drawable.sachbia5));
            cartList.add(new CartItem("The Uniform of Leadership", "Leadership", 100000, 2, R.drawable.sachbia6));
        }

        // Cấu hình RecyclerView cho giỏ hàng
        cartItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartList, new CartAdapter.OnItemActionListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                cartList.get(position).setQuantity(newQuantity);
                updateCartSummary();
                cartAdapter.notifyItemChanged(position);
            }

            @Override
            public void onDeleteItem(int position) {
                cartList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateCartSummary();
                Toast.makeText(CartActivity.this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditItem(int position) {
                Toast.makeText(CartActivity.this, "Chức năng chỉnh sửa đang phát triển", Toast.LENGTH_SHORT).show();
                // TODO: Mở màn hình chi tiết sản phẩm để chỉnh sửa hoặc một dialog
            }
        });
        cartItemsRecyclerView.setAdapter(cartAdapter);

        // Cập nhật tóm tắt giỏ hàng lần đầu
        updateCartSummary();

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> finish());

        // Xử lý nút "Thêm vào giỏ hàng"
        addMoreToCartTextView.setOnClickListener(v -> {
            Intent homeIntent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(homeIntent);
            finish(); // Tùy chọn: đóng CartActivity nếu muốn
        });

        // Xử lý chọn phương thức thanh toán
        paymentMethodLayout.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(CartActivity.this, PaymentMethodActivity.class);
            paymentMethodLauncher.launch(paymentIntent); // Thay thế startActivityForResult
        });

        // Xử lý chọn địa chỉ giao hàng
        shippingAddressLayout.setOnClickListener(v -> {
            Intent addressIntent = new Intent(CartActivity.this, ShippingAddressActivity.class);
            shippingAddressLauncher.launch(addressIntent); // Thay thế startActivityForResult
        });

        // Xử lý áp dụng mã khuyến mãi
        promoCodeLayout.setOnClickListener(v -> Toast.makeText(CartActivity.this, "Áp dụng mã khuyến mại", Toast.LENGTH_SHORT).show());

        // Xử lý nút "Đặt đơn hàng"
        placeOrderButton.setOnClickListener(v -> {
            if (selectedPaymentMethodTextView.getText().toString().equals("Chưa chọn phương thức thanh toán")) {
                Toast.makeText(CartActivity.this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedShippingAddressTextView.getText().toString().equals("Chưa chọn địa chỉ giao hàng")) {
                Toast.makeText(CartActivity.this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cartList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Implement actual order placement logic (e.g., save to SQLite, send to API)
            Toast.makeText(CartActivity.this, "Đặt đơn hàng thành công!", Toast.LENGTH_LONG).show();
            // Xóa giỏ hàng sau khi đặt hàng thành công
            cartList.clear();
            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
            // Chuyển đến màn hình xác nhận đơn hàng hoặc màn hình Home
            Intent thankYouIntent = new Intent(CartActivity.this, OrderConfirmationActivity.class); // Giả định có màn hình này
            startActivity(thankYouIntent);
            finish();
        });
    }

    // Cập nhật tổng tiền và các thông tin khác của giỏ hàng
    private void updateCartSummary() {
        int subtotal = 0;
        int totalItems = 0;
        for (CartItem item : cartList) {
            subtotal += item.getPrice() * item.getQuantity();
            totalItems += item.getQuantity();
        }

        subtotalPriceTextView.setText(String.format(Locale.getDefault(), "%,dđ", subtotal));
        discountAmountTextView.setText("-0đ"); // Tạm thời là 0
        finalTotalAmountTextView.setText(String.format(Locale.getDefault(), "%,dđ", subtotal));
    }


    public static class CartItem {
        private String title;
        private String category;
        private int price;
        private int quantity;
        private int imageResId;

        public CartItem(String title, String category, int price, int quantity, int imageResId) {
            this.title = title;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.imageResId = imageResId;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public int getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getImageResId() { return imageResId; }
    }

    // --- CartAdapter Class ---
    public static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<CartItem> cartList;
        private OnItemActionListener listener;

        public interface OnItemActionListener {
            void onQuantityChange(int position, int newQuantity);
            void onDeleteItem(int position);
            void onEditItem(int position);
        }
        public CartAdapter(List<CartItem> cartList, OnItemActionListener listener) {
            this.cartList = cartList;
            this.listener = listener;
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
            holder.titleTextView.setText(item.getTitle());
            holder.categoryTextView.setText(item.getCategory());
            holder.priceTextView.setText(String.format(Locale.getDefault(), "%,dđ", item.getPrice()));
            holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
            holder.productImageView.setImageResource(item.getImageResId());

            holder.increaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChange(position, newQuantity);
                }
            });

            holder.decreaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity >= 1 && listener != null) {
                    listener.onQuantityChange(position, newQuantity);
                } else if (newQuantity < 1) {
                    Toast.makeText(holder.itemView.getContext(), "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show();
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(position);
                }
            });

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItem(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cartList.size();
        }

        public static class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView productImageView;
            TextView titleTextView;
            TextView categoryTextView;
            TextView priceTextView;
            TextView quantityTextView;
            Button decreaseButton;
            Button increaseButton;
            ImageView editButton;
            ImageView deleteButton;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.image_view_cart_product);
                titleTextView = itemView.findViewById(R.id.text_view_cart_product_title);
                categoryTextView = itemView.findViewById(R.id.text_view_cart_product_category);
                priceTextView = itemView.findViewById(R.id.text_view_cart_product_price);
                quantityTextView = itemView.findViewById(R.id.text_view_cart_quantity);
                decreaseButton = itemView.findViewById(R.id.button_decrease_cart_quantity);
                increaseButton = itemView.findViewById(R.id.button_increase_cart_quantity);
                editButton = itemView.findViewById(R.id.image_view_edit_cart_item);
                deleteButton = itemView.findViewById(R.id.image_view_delete_cart_item);
            }
        }
    }
}