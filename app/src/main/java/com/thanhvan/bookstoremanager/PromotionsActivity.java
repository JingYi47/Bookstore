package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thanhvan.bookstoremanager.Adapter.PromotionsAdapter;
import com.thanhvan.bookstoremanager.model.Discount;
import com.thanhvan.bookstoremanager.sqlite.DiscountDao;

import java.util.ArrayList;
import java.util.List;

public class PromotionsActivity extends AppCompatActivity implements PromotionsAdapter.OnPromotionSelectedListener {

    private RecyclerView rvPromotions;
    private PromotionsAdapter promotionsAdapter;

    private DiscountDao discountDao;
    private double currentCartTotal;
    private Discount selectedDiscount = null;

    public static final String EXTRA_CART_TOTAL = "extra_cart_total";
    public static final String EXTRA_SELECTED_DISCOUNT_ID = "extra_selected_discount_id";
    public static final String EXTRA_SELECTED_DISCOUNT_PERCENTAGE = "extra_selected_discount_percentage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);

        rvPromotions = findViewById(R.id.rv_promotions);

        discountDao = new DiscountDao(this);
        discountDao.open();

        currentCartTotal = getIntent().getDoubleExtra(EXTRA_CART_TOTAL, 0.0);
        int previouslySelectedDiscountId = getIntent().getIntExtra(EXTRA_SELECTED_DISCOUNT_ID, -1);


        List<Discount> allDiscounts = discountDao.getAllActiveDiscounts();

        promotionsAdapter = new PromotionsAdapter(allDiscounts, currentCartTotal, previouslySelectedDiscountId, this);
        rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        rvPromotions.setAdapter(promotionsAdapter);

        if (previouslySelectedDiscountId != -1) {
            for (Discount discount : allDiscounts) {
                if (discount.getId() == previouslySelectedDiscountId) {
                    selectedDiscount = discount;
                    break;
                }
            }
        }

        // Nếu danh sách giảm giá trống, thêm các giảm giá mặc định
        if (allDiscounts.isEmpty()) {
            discountDao.addDiscount(new Discount(0, "Giảm giá 50%", 0.50, 900000.0, "Áp dụng cho đơn hàng tối thiểu 900.000vnd", true));
            discountDao.addDiscount(new Discount(0, "Giảm giá 40%", 0.40, 800000.0, "Áp dụng cho đơn hàng tối thiểu 800.000vnd", true));
            discountDao.addDiscount(new Discount(0, "Giảm giá 30%", 0.30, 500000.0, "Áp dụng cho đơn hàng tối thiểu 500.000vnd", true));
            discountDao.addDiscount(new Discount(0, "Giảm giá 15%", 0.15, 200000.0, "Áp dụng cho đơn hàng tối thiểu 200.000vnd", true));
            discountDao.addDiscount(new Discount(0, "Giảm giá 10%", 0.10, 0.0, "Áp dụng cho mọi đơn hàng", true));

            // Cập nhật lại danh sách sau khi thêm mới
            allDiscounts = discountDao.getAllActiveDiscounts();
            promotionsAdapter.updateData(allDiscounts, currentCartTotal, previouslySelectedDiscountId);
        }
    }

    @Override
    public void onPromotionSelected(Discount promotion) {
        selectedDiscount = promotion;
        Toast.makeText(this, "Đã chọn: " + promotion.getName(), Toast.LENGTH_SHORT).show();
        sendResultBackToCartActivity();
    }

    @Override
    public void onPromotionDeselected() {
        selectedDiscount = null;
        Toast.makeText(this, "Đã bỏ chọn khuyến mại", Toast.LENGTH_SHORT).show();
        sendResultBackToCartActivity();
    }

    private void sendResultBackToCartActivity() {
        Intent resultIntent = new Intent();
        if (selectedDiscount != null) {
            resultIntent.putExtra(EXTRA_SELECTED_DISCOUNT_ID, selectedDiscount.getId());
            resultIntent.putExtra(EXTRA_SELECTED_DISCOUNT_PERCENTAGE, selectedDiscount.getDiscountPercentage());
        } else {
            resultIntent.putExtra(EXTRA_SELECTED_DISCOUNT_ID, -1);
            resultIntent.putExtra(EXTRA_SELECTED_DISCOUNT_PERCENTAGE, 0.0);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        sendResultBackToCartActivity();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (discountDao != null) {
            discountDao.close();
        }
    }
}