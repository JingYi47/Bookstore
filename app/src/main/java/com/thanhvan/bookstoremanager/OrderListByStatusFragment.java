package com.thanhvan.bookstoremanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thanhvan.bookstoremanager.Adapter.OrderAdapter;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.sqlite.OrderDao;

import java.util.ArrayList;
import java.util.List;

public class OrderListByStatusFragment extends Fragment implements OrderAdapter.OnOrderActionListener {
    private static final String ARG_STATUS = "order_status"; private String orderStatus;
    private RecyclerView rvOrders; private OrderAdapter orderAdapter;
    private List<Order> orderList; private OrderDao orderDao;

    public OrderListByStatusFragment() {}
    public static OrderListByStatusFragment newInstance(String status) {
        OrderListByStatusFragment fragment = new OrderListByStatusFragment();
        Bundle args = new Bundle(); args.putString(ARG_STATUS, status); fragment.setArguments(args); return fragment;
    }
    @Override public void onCreate(@Nullable Bundle savedInstanceState) { super.onCreate(savedInstanceState); if (getArguments() != null) { orderStatus = getArguments().getString(ARG_STATUS); } }
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list_by_status, container, false);
        rvOrders = view.findViewById(R.id.rv_orders_by_status);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); orderDao = new OrderDao(getContext()); setupRecyclerView();
    }
    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext())); orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), orderList, this); rvOrders.setAdapter(orderAdapter);
    }
    private void loadOrders() {
        if (orderDao != null) { orderList.clear();
            orderList.addAll(orderDao.getOrdersByStatus(orderStatus));
            orderAdapter.notifyDataSetChanged(); }
    }
    @Override public void onTrackOrderClick(Order order) {
        Toast.makeText(getContext(), "Theo dõi đơn hàng: " + order.getOrderCode(), Toast.LENGTH_SHORT).show(); }
    @Override public void onResume() { super.onResume(); if (orderDao != null) { orderDao.open(); loadOrders(); } }
    @Override public void onPause() { super.onPause(); if (orderDao != null) { orderDao.close(); } }
    @Override public void onDestroyView() {
        super.onDestroyView(); if (orderAdapter != null) { orderAdapter.closeDao(); } }
}