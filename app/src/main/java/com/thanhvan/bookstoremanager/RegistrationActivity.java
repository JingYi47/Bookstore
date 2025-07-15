package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhvan.bookstoremanager.model.User;
import com.thanhvan.bookstoremanager.sqlite.UserDao;

public class RegistrationActivity extends AppCompatActivity {

    private EditText regEmailEditText;
    private EditText regPasswordEditText;
    private EditText regConfirmPasswordEditText;
    private Button registerButton;
    private TextView backToLoginTextView;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        regEmailEditText = findViewById(R.id.regEmailEditText);
        regPasswordEditText = findViewById(R.id.regPasswordEditText);
        regConfirmPasswordEditText = findViewById(R.id.regConfirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);

        userDao = new UserDao(this);
        userDao.open();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerNewUser() {
        String username = regEmailEditText.getText().toString().trim();
        String password = regPasswordEditText.getText().toString().trim();
        String confirmPassword = regConfirmPasswordEditText.getText().toString().trim();
        String email = username;

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegistrationActivity.this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDao.isUsernameExists(username)) {
            Toast.makeText(RegistrationActivity.this, "Email (tên đăng nhập) đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password, email, "user");

        long result = userDao.registerUser(newUser);

        if (result != -1) {
            Toast.makeText(RegistrationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(RegistrationActivity.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userDao != null) {
            userDao.open();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (userDao != null) {
            userDao.close();
        }
    }
}