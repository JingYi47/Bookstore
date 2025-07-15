package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhvan.bookstoremanager.model.User;
import com.thanhvan.bookstoremanager.sqlite.UserDao;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        registerTextView = findViewById(R.id.registerTextView);

        userDao = new UserDao(this);
        userDao.open();

        loginButton.setOnClickListener(v -> loginUser());

        forgotPasswordTextView.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Chức năng quên mật khẩu đang được phát triển.", Toast.LENGTH_SHORT).show());

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String usernameOrEmail = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ email và mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userDao.checkLogin(usernameOrEmail, password);

        if (user != null) {
            // Sửa lại để dùng hằng số từ AppConstants
            SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, user.getEmail());
            editor.putString(AppConstants.KEY_LOGGED_IN_USERNAME, user.getUsername());
            editor.putBoolean(AppConstants.KEY_IS_LOGGED_IN, true);
            editor.apply();

            Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Chào mừng " + user.getUsername(), Toast.LENGTH_SHORT).show();

            Intent intent;
            if ("admin".equalsIgnoreCase(user.getRole())) {
                intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, HomeActivity.class);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Email (tên đăng nhập) hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }
}