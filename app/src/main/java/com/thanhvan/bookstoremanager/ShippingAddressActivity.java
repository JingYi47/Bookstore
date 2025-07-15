package com.thanhvan.bookstoremanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.SharedPreferences;
import static android.app.Activity.RESULT_OK;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.thanhvan.bookstoremanager.Adapter.AddressAdapter;
import com.thanhvan.bookstoremanager.model.Address;
import com.thanhvan.bookstoremanager.sqlite.AddressDao;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class ShippingAddressActivity extends AppCompatActivity implements AddAddressDialogFragment.AddAddressDialogListener {

    private ImageView backButton;
    private RecyclerView addressesRecyclerView;
    private Button addNewAddressButton;

    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private AddressDao addressDao;
    private int selectedAddressPosition = -1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private String loggedInUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        // SỬA LỖI Ở ĐÂY: Sử dụng AppConstants thay vì AddAddressDialogFragment
        SharedPreferences prefs = getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        loggedInUserEmail = prefs.getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, null);

        if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem và quản lý địa chỉ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sendSelectedAddressResult();
                finish();
            }
        });

        backButton = findViewById(R.id.button_address_back);
        addressesRecyclerView = findViewById(R.id.recycler_view_addresses);
        addNewAddressButton = findViewById(R.id.button_add_new_address);

        addressList = new ArrayList<>();
        addressDao = new AddressDao(this);
        addressDao.open();

        loadAddressesFromDatabase();

        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList, new AddressAdapter.OnItemActionListener() {
            @Override
            public void onAddressSelected(int position) {
                selectedAddressPosition = position;
                sendSelectedAddressResult();
            }

            @Override
            public void onEditAddress(int position) {
                Toast.makeText(ShippingAddressActivity.this, "Chức năng chỉnh sửa địa chỉ đang phát triển: " + addressList.get(position).getName(), Toast.LENGTH_SHORT).show();
                // TODO: Triển khai logic chỉnh sửa địa chỉ
            }

            @Override
            public void onDeleteAddress(int position) {
                Address addressToDelete = addressList.get(position);
                int deletedRows = addressDao.deleteAddress(addressToDelete.getId(), loggedInUserEmail);
                if (deletedRows > 0) {
                    addressList.remove(position);
                    addressAdapter.notifyItemRemoved(position);
                    Toast.makeText(ShippingAddressActivity.this, "Đã xóa địa chỉ: " + addressToDelete.getFullAddress(), Toast.LENGTH_SHORT).show();
                    if (addressList.isEmpty()) {
                        showEmptyAddressPrompt();
                    }
                } else {
                    Toast.makeText(ShippingAddressActivity.this, "Xóa địa chỉ thất bại hoặc không tìm thấy địa chỉ.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addressesRecyclerView.setAdapter(addressAdapter);

        backButton.setOnClickListener(v -> sendSelectedAddressResult());

        addNewAddressButton.setOnClickListener(v -> {
            AddAddressDialogFragment dialog = AddAddressDialogFragment.newInstance(loggedInUserEmail);
            dialog.show(getSupportFragmentManager(), "AddAddressDialog");
        });
    }

    private void loadAddressesFromDatabase() {
        addressList.clear();
        addressList.addAll(addressDao.getAllAddressesForUser(loggedInUserEmail));
        if (addressAdapter != null) {
            addressAdapter.notifyDataSetChanged();
        }
        if (addressList.isEmpty()) {
            showEmptyAddressPrompt();
        }
    }

    private void showEmptyAddressPrompt() {
        Toast.makeText(this, "Bạn chưa có địa chỉ nào. Hãy thêm một địa chỉ mới.", Toast.LENGTH_LONG).show();
        if (!isFinishing() && !isDestroyed()) {
            handler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed() && addressDao.getAllAddressesForUser(loggedInUserEmail).isEmpty()) {
                    AddAddressDialogFragment dialog = AddAddressDialogFragment.newInstance(loggedInUserEmail);
                    dialog.show(getSupportFragmentManager(), "AddAddressDialog");
                }
            }, 500);
        }
    }

    @Override
    public void onAddressAdded(Address address) {
        loadAddressesFromDatabase();
        Toast.makeText(this, "Địa chỉ đã được thêm: " + address.getFullAddress(), Toast.LENGTH_SHORT).show();
    }

    private void sendSelectedAddressResult() {
        Intent resultIntent = new Intent();
        if (selectedAddressPosition != -1 && selectedAddressPosition < addressList.size()) {
            Address selectedAddress = addressList.get(selectedAddressPosition);
            resultIntent.putExtra("selected_shipping_address_obj", selectedAddress);
            resultIntent.putExtra("selected_shipping_address", selectedAddress.getFullAddress());
            resultIntent.putExtra("selected_shipping_name", selectedAddress.getName());
            resultIntent.putExtra("selected_shipping_phone", selectedAddress.getPhone());
        } else {
            resultIntent.putExtra("selected_shipping_address", "Chưa chọn địa chỉ giao hàng");
            resultIntent.putExtra("selected_shipping_name", "Chưa chọn tên");
            resultIntent.putExtra("selected_shipping_phone", "Chưa chọn SĐT");
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addressDao != null) {
            addressDao.close();
        }
    }
}