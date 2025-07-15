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
import com.thanhvan.bookstoremanager.Adapter.ProductAdapter;
import com.thanhvan.bookstoremanager.EditProductActivity;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Product;
import com.thanhvan.bookstoremanager.sqlite.BookDao;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementFragment extends Fragment implements ProductAdapter.OnItemActionListener {
    private RecyclerView rvProducts; private ProductAdapter productAdapter;
    private List<Product> productList; private BookDao bookDao;
    private EditText etProductSearch; private FloatingActionButton fabAddProduct;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_management, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvProducts = view.findViewById(R.id.rv_products);
        etProductSearch = view.findViewById(R.id.et_product_search);
        fabAddProduct = view.findViewById(R.id.fab_add_product);
        bookDao = new BookDao(getContext());
        setupRecyclerView(); setupSearch();
        setupFloatingActionButton();
    }
    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), R.layout.item_product, productList, this);
        rvProducts.setAdapter(productAdapter);
    }
    private void loadProducts() {
        productList.clear();
        productList.addAll(bookDao.getAllProducts());
        productAdapter.notifyDataSetChanged();
    }
    private void setupSearch() {
        etProductSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterProducts(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    private void filterProducts(String query) {
        List<Product> filteredList = bookDao.searchProducts(query);
        productAdapter.updateList(filteredList);
    }
    private void setupFloatingActionButton() {
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProductActivity.class);
            startActivity(intent);
        });
    }
    @Override
    public void onEditClick(int position) {
        Product productToEdit = productList.get(position);
        Intent intent = new Intent(getContext(), EditProductActivity.class);
        intent.putExtra("PRODUCT_ID", productToEdit.getId());
        startActivity(intent);
    }
    @Override
    public void onDeleteClick(int position) {
        Product productToDelete = productList.get(position);
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa") .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + productToDelete.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = bookDao.deleteProduct(productToDelete.getId());
                    if (result > 0) { Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show(); loadProducts(); }
                    else { Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show(); }
                }) .setNegativeButton("Hủy", null) .show();
    }
    @Override
    public void onItemClick(int position) {

    }

    @Override public void onResume() { super.onResume();
        bookDao.open(); loadProducts(); }
    @Override public void onPause() { super.onPause();
        bookDao.close(); }
    @Override public void onDestroyView() {
        super.onDestroyView();
        if (productAdapter != null) {
            productAdapter.closeCategoryDao(); }
    }
}