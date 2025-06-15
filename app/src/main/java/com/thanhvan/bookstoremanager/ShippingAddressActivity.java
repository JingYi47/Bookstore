package com.thanhvan.bookstoremanager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShippingAddressActivity extends AppCompatActivity {

    private ImageView backButton;
    private RecyclerView addressesRecyclerView;
    private Button addNewAddressButton;

    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private int selectedAddressPosition = -1; // Vị trí địa chỉ được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        backButton = findViewById(R.id.button_address_back);
        addressesRecyclerView = findViewById(R.id.recycler_view_addresses);
        addNewAddressButton = findViewById(R.id.button_add_new_address);

        // Khởi tạo danh sách địa chỉ mẫu
        addressList = new ArrayList<>();
        addressList.add(new Address("Nguyen Thi Van", "09868686868", "Trung Kinh, Cau Giay, Ha Noi"));
        addressList.add(new Address("Nguyen Van Anh", "0978787878", "Chuong Duong, Hoan Kiem, Ha Noi"));

        // Cấu hình RecyclerView
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList, new AddressAdapter.OnItemActionListener() {
            @Override
            public void onAddressSelected(int position) {
                selectedAddressPosition = position;
                // Khi người dùng chọn địa chỉ, tự động gửi kết quả và thoát
                sendSelectedAddressResult();
            }

            @Override
            public void onEditAddress(int position) {
                // TODO: Mở dialog chỉnh sửa địa chỉ với dữ liệu hiện có
                Toast.makeText(ShippingAddressActivity.this, "Chỉnh sửa địa chỉ " + addressList.get(position).getFullName(), Toast.LENGTH_SHORT).show();
            }
        });
        addressesRecyclerView.setAdapter(addressAdapter);

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> sendSelectedAddressResult()); // Gửi kết quả khi quay lại

        // Xử lý nút "Thêm địa chỉ giao hàng mới"
        addNewAddressButton.setOnClickListener(v -> showAddAddressDialog());
    }

    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_address, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.edit_text_add_name);
        EditText phoneEditText = dialogView.findViewById(R.id.edit_text_add_phone);
        EditText addressEditText = dialogView.findViewById(R.id.edit_text_add_address);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_add_address);
        Button confirmButton = dialogView.findViewById(R.id.button_add_address_confirm);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                addressList.add(new Address(name, phone, address));
                addressAdapter.notifyItemInserted(addressList.size() - 1);
                dialog.dismiss();
                Toast.makeText(this, "Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendSelectedAddressResult() {
        Intent resultIntent = new Intent();
        if (selectedAddressPosition != -1) {
            Address selectedAddress = addressList.get(selectedAddressPosition);
            resultIntent.putExtra("selected_shipping_address", selectedAddress.getFullAddress());
        } else {
            resultIntent.putExtra("selected_shipping_address", "Chưa chọn địa chỉ giao hàng");
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Xử lý khi nhấn nút back của thiết bị
        sendSelectedAddressResult();
        super.onBackPressed();
    }

    // --- Address Model Class ---
    public static class Address {
        private String fullName;
        private String phone;
        private String fullAddress;

        public Address(String fullName, String phone, String fullAddress) {
            this.fullName = fullName;
            this.phone = phone;
            this.fullAddress = fullAddress;
        }

        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getFullAddress() { return fullAddress; }
    }

    // --- AddressAdapter Class ---
    public static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

        private List<Address> addressList;
        private OnItemActionListener listener;
        private int lastSelectedPosition = -1; // Theo dõi radio button được chọn

        public interface OnItemActionListener {
            void onAddressSelected(int position);
            void onEditAddress(int position);
        }

        public AddressAdapter(List<Address> addressList, OnItemActionListener listener) {
            this.addressList = addressList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_address_item, parent, false);
            return new AddressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
            Address address = addressList.get(position);
            holder.nameTextView.setText(address.getFullName());
            holder.phoneTextView.setText("Điện thoại: " + address.getPhone());
            holder.fullAddressTextView.setText("Địa chỉ: " + address.getFullAddress());

            holder.radioButton.setChecked(position == lastSelectedPosition);

            holder.radioButton.setOnClickListener(v -> {
                int previousSelectedPosition = lastSelectedPosition;
                lastSelectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelectedPosition); // Cập nhật trạng thái radio button cũ
                notifyItemChanged(lastSelectedPosition); // Cập nhật trạng thái radio button mới
                if (listener != null) {
                    listener.onAddressSelected(lastSelectedPosition);
                }
            });

            // Cho phép click vào toàn bộ item để chọn radio button
            holder.itemView.setOnClickListener(v -> {
                holder.radioButton.setChecked(true); // Đặt trạng thái checked cho radio button
                int previousSelectedPosition = lastSelectedPosition;
                lastSelectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(lastSelectedPosition);
                if (listener != null) {
                    listener.onAddressSelected(lastSelectedPosition);
                }
            });

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return addressList.size();
        }

        public static class AddressViewHolder extends RecyclerView.ViewHolder {
            RadioButton radioButton;
            TextView nameTextView;
            TextView phoneTextView;
            TextView fullAddressTextView;
            ImageView editButton;

            public AddressViewHolder(@NonNull View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.radio_select_address);
                nameTextView = itemView.findViewById(R.id.text_view_address_name);
                phoneTextView = itemView.findViewById(R.id.text_view_address_phone);
                fullAddressTextView = itemView.findViewById(R.id.text_view_full_address);
                editButton = itemView.findViewById(R.id.image_view_edit_address);
            }
        }
    }
}
