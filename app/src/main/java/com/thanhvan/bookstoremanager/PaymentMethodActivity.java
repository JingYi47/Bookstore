package com.thanhvan.bookstoremanager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageView backButton;
    private RadioGroup paymentMethodsRadioGroup;
    private Button selectMethodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        backButton = findViewById(R.id.button_payment_back);
        paymentMethodsRadioGroup = findViewById(R.id.radio_group_payment_methods);
        selectMethodButton = findViewById(R.id.button_select_payment_method);

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> finish());

        // Mặc định chọn Cash on Delivery
        paymentMethodsRadioGroup.check(R.id.radio_cash_on_delivery);

        selectMethodButton.setOnClickListener(v -> {
            int selectedId = paymentMethodsRadioGroup.getCheckedRadioButtonId();
            String selectedMethod = "";

            if (selectedId == R.id.radio_cash_on_delivery) {
                selectedMethod = "Thanh toán tiền mặt";
            } else if (selectedId == R.id.radio_credit_debit_card) {
                selectedMethod = "Credit or debit card";
            } else if (selectedId == R.id.radio_bank_transfer) {
                selectedMethod = "Chuyển khoản ngân hàng";
            } else if (selectedId == R.id.radio_zalopay) {
                selectedMethod = "ZaloPay";
            } else {
                Toast.makeText(this, "Vui lòng chọn một phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_payment_method", selectedMethod);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}