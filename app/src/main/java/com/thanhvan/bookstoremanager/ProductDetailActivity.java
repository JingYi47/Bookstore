package com.thanhvan.bookstoremanager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView productImageView;
    private TextView productTitleTextView;
    private TextView productCategoryTextView;
    private TextView productPriceTextView; // Giá bán
    private TextView productOriginalPriceTextView; // Giá gốc (cần thêm vào layout nếu muốn hiển thị)
    private TextView quantityTextView;
    private Button decreaseQuantityButton;
    private Button increaseQuantityButton;
    private TextView productRatingTextView;
    private TextView productDescriptionTextView;
    private TextView totalAmountTextView;
    private Button addToCartButton;

    private int currentQuantity = 1; // Số lượng mặc định
    private double productCurrentPrice = 0.0; // Biến lưu giá hiện tại của sản phẩm
    private double productOriginalPrice = 0.0; // Biến lưu giá gốc của sản phẩm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ các View (đảm bảo các ID này khớp với activity_product_detail.xml của bạn)
        backButton = findViewById(R.id.button_back);
        productImageView = findViewById(R.id.image_view_detail_product);
        productTitleTextView = findViewById(R.id.text_view_detail_product_title);
        productCategoryTextView = findViewById(R.id.text_view_detail_product_category);
        productPriceTextView = findViewById(R.id.text_view_detail_product_price);
        // Nếu có TextView cho giá gốc trong layout, ánh xạ nó:
        // productOriginalPriceTextView = findViewById(R.id.text_view_detail_product_original_price);
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
            String title = intent.getStringExtra("product_title");
            String category = intent.getStringExtra("product_category");
            productCurrentPrice = intent.getDoubleExtra("product_price", 0.0); // SỬA: dùng getDoubleExtra
            productOriginalPrice = intent.getDoubleExtra("product_original_price", 0.0); // SỬA: lấy giá gốc
            int imageResId = intent.getIntExtra("product_image", 0);
            double rating = intent.getDoubleExtra("product_rating", 0.0); // SỬA: lấy rating

            // Gán dữ liệu lên các View
            productTitleTextView.setText(title);
            productCategoryTextView.setText(category);
            // Định dạng giá bán
            productPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", productCurrentPrice));

            // Nếu có TextView cho giá gốc, hiển thị và gạch ngang
            // if (productOriginalPriceTextView != null) {
            //     productOriginalPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", productOriginalPrice));
            //     productOriginalPriceTextView.setPaintFlags(productOriginalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            // }

            if (imageResId != 0) {
                productImageView.setImageResource(imageResId);
            }

            productRatingTextView.setText(String.format(Locale.getDefault(), "%.1f", rating) + " (1) - Xếp hạng và đánh giá");
            productDescriptionTextView.setText("Tóm tắt nội dung: Đây là một cuốn sách hấp dẫn với nội dung sâu sắc. Nó mang đến cho người đọc những trải nghiệm mới mẻ và nhiều cảm xúc khó quên. Đọc sách giúp mở rộng kiến thức và thế giới quan."); // Bạn có thể lấy description từ Intent nếu muốn

            // Cập nhật tổng tiền ban đầu (khi Activity vừa khởi tạo)
            updateTotalPrice(); // Gọi phương thức để tính và hiển thị tổng tiền
        }

        // Xử lý nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng màn hình hiện tại và quay lại màn hình trước
            }
        });

        // Xử lý tăng số lượng
        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuantity++;
                quantityTextView.setText(String.valueOf(currentQuantity));
                updateTotalPrice(); // Cập nhật tổng tiền
            }
        });

        // Xử lý giảm số lượng
        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    quantityTextView.setText(String.valueOf(currentQuantity));
                    updateTotalPrice(); // Cập nhật tổng tiền
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý nút "Thêm vào giỏ hàng"
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement logic to add product to cart (e.g., save to SQLite, update global cart object)
                Toast.makeText(ProductDetailActivity.this, "Đã thêm " + currentQuantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();

                // Chuyển sang màn hình giỏ hàng
                Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
                // Truyền thông tin sản phẩm và số lượng để hiển thị trong giỏ hàng
                cartIntent.putExtra("added_product_title", productTitleTextView.getText().toString());
                cartIntent.putExtra("added_product_price", productCurrentPrice); // Truyền giá Double
                cartIntent.putExtra("added_product_quantity", currentQuantity);
                cartIntent.putExtra("added_product_image", intent.getIntExtra("product_image", 0)); // Vẫn lấy từ intent gốc
                startActivity(cartIntent);
                // Sau khi thêm vào giỏ, bạn có thể cân nhắc finish() ProductDetailActivity hoặc không
            }
        });
    }

    // Phương thức cập nhật tổng tiền
    private void updateTotalPrice() {
        double totalPrice = productCurrentPrice * currentQuantity; // SỬA: dùng productCurrentPrice
        totalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPrice)); // SỬA: định dạng số thực
    }
}