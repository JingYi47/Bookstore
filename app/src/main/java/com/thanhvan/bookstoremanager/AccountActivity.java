package com.thanhvan.bookstoremanager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhvan.bookstoremanager.sqlite.UserDao;

public class AccountActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView userEmailTextView;
    private LinearLayout layoutFeedback;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutLogout;

    private UserDao userDao;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        userDao = new UserDao(this);
        // >>> ĐÃ SỬA: Dùng AppConstants.APP_PREFS để nhất quán với LoginActivity <<<
        sharedPreferences = getSharedPreferences(AppConstants.APP_PREFS, Context.MODE_PRIVATE);

        backButton = findViewById(R.id.button_account_back);
        userEmailTextView = findViewById(R.id.text_view_user_email);
        layoutFeedback = findViewById(R.id.layout_feedback);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        layoutLogout = findViewById(R.id.layout_logout);

        displayUserEmail();

        backButton.setOnClickListener(v -> finish());

        layoutFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        layoutLogout.setOnClickListener(v -> {
            performLogout();
        });
    }

    private void displayUserEmail() {
        // >>> ĐÃ SỬA: Dùng AppConstants.KEY_LOGGED_IN_USER_EMAIL để lấy email <<<
        String loggedInEmail = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, "Không có email");
        userEmailTextView.setText(loggedInEmail);
    }

    private void performLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Nên xóa các key liên quan đến đăng nhập thay vì editor.clear() toàn bộ file nếu có dữ liệu khác
        editor.remove(AppConstants.KEY_LOGGED_IN_USER_EMAIL);
        editor.remove(AppConstants.KEY_LOGGED_IN_USERNAME);
        editor.remove(AppConstants.KEY_IS_LOGGED_IN);
        editor.apply();

        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserEmail();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }
}