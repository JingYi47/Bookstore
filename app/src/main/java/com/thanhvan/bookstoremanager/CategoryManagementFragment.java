package com.thanhvan.bookstoremanager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thanhvan.bookstoremanager.Adapter.CategoryAdapter;
import com.thanhvan.bookstoremanager.EditCategoryActivity;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Category;
import com.thanhvan.bookstoremanager.sqlite.CategoryDao;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementFragment extends Fragment implements CategoryAdapter.OnItemActionListener {
    private RecyclerView rvCategories; private CategoryAdapter categoryAdapter;
    private List<Category> categoryList; private CategoryDao categoryDao;
    private EditText etCategorySearch; private FloatingActionButton fabAddCategory;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvCategories = view.findViewById(R.id.rv_categories);
        etCategorySearch = view.findViewById(R.id.et_category_search);
        fabAddCategory = view.findViewById(R.id.fab_add_category);
        categoryDao = new CategoryDao(getContext());
        setupRecyclerView(); setupSearch();
        setupFloatingActionButton();
    }
    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryList = new ArrayList<>(); categoryAdapter = new CategoryAdapter(categoryList, this);
        rvCategories.setAdapter(categoryAdapter);
    }
    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(categoryDao.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }
    private void setupSearch() {
        etCategorySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterCategories(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    private void filterCategories(String query) {
        List<Category> filteredList = categoryDao.searchCategories(query);
        categoryAdapter.updateList(filteredList);
    }
    private void setupFloatingActionButton() {
        fabAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditCategoryActivity.class); startActivity(intent);
        });
    }
    @Override
    public void onEditClick(int position) {
        Category categoryToEdit = categoryList.get(position);
        Intent intent = new Intent(getContext(), EditCategoryActivity.class);
        intent.putExtra("CATEGORY_ID", categoryToEdit.getId()); startActivity(intent);
    }
    @Override
    public void onDeleteClick(int position) {
        Category categoryToDelete = categoryList.get(position);
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa") .setMessage("Bạn có chắc chắn muốn xóa thể loại \"" + categoryToDelete.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = categoryDao.deleteCategory(categoryToDelete.getId());
                    if (result > 0) { Toast.makeText(getContext(), "Đã xóa thể loại", Toast.LENGTH_SHORT).show(); loadCategories(); }
                    else { Toast.makeText(getContext(), "Lỗi khi xóa thể loại", Toast.LENGTH_SHORT).show(); }
                }) .setNegativeButton("Hủy", null) .show();
    }
    @Override public void onResume() { super.onResume();
        categoryDao.open(); loadCategories(); }
    @Override public void onPause() { super.onPause();
        categoryDao.close(); }
    @Override public void onDestroyView() {
        super.onDestroyView(); }
}