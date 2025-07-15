package com.thanhvan.bookstoremanager.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.thanhvan.bookstoremanager.R;
import com.thanhvan.bookstoremanager.model.Discount;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PromotionsAdapter extends RecyclerView.Adapter<PromotionsAdapter.PromotionViewHolder> {

    private List<Discount> promotionList;
    private double currentCartTotal;
    private int selectedPromoId = -1;
    private OnPromotionSelectedListener listener;

    public interface OnPromotionSelectedListener {
        void onPromotionSelected(Discount promotion);
        void onPromotionDeselected();
    }

    public PromotionsAdapter(List<Discount> promotionList, double currentCartTotal, int selectedPromoId, OnPromotionSelectedListener listener) {
        this.promotionList = promotionList;
        this.currentCartTotal = currentCartTotal;
        this.selectedPromoId = selectedPromoId;
        this.listener = listener;
    }

    public void updateData(List<Discount> newPromotionList, double newCartTotal, int newSelectedPromoId) {
        this.promotionList = newPromotionList;
        this.currentCartTotal = newCartTotal;
        this.selectedPromoId = newSelectedPromoId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Discount promotion = promotionList.get(position);
        holder.bind(promotion);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCondition, tvRemainingAmount;
        RadioButton radioButton;
        ConstraintLayout clPromotionItem;
        ImageView ivSaleIcon;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_promotion_title);
            tvCondition = itemView.findViewById(R.id.tv_promotion_condition);
            tvRemainingAmount = itemView.findViewById(R.id.tv_remaining_amount);
            radioButton = itemView.findViewById(R.id.radio_button_selection);
            clPromotionItem = itemView.findViewById(R.id.cl_promotion_item);
            ivSaleIcon = itemView.findViewById(R.id.iv_sale_icon);
        }

        public void bind(final Discount promotion) {
            tvTitle.setText(promotion.getName());
            tvCondition.setText(promotion.getMinOrderValue() > 0 ? "Áp dụng cho đơn hàng tối thiểu " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(promotion.getMinOrderValue()) : "Áp dụng cho mọi đơn hàng");

            double remaining = promotion.getMinOrderValue() - currentCartTotal;
            boolean isEligible = remaining <= 0.0;

            if (isEligible) {
                tvRemainingAmount.setVisibility(View.GONE);
                clPromotionItem.setEnabled(true);
                clPromotionItem.setAlpha(1.0f);
                tvTitle.setTextColor(Color.BLACK);
                tvCondition.setTextColor(Color.DKGRAY);
            } else {
                tvRemainingAmount.setVisibility(View.VISIBLE);
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                currencyFormat.setCurrency(java.util.Currency.getInstance("VND"));
                tvRemainingAmount.setText("Hãy mua thêm " + currencyFormat.format(remaining) + " để nhận được khuyến mại này");
                clPromotionItem.setEnabled(false);
                clPromotionItem.setAlpha(0.5f);
                tvTitle.setTextColor(Color.GRAY);
                tvCondition.setTextColor(Color.LTGRAY);
            }

            radioButton.setChecked(selectedPromoId == promotion.getId());

            clPromotionItem.setOnClickListener(v -> {
                if (isEligible && listener != null) {
                    if (selectedPromoId == promotion.getId()) {
                        listener.onPromotionDeselected();
                        selectedPromoId = -1;
                    } else {
                        listener.onPromotionSelected(promotion);
                        selectedPromoId = promotion.getId();
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}