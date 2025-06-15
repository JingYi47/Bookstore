package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhvan.bookstoremanager.model.User; // Import lớp User
import com.thanhvan.bookstoremanager.sqlite.UserDao; // Import lớp UserDao

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText; // Sẽ dùng làm username
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView; // Đã đổi tên để khớp với XML của bạn

    private UserDao userDao; // Khai báo UserDao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Layout của bạn

        // Ánh xạ các thành phần UI
        emailEditText = findViewById(R.id.emailEditText); // Khớp với ID trong XML của bạn
        passwordEditText = findViewById(R.id.passwordEditText); // Khớp với ID trong XML của bạn
        loginButton = findViewById(R.id.loginButton); // Khớp với ID trong XML của bạn
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView); // Khớp với ID trong XML của bạn
        registerTextView = findViewById(R.id.registerTextView); // Khớp với ID trong XML của bạn

        // Khởi tạo UserDao
        userDao = new UserDao(this);

        // Xử lý sự kiện khi click nút Đăng Nhập
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(); // Gọi phương thức xử lý đăng nhập
            }
        });

        // Xử lý sự kiện khi click Quên mật khẩu?
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Chức năng quên mật khẩu đang được phát triển.", Toast.LENGTH_SHORT).show();
                // TODO: Implement Forgot Password Activity
            }
        });

        // Xử lý sự kiện khi click Đăng Ký
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến màn hình đăng ký.
                // Lưu ý: Tên Activity đăng ký của bạn là RegistrationActivity, không phải RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish(); // Đóng LoginActivity để người dùng không quay lại đây khi nhấn Back
            }
        });
    }

    private void loginUser() {
        String username = emailEditText.getText().toString().trim(); // Email của bạn sẽ là username
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ email (tên đăng nhập) và mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thay thế logic đăng nhập cứng bằng UserDao
        User user = userDao.checkLogin(username, password);

        if (user != null) {
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Chào mừng " + user.getUsername(), Toast.LENGTH_SHORT).show();
            // Chuyển sang HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("loggedInUsername", user.getUsername()); // Truyền username nếu cần
            startActivity(intent);
            finish(); // Đóng LoginActivity
        } else {
            Toast.makeText(LoginActivity.this, "Email (tên đăng nhập) hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng kết nối database khi Activity bị hủy
        if (userDao != null) {
            userDao.close();
        }
    }
}