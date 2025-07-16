package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.thanhvan.bookstoremanager.Adapter.OrderHistoryAdapter;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.sqlite.OrderDao;

import java.util.ArrayList;
import java.util.List;

public class OrderListByStatusFragment extends Fragment implements OrderHistoryAdapter.OnOrderActionListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;

    private OrderHistoryAdapter adapter;
    private List<Order> orderList;
    private OrderDao orderDao;

    private static final String STATUS_PENDING = "Đang xử lý";
    private static final String STATUS_COMPLETED = "Đã hoàn thành";
    private static final String STATUS_CANCELLED = "Đã hủy";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list_by_status, container, false);

        initViews(view);
        initData();
        setupRecyclerView();
        setupListeners();

        loadOrdersByStatus(STATUS_PENDING);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderDao != null) orderDao.open();
        if (tabLayout != null && tabLayout.getSelectedTabPosition() != -1) {
            String currentSelectedStatus = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
            loadOrdersByStatus(currentSelectedStatus);
        } else {
            loadOrdersByStatus(STATUS_PENDING);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (orderDao != null) orderDao.close();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout_admin_order_status);
        recyclerView = view.findViewById(R.id.recycler_view_admin_orders);
        emptyStateTextView = view.findViewById(R.id.text_view_admin_empty_state);
    }

    private void initData() {
        orderDao = new OrderDao(requireContext());
        orderList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(orderList, requireContext(), this, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                String status = tab.getText().toString();
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

    private void loadOrdersByStatus(String statusFilter) {
        List<Order> loadedOrders;
        orderDao.open();
        loadedOrders = orderDao.getOrdersByStatus(statusFilter);

        orderDao.close();

        orderList.clear();
        orderList.addAll(loadedOrders);
        adapter.notifyDataSetChanged();

        if (orderList.isEmpty()) {
            emptyStateTextView.setText(getString(R.string.empty_admin_order_list_message_with_status, statusFilter));
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewDetails(Order order) {
        Intent intent = new Intent(requireContext(), OrderConfirmationActivity.class);
        intent.putExtra("order_id", order.getId());
        intent.putExtra("order_code", order.getOrderCode());
        startActivity(intent);
    }

    @Override
    public void onChangeStatus(Order order, String newStatus) {
        Toast.makeText(requireContext(), "Admin: Chức năng thay đổi trạng thái trực tiếp không khả dụng từ đây.", Toast.LENGTH_SHORT).show();
    }
}