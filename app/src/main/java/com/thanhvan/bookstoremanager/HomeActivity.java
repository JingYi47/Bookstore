package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.thanhvan.bookstoremanager.Adapter.ProductAdapter;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.BookDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements ProductAdapter.OnItemActionListener {

    private SearchView searchView;
    private ImageView banner1, banner2;
    private TabLayout categoryTabLayout;
    private Chip chipAll, chipRating, chipPrice, chipPromo;
    private RecyclerView productRecyclerView;
    private BottomNavigationView bottomNavigationView;

    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private List<Product> displayedProducts;
    private BookDao bookDao;

    private String currentCategoryFilter = "Tất cả";
    private String currentSortType = "None";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        searchView = findViewById(R.id.search_view_product);
        banner1 = findViewById(R.id.image_view_banner1);
        banner2 = findViewById(R.id.image_view_banner2);
        categoryTabLayout = findViewById(R.id.tab_layout_categories);
        chipAll = findViewById(R.id.chip_all);
        chipRating = findViewById(R.id.chip_rating);
        chipPrice = findViewById(R.id.chip_price);
        chipPromo = findViewById(R.id.chip_promo);
        productRecyclerView = findViewById(R.id.recycler_view_products);
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        bookDao = new BookDao(this);
        bookDao.open();
        allProducts = bookDao.getAllProducts();

        if (allProducts.isEmpty()) {
            bookDao.addProduct(new Product("BOOK001", "Chiến binh cầu vồng", "Andrea Hirata", "Tiểu thuyết tình cảm", 5.0, 45000, 50000,"https://cdn-images.kiotviet.vn/nhungvisao/1e8de132cac14413af4a43b229194b75.jpg"));
            bookDao.addProduct(new Product("BOOK002", "Những người khốn khổ", "Victor Hugo", "Tiểu thuyết bi kịch", 5.0, 30000, 35000, "https://cdn1.fahasa.com/media/catalog/product/i/m/image_229206.jpg"));
            bookDao.addProduct(new Product("BOOK003", "Rừng Na Uy", "Haruki Murakami", "Tiểu thuyết hư cấu", 3.3, 23000, 25000,"https://nhasachphuongnam.com/images/thumbnails/730/900/detailed/221/rung-nauy-tb-2021.jpg"));
            bookDao.addProduct(new Product("BOOK004", "Cuốn theo chiều gió", "Margaret Mitchell", "Tiểu thuyết lịch sử", 4.5, 150000, 160000, "https://cdn1.fahasa.com/media/catalog/product/i/m/image_227898.jpg"));
            bookDao.addProduct(new Product("BOOK005", "Lịch sử thời gian", "Stephen Hawking", "Khoa học", 4.8, 95000, 100000, "https://m.media-amazon.com/images/I/81bu1amfwRL._SL1500_.jpg"));
            allProducts = bookDao.getAllProducts();
        }

        displayedProducts = new ArrayList<>(allProducts);

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new ProductAdapter(this, R.layout.item_product_customer, displayedProducts, this);
        productRecyclerView.setAdapter(productAdapter);

        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tất cả"));
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tiểu thuyết"));
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tình cảm"));
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Khoa học"));

        categoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategoryFilter = tab.getText().toString();
                filterAndSortProducts();
            }

            public void onTabUnselected(TabLayout.Tab tab) {}
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipAll.setOnClickListener(v -> {
            currentSortType = "None";
            currentCategoryFilter = "Tất cả";
            currentSearchQuery = "";
            searchView.setQuery("", false);
            searchView.clearFocus();
            categoryTabLayout.selectTab(categoryTabLayout.getTabAt(0));
            filterAndSortProducts();
        });

        chipRating.setOnClickListener(v -> {
            currentSortType = "Xếp hạng";
            filterAndSortProducts();
        });

        chipPrice.setOnClickListener(v -> {
            currentSortType = "Giá";
            filterAndSortProducts();
        });

        chipPromo.setOnClickListener(v -> {
            currentSortType = "Khuyến mãi";
            filterAndSortProducts();
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            }
            if (itemId == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query.toLowerCase(Locale.getDefault());
                filterAndSortProducts();
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.toLowerCase(Locale.getDefault());
                filterAndSortProducts();
                return false;
            }
        });

        filterAndSortProducts();
    }

    private void filterAndSortProducts() {
        List<Product> tempProducts = new ArrayList<>(allProducts);

        if (!currentCategoryFilter.equals("Tất cả")) {
            tempProducts = tempProducts.stream()
                    .filter(p -> {
                        String c = p.getCategory().toLowerCase(Locale.getDefault());
                        String f = currentCategoryFilter.toLowerCase(Locale.getDefault());
                        return f.equals("tiểu thuyết") ? c.contains(f) : c.equals(f);
                    }).collect(Collectors.toList());
        }

        if (!currentSearchQuery.isEmpty()) {
            tempProducts = tempProducts.stream()
                    .filter(p -> p.getTitle().toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                            p.getAuthor().toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                            p.getCategory().toLowerCase(Locale.getDefault()).contains(currentSearchQuery))
                    .collect(Collectors.toList());
        }

        switch (currentSortType) {
            case "Xếp hạng":
                tempProducts.sort((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));
                break;
            case "Giá":
                tempProducts.sort(Comparator.comparingDouble(Product::getPrice));
                break;
            case "Khuyến mãi":
                tempProducts = tempProducts.stream()
                        .filter(p -> p.getPrice() < p.getOriginalPrice())
                        .sorted(Comparator.comparingDouble(Product::getPrice))
                        .collect(Collectors.toList());
                break;
        }

        displayedProducts.clear();
        displayedProducts.addAll(tempProducts);
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookDao != null) {
            bookDao.open();
            loadProducts();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bookDao != null) {
            bookDao.close();
        }
    }

    @Override
    public void onEditClick(int position) {
        Toast.makeText(this, "Chức năng chỉnh sửa không khả dụng tại đây", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        Toast.makeText(this, "Chức năng xóa không khả dụng tại đây", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        Product product = displayedProducts.get(position);
        Toast.makeText(HomeActivity.this, "Đã chọn: " + product.getTitle(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_title", product.getTitle());
        intent.putExtra("product_category", product.getCategory());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_original_price", product.getOriginalPrice());
        intent.putExtra("product_rating", product.getRating());
        intent.putExtra("product_image_url", product.getImageUrl());
        startActivity(intent);
    }

    private void loadProducts() {
        allProducts = bookDao.getAllProducts();
        if (allProducts.isEmpty()) {
            bookDao.addProduct(new Product("BOOK001", "Chiến binh cầu vồng", "Andrea Hirata", "Tiểu thuyết tình cảm", 5.0, 45000, 50000, "https://cdn-images.kiotviet.vn/nhungvisao/1e8de132cac14413af4a43b229194b75.jpg"));
            bookDao.addProduct(new Product("BOOK002", "Những người khốn khổ", "Victor Hugo", "Tiểu thuyết bi kịch", 5.0, 30000, 35000, "https://cdn1.fahasa.com/media/catalog/product/i/m/image_229206.jpg"));
            bookDao.addProduct(new Product("BOOK003", "Rừng Na Uy", "Haruki Murakami", "Tiểu thuyết hư cấu", 3.3, 23000, 25000, "https://nhasachphuongnam.com/images/thumbnails/730/900/detailed/221/rung-nauy-tb-2021.jpg"));
            bookDao.addProduct(new Product("BOOK004", "Cuốn theo chiều gió", "Margaret Mitchell", "Tiểu thuyết lịch sử", 4.5, 150000, 160000,"https://nhasachphuongnam.com/images/thumbnails/730/900/detailed/221/rung-nauy-tb-2021.jpg"));
            bookDao.addProduct(new Product("BOOK005", "Lịch sử thời gian", "Stephen Hawking", "Khoa học", 4.8, 95000, 100000,"https://m.media-amazon.com/images/I/81bu1amfwRL._SL1500_.jpg"));
            allProducts = bookDao.getAllProducts();
        }
        displayedProducts.clear();
        displayedProducts.addAll(allProducts);
        filterAndSortProducts();
    }
}