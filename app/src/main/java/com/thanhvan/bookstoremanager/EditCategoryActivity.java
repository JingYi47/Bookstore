package com.thanhvan.bookstoremanager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.thanhvan.bookstoremanager.model.Category;
import com.thanhvan.bookstoremanager.sqlite.CategoryDao;

public class EditCategoryActivity extends AppCompatActivity {
    private EditText etCategoryName; private Button btnUpdateCategory;
    private CategoryDao categoryDao; private int categoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            toolbarTitle.setText("Thêm thể loại");
        }
        etCategoryName = findViewById(R.id.et_category_name);
        btnUpdateCategory = findViewById(R.id.btn_update_category);
        categoryDao = new CategoryDao(this); categoryDao.open();
        if (getIntent().hasExtra("CATEGORY_ID")) {
            categoryId = getIntent().getIntExtra("CATEGORY_ID", -1);
            if (categoryId != -1) {
                Category existingCategory = categoryDao.getCategoryById(categoryId);
                if (existingCategory != null) {
                    etCategoryName.setText(existingCategory.getName());
                    if (getSupportActionBar() != null) { TextView toolbarTitle = findViewById(R.id.toolbar_title); toolbarTitle.setText("Chỉnh sửa thể loại"); }
                    btnUpdateCategory.setText("CẬP NHẬT");
                } else { Toast.makeText(this, "Không tìm thấy thể loại", Toast.LENGTH_SHORT).show(); finish(); }
            }
        }
        btnUpdateCategory.setOnClickListener(v -> saveCategory());
    }
    private void saveCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        if (TextUtils.isEmpty(categoryName)) {
            etCategoryName.setError("Tên thể loại không được để trống");
            etCategoryName.requestFocus(); return; }
        Category category = new Category();
        category.setName(categoryName); long result;
        if (categoryId == -1) { result = categoryDao.addCategory(category);
            if (result > 0) { Toast.makeText(this, "Thêm thể loại thành công!", Toast.LENGTH_SHORT).show(); finish(); }
            else { Toast.makeText(this, "Thêm thể loại thất bại (có thể tên đã tồn tại)", Toast.LENGTH_SHORT).show(); }
        } else { category.setId(categoryId); result = categoryDao.updateCategory(category);
            if (result > 0) { Toast.makeText(this, "Cập nhật thể loại thành công!", Toast.LENGTH_SHORT).show(); finish(); }
            else { Toast.makeText(this, "Cập nhật thể loại thất bại", Toast.LENGTH_SHORT).show(); }
        }
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed(); return true; }
    @Override protected void onDestroy() { super.onDestroy();
        categoryDao.close(); }
}