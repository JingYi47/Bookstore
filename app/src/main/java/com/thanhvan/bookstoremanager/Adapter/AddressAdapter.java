package com.thanhvan.bookstoremanager.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnItemActionListener listener;
    private int selectedPosition = -1;

    public AddressAdapter(List<Address> addressList, OnItemActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    public interface OnItemActionListener {
        void onAddressSelected(int position);
        void onEditAddress(int position);
        void onDeleteAddress(int position);
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

        holder.fullNameTextView.setText(address.getName());
        holder.phoneTextView.setText(address.getPhone());
        holder.fullAddressTextView.setText(address.getFullAddress());

        holder.selectAddressRadioButton.setChecked(position == selectedPosition);
        holder.selectAddressRadioButton.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onAddressSelected(selectedPosition);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditAddress(holder.getAdapterPosition());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteAddress(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton selectAddressRadioButton;
        TextView fullNameTextView;
        TextView phoneTextView;
        TextView fullAddressTextView;
        ImageView editButton;
        ImageView deleteButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            selectAddressRadioButton = itemView.findViewById(R.id.radio_select_address);
            fullNameTextView = itemView.findViewById(R.id.text_view_address_name);
            phoneTextView = itemView.findViewById(R.id.text_view_address_phone);
            fullAddressTextView = itemView.findViewById(R.id.text_view_address_full_address);
            editButton = itemView.findViewById(R.id.image_view_edit_address);
            deleteButton = itemView.findViewById(R.id.image_view_delete_address);
        }
    }
}