package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.thanhvan.bookstoremanager.model.CartItem;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.CartItemDao;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView productImageView;
    private TextView productTitleTextView;
    private TextView productCategoryTextView;
    private TextView productPriceTextView;
    private TextView productOriginalPriceTextView;
    private TextView quantityTextView;
    private Button decreaseQuantityButton;
    private Button increaseQuantityButton;
    private TextView productRatingTextView;
    private TextView productDescriptionTextView;
    private TextView totalAmountTextView;
    private Button addToCartButton;

    private int currentQuantity = 1;
    private double productCurrentPrice = 0.0;
    private double productOriginalPrice = 0.0;
    private String productId;
    private String productTitle;
    private String productCategory;
    private String productImageUrl;
    private double productRating;

    private CartItemDao cartItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        cartItemDao = new CartItemDao(this);

        backButton = findViewById(R.id.button_back);
        productImageView = findViewById(R.id.image_view_detail_product);
        productTitleTextView = findViewById(R.id.text_view_detail_product_title);
        productCategoryTextView = findViewById(R.id.text_view_detail_product_category);
        productPriceTextView = findViewById(R.id.text_view_detail_product_price);
        quantityTextView = findViewById(R.id.text_view_quantity);
        decreaseQuantityButton = findViewById(R.id.button_decrease_quantity);
        increaseQuantityButton = findViewById(R.id.button_increase_quantity);
        productRatingTextView = findViewById(R.id.text_view_detail_product_rating);
        productDescriptionTextView = findViewById(R.id.text_view_product_description);
        totalAmountTextView = findViewById(R.id.text_view_total_amount);
        addToCartButton = findViewById(R.id.button_add_to_cart);

        // Lấy dữ liệu sản phẩm từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("product_id");
            productTitle = intent.getStringExtra("product_title");
            productCategory = intent.getStringExtra("product_category");
            productCurrentPrice = intent.getDoubleExtra("product_price", 0.0);
            productOriginalPrice = intent.getDoubleExtra("product_original_price", 0.0);
            productImageUrl = intent.getStringExtra("product_image_url");
            productRating = intent.getDoubleExtra("product_rating", 0.0);

            productTitleTextView.setText(productTitle);
            productCategoryTextView.setText(productCategory);
            productPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", productCurrentPrice));

            if (productImageUrl != null && !productImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(productImageUrl)
                        .placeholder(R.drawable.sachbia1)
                        .error(R.drawable.sachbia1)
                        .into(productImageView);
            } else {
                productImageView.setImageResource(R.drawable.sachbia1);
            }

            productRatingTextView.setText(String.format(Locale.getDefault(), "%.1f", productRating) + " (1) - Xếp hạng và đánh giá");
            productDescriptionTextView.setText("Tóm tắt nội dung: Đây là một cuốn sách hấp dẫn với nội dung sâu sắc. Nó mang đến cho người đọc những trải nghiệm mới mẻ và nhiều cảm xúc khó quên. Đọc sách giúp mở rộng kiến thức và thế giới quan.");

            updateTotalPrice();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuantity++;
                quantityTextView.setText(String.valueOf(currentQuantity));
                updateTotalPrice();
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    quantityTextView.setText(String.valueOf(currentQuantity));
                    updateTotalPrice();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSelectedProductToCart();
            }
        });
    }

    private void addSelectedProductToCart() {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không thể thêm sản phẩm không xác định vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa (dùng productId)
        if (cartItemDao.isProductInCart(productId)) {
            int existingQuantity = cartItemDao.getProductQuantity(productId);
            long result = cartItemDao.updateCartItemQuantity(productId, existingQuantity + currentQuantity);
            if (result > 0) {
                Toast.makeText(this, "Đã cập nhật số lượng sản phẩm trong giỏ hàng.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cập nhật giỏ hàng thất bại.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Thêm mới sản phẩm vào giỏ hàng (với productId)
            CartItem newCartItem = new CartItem(
                    productTitle,
                    productCurrentPrice,
                    currentQuantity,
                    productImageUrl,
                    productCategory,
                    productId
            );
            long result = cartItemDao.addCartItem(newCartItem);
            if (result != -1) {
                Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thêm sản phẩm vào giỏ hàng thất bại.", Toast.LENGTH_SHORT).show();
            }
        }

        Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
        startActivity(cartIntent);
        finish();
    }


    private void updateTotalPrice() {
        double totalPrice = productCurrentPrice * currentQuantity;
        totalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPrice));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cartItemDao != null) {
            cartItemDao.open();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cartItemDao != null) {
            cartItemDao.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartItemDao != null) {
            cartItemDao.close();
        }
    }
}