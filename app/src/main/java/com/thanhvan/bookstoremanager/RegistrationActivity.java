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

public class RegistrationActivity extends AppCompatActivity { // Tên Activity của bạn

    private EditText regEmailEditText; // Sẽ dùng làm username
    private EditText regPasswordEditText;
    private EditText regConfirmPasswordEditText;
    private Button registerButton;
    private TextView backToLoginTextView; // Đã đổi tên để khớp với XML của bạn

    private UserDao userDao; // Khai báo UserDao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration); // Layout của bạn là activity_registration.xml

        // Ánh xạ các thành phần UI (khớp với ID trong XML của bạn)
        regEmailEditText = findViewById(R.id.regEmailEditText);
        regPasswordEditText = findViewById(R.id.regPasswordEditText);
        regConfirmPasswordEditText = findViewById(R.id.regConfirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView); // Khớp với ID trong XML của bạn

        // Khởi tạo UserDao
        userDao = new UserDao(this);

        // Xử lý sự kiện khi click nút Đăng Ký
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser(); // Gọi phương thức xử lý đăng ký
            }
        });

        // Xử lý sự kiện khi click "Đã có tài khoản? Đăng nhập"
        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Đóng RegistrationActivity để người dùng không thể quay lại bằng nút back
            }
        });
    }

    private void registerNewUser() { // Đổi tên phương thức để rõ ràng hơn
        String username = regEmailEditText.getText().toString().trim(); // Email của bạn sẽ là username
        String password = regPasswordEditText.getText().toString().trim();
        String confirmPassword = regConfirmPasswordEditText.getText().toString().trim();
        String email = username; // Coi email chính là username, hoặc bạn có thể thêm trường email riêng nếu có

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegistrationActivity.this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (userDao.isUsernameExists(username)) {
            Toast.makeText(RegistrationActivity.this, "Email (tên đăng nhập) đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng User mới
        User newUser = new User(username, password, email); // Email sẽ là username

        // Thực hiện đăng ký thông qua UserDao
        long result = userDao.registerUser(newUser);

        if (result != -1) {
            Toast.makeText(RegistrationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            // Sau khi đăng ký thành công, chuyển về màn hình đăng nhập
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng RegistrationActivity
        } else {
            Toast.makeText(RegistrationActivity.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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