package com.thanhvan.bookstoremanager;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.BookDao;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors; // Thêm import này (API level 24+)

public class HomeActivity extends AppCompatActivity {

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

    // Biến lưu trạng thái lọc và sắp xếp hiện tại
    private String currentCategoryFilter = "Tất cả"; // Mặc định là "Tất cả" (hoặc tab đầu tiên)
    private String currentSortType = "None"; // Mặc định không sắp xếp (có thể là "Xếp hạng" / "Giá" / "Khuyến mãi")
    private String currentSearchQuery = ""; // Từ khóa tìm kiếm hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ các View
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

        // Lấy dữ liệu sách từ database
        allProducts = bookDao.getAllBooks(); // bookDao.getAllBooks() sẽ trả về List<Product>

        // Nếu database trống, chèn DỮ LIỆU MẪU CỦA BẠN VÀO ĐÂY
        if (allProducts.isEmpty()) {
            bookDao.insertBook(new Product("BOOK001", "Chiến binh cầu vồng", "Andrea Hirata", "Tiểu thuyết tình cảm", 5.0, 45000, 50000, R.drawable.sachbia2));
            bookDao.insertBook(new Product("BOOK002", "Những người khốn khổ", "Victor Hugo", "Tiểu thuyết bi kịch", 5.0, 30000, 35000, R.drawable.sachbia3));
            bookDao.insertBook(new Product("BOOK003", "Rừng Na Uy", "Haruki Murakami", "Tiểu thuyết hư cấu", 3.3, 23000, 25000, R.drawable.sachbia4));
            bookDao.insertBook(new Product("BOOK004", "Cuốn theo chiều gió", "Margaret Mitchell", "Tiểu thuyết lịch sử", 4.5, 150000, 160000, R.drawable.sachbia2));
            bookDao.insertBook(new Product("BOOK005", "Lịch sử thời gian", "Stephen Hawking", "Khoa học", 4.8, 95000, 100000, R.drawable.sachbia6));

            allProducts = bookDao.getAllBooks(); // Lấy lại danh sách sau khi chèn
        }

        // Khởi tạo danh sách hiển thị ban đầu là tất cả sản phẩm
        displayedProducts = new ArrayList<>(allProducts);

        // Cấu hình RecyclerView
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(displayedProducts, product -> {
            Toast.makeText(HomeActivity.this, "Đã chọn: " + product.getTitle(), Toast.LENGTH_SHORT).show();
            // Chuyển sang màn hình chi tiết sản phẩm
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId()); // Truyền ID để ProductDetailActivity có thể lấy chi tiết
            intent.putExtra("product_title", product.getTitle());
            intent.putExtra("product_category", product.getCategory());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_original_price", product.getOriginalPrice());
            intent.putExtra("product_rating", product.getRating());
            intent.putExtra("product_image", product.getImageResId());
            startActivity(intent);
        });
        productRecyclerView.setAdapter(productAdapter);

        // Cấu hình Category Tabs (Dùng các tab đã có của bạn)
        // Bạn có thể thêm tab "Tất cả" vào đầu nếu muốn người dùng reset lọc category dễ dàng
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tất cả")); // Thêm tab "Tất cả"
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tiểu thuyết"));
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Tình cảm"));
        categoryTabLayout.addTab(categoryTabLayout.newTab().setText("Khoa học"));
        // Thêm Tab Listener để xử lý khi chọn tab
        categoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategoryFilter = tab.getText().toString(); // Lấy tên category từ tab
                filterAndSortProducts(); // Gọi hàm lọc và sắp xếp
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        // Cấu hình Filter Chips (Chỉnh sửa để thực hiện sắp xếp/lọc thực tế)
        chipAll.setOnClickListener(v -> {
            currentSortType = "None"; // Bỏ mọi sắp xếp
            currentCategoryFilter = "Tất cả"; // Bỏ lọc category
            currentSearchQuery = ""; // Xóa tìm kiếm
            searchView.setQuery("", false); // Xóa văn bản trong SearchView
            searchView.clearFocus(); // Ẩn bàn phím ảo nếu có
            categoryTabLayout.selectTab(categoryTabLayout.getTabAt(0)); // Chọn lại tab "Tất cả" (tab đầu tiên)
            filterAndSortProducts();
            Toast.makeText(HomeActivity.this, "Hiển thị tất cả sản phẩm.", Toast.LENGTH_SHORT).show();
        });

        chipRating.setOnClickListener(v -> {
            currentSortType = "Xếp hạng";
            filterAndSortProducts();
            Toast.makeText(HomeActivity.this, "Sắp xếp theo Xếp hạng (Cao nhất).", Toast.LENGTH_SHORT).show();
        });

        chipPrice.setOnClickListener(v -> {
            currentSortType = "Giá";
            filterAndSortProducts();
            Toast.makeText(HomeActivity.this, "Sắp xếp theo Giá (Thấp nhất).", Toast.LENGTH_SHORT).show();
        });

        chipPromo.setOnClickListener(v -> {
            currentSortType = "Khuyến mãi"; // Coi khuyến mãi là 1 loại sắp xếp/lọc đặc biệt
            filterAndSortProducts();
            Toast.makeText(HomeActivity.this, "Lọc: Sản phẩm khuyến mãi.", Toast.LENGTH_SHORT).show();
        });


        // Cấu hình Bottom Navigation (Giữ nguyên như bạn)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Đang ở Trang chủ, không làm gì hoặc khởi tạo lại
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                return true;
            }
            return false;
        });
        // Đặt mục mặc định được chọn cho BottomNavigationView là Trang chủ
        bottomNavigationView.setSelectedItemId(R.id.nav_home);


        // Xử lý tìm kiếm (Chỉnh sửa để thực hiện tìm kiếm thực tế)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query.toLowerCase(Locale.getDefault());
                filterAndSortProducts(); // Lọc khi nhấn Enter hoặc nút tìm kiếm
                Toast.makeText(HomeActivity.this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.toLowerCase(Locale.getDefault());
                filterAndSortProducts(); // Lọc ngay khi người dùng gõ
                return false;
            }
        });

        // Gọi lần đầu để hiển thị tất cả sản phẩm
        filterAndSortProducts();
    }


    private void filterAndSortProducts() {
        // Bắt đầu với tất cả sản phẩm gốc
        List<Product> tempProducts = new ArrayList<>(allProducts);

        // 1. Lọc theo danh mục (Category Filter)
        if (!currentCategoryFilter.equals("Tất cả")) {
            tempProducts = tempProducts.stream()
                    .filter(product -> {
                        String productCategoryLowerCase = product.getCategory().toLowerCase(Locale.getDefault());
                        String filterCategoryLowerCase = currentCategoryFilter.toLowerCase(Locale.getDefault());

                        // Nếu category là "Tiểu thuyết", kiểm tra xem category của sách có chứa "tiểu thuyết" không
                        if (filterCategoryLowerCase.equals("tiểu thuyết")) {
                            return productCategoryLowerCase.contains(filterCategoryLowerCase);
                        } else {

                            return productCategoryLowerCase.equals(filterCategoryLowerCase);
                        }
                    })
                    .collect(Collectors.toList());
        }

        // 2. Lọc theo từ khóa tìm kiếm (Search Filter)
        if (!currentSearchQuery.isEmpty()) {
            tempProducts = tempProducts.stream()
                    .filter(product -> product.getTitle().toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                            product.getAuthor().toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                            product.getCategory().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)) // Có thể tìm cả trong category
                    .collect(Collectors.toList());
        }

        // 3. Sắp xếp (Sort)
        switch (currentSortType) {
            case "Xếp hạng":
                Collections.sort(tempProducts, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating())); // Sắp xếp giảm dần theo rating
                break;
            case "Giá":
                Collections.sort(tempProducts, Comparator.comparingDouble(Product::getPrice)); // Sắp xếp tăng dần theo giá
                break;
            case "Khuyến mãi":
                // Lọc sản phẩm có khuyến mãi, sau đó có thể sắp xếp phụ
                tempProducts = tempProducts.stream()
                        .filter(product -> product.getPrice() < product.getOriginalPrice())
                        .collect(Collectors.toList());
                // Sau khi lọc khuyến mãi, có thể sắp xếp theo giá tăng dần
                Collections.sort(tempProducts, Comparator.comparingDouble(Product::getPrice));
                break;
            case "None": // Trường hợp "Tất cả" hoặc không sắp xếp
            default:
                // Không làm gì, giữ nguyên thứ tự ban đầu từ DB hoặc thứ tự sau khi lọc
                break;
        }

        // Cập nhật danh sách hiển thị và thông báo cho Adapter để làm mới RecyclerView
        displayedProducts.clear();
        displayedProducts.addAll(tempProducts);
        productAdapter.notifyDataSetChanged();
    }


    // ProductAdapter Class (Giữ nguyên như code bạn đã cung cấp, không cần thay đổi gì thêm)
    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<Product> productList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Product product);
        }

        public ProductAdapter(List<Product> productList, OnItemClickListener listener) {
            this.productList = productList;
            this.listener = listener;
        }

        // Phương thức này không còn cần thiết vì chúng ta truyền trực tiếp displayedProducts vào constructor
        // public void updateData(List<Product> newData) {
        //     this.productList.clear();
        //     this.productList.addAll(newData);
        //     notifyDataSetChanged();
        // }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_product_item, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = productList.get(position);
            holder.titleTextView.setText(product.getTitle());
            holder.categoryTextView.setText(product.getCategory());
            // Sửa lỗi định dạng số thực: %f cho double/float
            holder.ratingTextView.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()) + " (1)");
            holder.priceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getPrice()));
            holder.originalPriceTextView.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getOriginalPrice())); // Đã sửa lỗi %d -> %.0f
            holder.originalPriceTextView.setPaintFlags(holder.originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productImageView.setImageResource(product.getImageResId());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product);
                }
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView productImageView;
            TextView titleTextView;
            TextView categoryTextView;
            TextView ratingTextView;
            TextView priceTextView;
            TextView originalPriceTextView;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.image_view_product);
                titleTextView = itemView.findViewById(R.id.text_view_product_title);
                categoryTextView = itemView.findViewById(R.id.text_view_product_category);
                ratingTextView = itemView.findViewById(R.id.text_view_product_rating);
                priceTextView = itemView.findViewById(R.id.text_view_product_price);
                originalPriceTextView = itemView.findViewById(R.id.text_view_product_original_price);
            }
        }
    }
}