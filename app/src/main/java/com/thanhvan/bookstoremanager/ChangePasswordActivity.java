package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.thanhvan.bookstoremanager.model.User;
import com.thanhvan.bookstoremanager.sqlite.UserDao;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button changePasswordButton;

    private UserDao userDao;
    private SharedPreferences sharedPreferences;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userDao = new UserDao(this);
        sharedPreferences = getSharedPreferences(AppConstants.APP_PREFS, Context.MODE_PRIVATE);

        backButton = findViewById(R.id.button_change_password_back);
        oldPasswordEditText = findViewById(R.id.edit_text_old_password);
        newPasswordEditText = findViewById(R.id.edit_text_new_password);
        confirmNewPasswordEditText = findViewById(R.id.edit_text_confirm_new_password);
        changePasswordButton = findViewById(R.id.button_change_password);

        loggedInUsername = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USERNAME, null);

        if (loggedInUsername == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> finish());

        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            newPasswordEditText.setError("Mật khẩu mới không khớp");
            confirmNewPasswordEditText.setError("Mật khẩu mới không khớp");
            newPasswordEditText.requestFocus();
            Toast.makeText(this, "Mật khẩu mới và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        userDao.open();
        User user = userDao.getUserByUsername(loggedInUsername);
        userDao.close();

        if (user == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng trong hệ thống.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!user.getPassword().equals(oldPassword)) {
            Toast.makeText(this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
            oldPasswordEditText.setError("Mật khẩu cũ không chính xác");
            oldPasswordEditText.requestFocus();
            return;
        }

        userDao.open();
        boolean updateSuccess = userDao.updateUserPassword(loggedInUsername, newPassword);
        userDao.close();

        if (updateSuccess) {
            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Đổi mật khẩu thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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