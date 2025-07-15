package com.thanhvan.bookstoremanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhvan.bookstoremanager.model.Feedback;
import com.thanhvan.bookstoremanager.sqlite.FeedbackDao;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText contentEditText;
    private Button sendFeedbackButton;

    private SharedPreferences sharedPreferences;
    private FeedbackDao feedbackDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        feedbackDao = new FeedbackDao(this);

        backButton = findViewById(R.id.button_feedback_back);
        nameEditText = findViewById(R.id.edit_text_feedback_name);
        phoneEditText = findViewById(R.id.edit_text_feedback_phone);
        emailEditText = findViewById(R.id.edit_text_feedback_email);
        contentEditText = findViewById(R.id.edit_text_feedback_content);
        sendFeedbackButton = findViewById(R.id.button_send_feedback);

        String loggedInEmail = sharedPreferences.getString("logged_in_email", "");
        emailEditText.setText(loggedInEmail);

        backButton.setOnClickListener(v -> finish());

        sendFeedbackButton.setOnClickListener(v -> sendFeedback());
    }

    private void sendFeedback() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Họ và tên không được để trống");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(content)) {
            contentEditText.setError("Nội dung không được để trống");
            contentEditText.requestFocus();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Feedback feedback = new Feedback(name, phone, email, content, timestamp);

        long result = feedbackDao.addFeedback(feedback);

        if (result != -1) {
            Toast.makeText(this, "Phản hồi của bạn đã được gửi. Cảm ơn!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Gửi phản hồi thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (feedbackDao != null) {
            feedbackDao.close();
        }
    }
}