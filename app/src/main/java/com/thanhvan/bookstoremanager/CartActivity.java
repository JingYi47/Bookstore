package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thanhvan.bookstoremanager.Adapter.CartAdapter;
import com.thanhvan.bookstoremanager.model.CartItem;
import com.thanhvan.bookstoremanager.model.Discount;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.model.OrderItem;
import com.thanhvan.bookstoremanager.sqlite.BookDao;
import com.thanhvan.bookstoremanager.sqlite.CartItemDao;
import com.thanhvan.bookstoremanager.sqlite.DiscountDao;
import com.thanhvan.bookstoremanager.sqlite.OrderDao;
import com.thanhvan.bookstoremanager.sqlite.OrderItemDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private CartItemDao cartItemDao;
    private OrderDao orderDao;
    private OrderItemDao orderItemDao;
    private BookDao bookDao;
    private DiscountDao discountDao;

    private ActivityResultLauncher<Intent> paymentMethodLauncher;
    private ActivityResultLauncher<Intent> shippingAddressLauncher;
    private ActivityResultLauncher<Intent> promotionLauncher;

    private int selectedDiscountId = -1;
    private double appliedDiscountPercentage = 0.0;
    private String currentShippingName = "Tên Người Nhận";
    private String currentShippingPhone = "Số Điện Thoại";
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initDaos();
        initViews();
        initLaunchers();
        setupRecyclerView();
        setupSimpleListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDaos();
        loadCartItems();
        updateLoginStatusAndOrderButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDaos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDaos();
    }

    private void initDaos() {
        bookDao = new BookDao(this);
        discountDao = new DiscountDao(this);
        orderItemDao = new OrderItemDao(this);
        orderDao = new OrderDao(this);
        cartItemDao = new CartItemDao(this);
    }

    private void initViews() {
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
    }

    private void updateLoginStatusAndOrderButton() {
        SharedPreferences sharedPref = getSharedPreferences(AppConstants.APP_PREFS, Context.MODE_PRIVATE);
        currentUserEmail = sharedPref.getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, "guest@example.com");

        placeOrderButton.setOnClickListener(v -> {
            if (currentUserEmail.equals("guest@example.com")) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CartActivity.this, LoginActivity.class));
                return;
            }
            if (!validateOrder()) {
                return;
            }
            createOrder();
        });
    }

    private boolean validateOrder() {
        if ("Chưa chọn phương thức thanh toán".equals(selectedPaymentMethodTextView.getText().toString())) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ("Chưa chọn địa chỉ giao hàng".equals(selectedShippingAddressTextView.getText().toString())) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (CartItem item : cartList) {
            if (!bookDao.bookExists(item.getProductId())) {
                Toast.makeText(this, "Lỗi: Sản phẩm '" + item.getProductTitle() + "' không còn tồn tại.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void createOrder() {
        double subtotal = 0;
        int totalQuantity = 0;
        for (CartItem item : cartList) {
            subtotal += item.getProductPrice() * item.getProductQuantity();
            totalQuantity += item.getProductQuantity();
        }
        double discountAmount = subtotal * appliedDiscountPercentage;
        double finalTotal = subtotal - discountAmount;
        String orderCode = "ORD" + System.currentTimeMillis();
        long orderDate = System.currentTimeMillis();
        String status = "Đang xử lý";
        String shippingAddressFullText = selectedShippingAddressTextView.getText().toString();

        Order newOrder = new Order();
        newOrder.setUserEmail(currentUserEmail);
        newOrder.setOrderCode(orderCode);
        newOrder.setTotalAmount(finalTotal);
        newOrder.setStatus(status);
        newOrder.setOrderDate(orderDate);
        newOrder.setShippingAddress(shippingAddressFullText + " (" + currentShippingName + " - " + currentShippingPhone + ")");
        newOrder.setTotalQuantity(totalQuantity);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartList) {
            orderItems.add(new OrderItem(item.getProductId(), item.getProductTitle(), item.getProductPrice(), item.getProductQuantity(), item.getImageUrl(), item.getProductCategory()));
        }

        long insertedOrderId = orderDao.addOrder(newOrder, orderItems);

        if (insertedOrderId != -1) {
            Toast.makeText(this, "Đặt đơn hàng thành công!", Toast.LENGTH_LONG).show();
            cartItemDao.clearCart();

            Intent orderConfirmationIntent = new Intent(CartActivity.this, OrderConfirmationActivity.class);
            orderConfirmationIntent.putExtra("transaction_code", newOrder.getOrderCode());
            orderConfirmationIntent.putExtra("order_time", new SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault()).format(new Date(orderDate)));
            orderConfirmationIntent.putExtra("subtotal_price", subtotal);
            orderConfirmationIntent.putExtra("discount_amount", discountAmount);
            orderConfirmationIntent.putExtra("final_total_amount", finalTotal);
            orderConfirmationIntent.putExtra("applied_discount_name", appliedPromoTextView.getText().toString());
            orderConfirmationIntent.putExtra("payment_method", selectedPaymentMethodTextView.getText().toString());
            orderConfirmationIntent.putExtra("shipping_name", currentShippingName);
            orderConfirmationIntent.putExtra("shipping_phone", currentShippingPhone);
            orderConfirmationIntent.putExtra("shipping_address_full", shippingAddressFullText);

            ArrayList<String> itemTitlesConf = new ArrayList<>();
            double[] itemPricesArray = new double[orderItems.size()];
            int[] itemQuantitiesArray = new int[orderItems.size()];
            ArrayList<String> itemImageUrlsConf = new ArrayList<>();
            for (int i = 0; i < orderItems.size(); i++) {
                OrderItem item = orderItems.get(i);
                itemTitlesConf.add(item.getProductName());
                itemPricesArray[i] = item.getProductPrice();
                itemQuantitiesArray[i] = item.getProductQuantity();
                itemImageUrlsConf.add(item.getProductImageUrl());
            }
            orderConfirmationIntent.putStringArrayListExtra("item_titles", itemTitlesConf);
            orderConfirmationIntent.putExtra("item_prices", itemPricesArray);
            orderConfirmationIntent.putExtra("item_quantities", itemQuantitiesArray);
            orderConfirmationIntent.putStringArrayListExtra("item_image_urls", itemImageUrlsConf);

            startActivity(orderConfirmationIntent);

            // finish();

        } else {
            Toast.makeText(this, "Đặt đơn hàng thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        cartList = new ArrayList<>();
        cartItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartList, new CartAdapter.OnItemActionListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                CartItem item = cartList.get(position);
                if (cartItemDao.updateCartItemQuantity(item.getProductId(), newQuantity) > 0) {
                    item.setProductQuantity(newQuantity);
                    updateCartSummary();
                    cartAdapter.notifyItemChanged(position);
                }
            }
            @Override
            public void onDeleteItem(int position) {
                CartItem item = cartList.get(position);
                if (cartItemDao.deleteCartItem(item.getProductId()) > 0) {
                    cartList.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    updateCartSummary();
                }
            }
            @Override
            public void onEditItem(int position) {
                Toast.makeText(CartActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        }, this); // <-- Tham số 'this' (Context) đã được thêm vào đây
        cartItemsRecyclerView.setAdapter(cartAdapter);
    }

    private void setupSimpleListeners() {
        backButton.setOnClickListener(v -> finish());
        addMoreToCartTextView.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }

    private void initLaunchers() {
        paymentMethodLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPaymentMethodTextView.setText(result.getData().getStringExtra("selected_payment_method"));
                    }
                });

        shippingAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedShippingAddressTextView.setText(result.getData().getStringExtra("selected_shipping_address"));
                        currentShippingName = result.getData().getStringExtra("selected_shipping_name");
                        currentShippingPhone = result.getData().getStringExtra("selected_shipping_phone");
                    }
                });

        promotionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedDiscountId = result.getData().getIntExtra(PromotionsActivity.EXTRA_SELECTED_DISCOUNT_ID, -1);
                        appliedDiscountPercentage = result.getData().getDoubleExtra(PromotionsActivity.EXTRA_SELECTED_DISCOUNT_PERCENTAGE, 0.0);
                        updateCartSummary();
                    }
                });

        paymentMethodLayout.setOnClickListener(v -> paymentMethodLauncher.launch(new Intent(this, PaymentMethodActivity.class)));
        shippingAddressLayout.setOnClickListener(v -> shippingAddressLauncher.launch(new Intent(this, ShippingAddressActivity.class)));
        promoCodeLayout.setOnClickListener(v -> {
            Intent promotionIntent = new Intent(this, PromotionsActivity.class);
            promotionLauncher.launch(promotionIntent);
        });
    }

    private void openDaos() {
        bookDao.open();
        discountDao.open();
        orderDao.open();
        orderItemDao.open();
        cartItemDao.open();
    }

    private void closeDaos() {
        bookDao.close();
        discountDao.close();
        orderDao.close();
        orderItemDao.close();
        cartItemDao.close();
    }

    private void loadCartItems() {
        List<CartItem> loadedItems = cartItemDao.getAllCartItems();
        cartList.clear();
        cartList.addAll(loadedItems);
        cartAdapter.notifyDataSetChanged();
        updateCartSummary();
    }

    private void updateCartSummary() {
        double subtotal = 0;
        for (CartItem item : cartList) {
            subtotal += item.getProductPrice() * item.getProductQuantity();
        }

        double discountAmount = subtotal * appliedDiscountPercentage;
        double finalTotal = subtotal - discountAmount;
        subtotalPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", subtotal));

        if (appliedDiscountPercentage > 0) {
            Discount promo = discountDao.getDiscountById(selectedDiscountId);
            appliedPromoTextView.setText(promo != null ? promo.getName() : String.format(Locale.getDefault(), "Giảm %.0f%%", appliedDiscountPercentage * 100));
            discountAmountTextView.setText(String.format(Locale.getDefault(), "-%,.0fđ", discountAmount));
            discountAmountTextView.setVisibility(View.VISIBLE);
        } else {
            appliedPromoTextView.setText("Chưa áp dụng");
            discountAmountTextView.setVisibility(View.GONE);
        }
        finalTotalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", finalTotal));
    }
}

