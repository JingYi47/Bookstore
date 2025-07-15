package com.thanhvan.bookstoremanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.thanhvan.bookstoremanager.model.Category;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.BookDao;
import com.thanhvan.bookstoremanager.sqlite.CategoryDao;

import java.util.ArrayList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {

    private EditText etProductId, etProductTitle, etProductAuthor, etProductRating, etProductPrice, etProductOriginalPrice, etProductImageUrl;
    private Spinner spinnerCategory;
    private Button btnUpdateProduct;

    private BookDao bookDao;
    private CategoryDao categoryDao;
    private String productId = null;

    private List<Category> categories;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            toolbarTitle.setText("Thêm sản phẩm");
        }

        etProductId = findViewById(R.id.et_product_id);
        etProductTitle = findViewById(R.id.et_product_name);
        etProductAuthor = findViewById(R.id.et_product_author);
        spinnerCategory = findViewById(R.id.spinner_category);
        etProductRating = findViewById(R.id.et_product_rating);
        etProductPrice = findViewById(R.id.et_product_price);
        etProductOriginalPrice = findViewById(R.id.et_product_original_price);
        etProductImageUrl = findViewById(R.id.et_product_image_url);

        btnUpdateProduct = findViewById(R.id.btn_update_product);

        bookDao = new BookDao(this);
        categoryDao = new CategoryDao(this);
        bookDao.open();
        categoryDao.open();

        loadCategoriesIntoSpinner();

        if (getIntent().hasExtra("PRODUCT_ID")) {
            productId = getIntent().getStringExtra("PRODUCT_ID");
            if (productId != null && !productId.isEmpty()) {
                Product existingProduct = bookDao.getProductById(productId);
                if (existingProduct != null) {
                    fillProductData(existingProduct);
                    if (getSupportActionBar() != null) {
                        TextView toolbarTitle = findViewById(R.id.toolbar_title);
                        toolbarTitle.setText("Chỉnh sửa sản phẩm");
                    }
                    btnUpdateProduct.setText("CẬP NHẬT");
                    etProductId.setEnabled(false);
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            etProductId.setEnabled(true);
        }

        btnUpdateProduct.setOnClickListener(v -> saveProduct());
    }

    private void loadCategoriesIntoSpinner() {
        categories = categoryDao.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (Category cat : categories) {
            categoryNames.add(cat.getName());
        }
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void fillProductData(Product product) {
        etProductId.setText(product.getId());
        etProductTitle.setText(product.getTitle());
        etProductAuthor.setText(product.getAuthor());
        if (product.getCategory() != null) {
            int spinnerPosition = categoryAdapter.getPosition(product.getCategory());
            spinnerCategory.setSelection(spinnerPosition);
        }
        etProductRating.setText(String.valueOf(product.getRating()));
        etProductPrice.setText(String.valueOf(product.getPrice()));
        etProductOriginalPrice.setText(String.valueOf(product.getOriginalPrice()));
        etProductImageUrl.setText(product.getImageUrl());
    }

    private void saveProduct() {
        String id = etProductId.getText().toString().trim();
        String title = etProductTitle.getText().toString().trim();
        String author = etProductAuthor.getText().toString().trim();
        String category = (String) spinnerCategory.getSelectedItem();
        String ratingStr = etProductRating.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String originalPriceStr = etProductOriginalPrice.getText().toString().trim();
        String imageUrl = etProductImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            etProductId.setError("ID không được để trống");
            etProductId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            etProductTitle.setError("Tên sách không được để trống");
            etProductTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(author)) {
            etProductAuthor.setError("Tác giả không được để trống");
            etProductAuthor.requestFocus();
            return;
        }
        if (category == null) {
            Toast.makeText(this, "Vui lòng chọn thể loại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ratingStr) || !isNumeric(ratingStr)) {
            etProductRating.setError("Rating không hợp lệ");
            etProductRating.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr) || !isNumeric(priceStr)) {
            etProductPrice.setError("Giá không hợp lệ");
            etProductPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(originalPriceStr) || !isNumeric(originalPriceStr)) {
            etProductOriginalPrice.setError("Giá gốc không hợp lệ");
            etProductOriginalPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(imageUrl)) {
            etProductImageUrl.setError("URL ảnh không được để trống");
            etProductImageUrl.requestFocus();
            return;
        }

        double rating = Double.parseDouble(ratingStr);
        double price = Double.parseDouble(priceStr);
        double originalPrice = Double.parseDouble(originalPriceStr);

        Product product = new Product(id, title, author, category, rating, price, originalPrice, imageUrl);

        long result;
        if (productId == null) {
            // Kiểm tra ID có bị trùng không khi thêm mới
            if (bookDao.getProductById(id) != null) {
                Toast.makeText(this, "Thêm sản phẩm thất bại (ID đã tồn tại)", Toast.LENGTH_SHORT).show();
                etProductId.setError("ID này đã tồn tại");
                etProductId.requestFocus();
                return;
            }
            result = bookDao.addProduct(product);
            if (result != -1) {
                Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Thêm sản phẩm thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            product.setId(productId);
            result = bookDao.updateProduct(product);
            if (result > 0) {
                Toast.makeText(this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Cập nhật sản phẩm thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bookDao.close();
        categoryDao.close();
    }
}