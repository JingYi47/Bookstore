package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.thanhvan.bookstoremanager.Adapter.OrderHistoryAdapter;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.sqlite.OrderDao;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ImageView backButton;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;

    private OrderHistoryAdapter adapter;
    private List<Order> orderList;
    private OrderDao orderDao;
    private String currentUserEmail;

    private static final String STATUS_PENDING = "Đang xử lý";
    private static final String STATUS_COMPLETED = "Đã hoàn thành";
    private static final String STATUS_CANCELLED = "Đã hủy";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        initData();
        setupRecyclerView();
        setupListeners();

        loadOrdersByStatus(STATUS_PENDING);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orderDao == null) {
            orderDao = new OrderDao(this);
        }
        orderDao.open();
        if (tabLayout != null && tabLayout.getSelectedTabPosition() != -1) {
            String currentStatus = "";
            if (tabLayout.getSelectedTabPosition() == 0) {
                currentStatus = STATUS_PENDING;
            } else if (tabLayout.getSelectedTabPosition() == 1) {
                currentStatus = STATUS_COMPLETED;
            } else if (tabLayout.getSelectedTabPosition() == 2) {
                currentStatus = STATUS_CANCELLED;
            }
            loadOrdersByStatus(currentStatus);
        } else {
            loadOrdersByStatus(STATUS_PENDING);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orderDao != null) {
            orderDao.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderDao != null) {
            orderDao.close();
        }
    }


    private void initViews() {
        backButton = findViewById(R.id.button_history_back);
        tabLayout = findViewById(R.id.tab_layout_order_status);
        recyclerView = findViewById(R.id.recycler_view_order_history);
        emptyStateTextView = findViewById(R.id.text_view_empty_state);
    }

    private void initData() {
        orderDao = new OrderDao(this);
        orderList = new ArrayList<>();

        SharedPreferences sharedPref = getSharedPreferences(AppConstants.APP_PREFS, Context.MODE_PRIVATE);
        currentUserEmail = sharedPref.getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, null);

        if (currentUserEmail == null) {
            emptyStateTextView.setText("Vui lòng đăng nhập để xem lịch sử đơn hàng.");
            emptyStateTextView.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(orderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                String status = "";
                if (tab.getPosition() == 0) {
                    status = STATUS_PENDING;
                } else if (tab.getPosition() == 1) {
                    status = STATUS_COMPLETED;
                } else if (tab.getPosition() == 2) {
                    status = STATUS_CANCELLED;
                }
                loadOrdersByStatus(status);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadOrdersByStatus(String status) {
        if (currentUserEmail == null) {
            return;
        }

        List<Order> loadedOrders;
        if (status.isEmpty()) {
            loadedOrders = orderDao.getOrdersByUserEmail(currentUserEmail);
        } else {
            loadedOrders = orderDao.getOrdersByUserEmailAndStatus(currentUserEmail, status);
        }

        orderList.clear();
        orderList.addAll(loadedOrders);
        adapter.notifyDataSetChanged();

        if (orderList.isEmpty()) {
            emptyStateTextView.setText("Bạn chưa có đơn hàng nào ở trạng thái '" + status + "'.");
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }
}