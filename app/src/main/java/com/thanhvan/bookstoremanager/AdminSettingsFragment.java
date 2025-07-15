package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.thanhvan.bookstoremanager.LoginActivity;
import com.thanhvan.bookstoremanager.R;

public class AdminSettingsFragment extends Fragment {
    private TextView tvAdminEmail;

    private RelativeLayout layoutPromotions, layoutFeedback, layoutLogout;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAdminEmail = view.findViewById(R.id.tv_admin_email);

        layoutPromotions = view.findViewById(R.id.layout_promotions);
        layoutFeedback = view.findViewById(R.id.layout_feedback);
        layoutLogout = view.findViewById(R.id.layout_logout);

        tvAdminEmail.setText("main@admin.com");
        layoutPromotions.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển đến màn hình Chương trình khuyến mãi", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(getContext(), PromotionsManagementActivity.class);
            // startActivity(intent);
        });

        layoutFeedback.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển đến màn hình Phản hồi & Góp ý", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(getContext(), FeedbackManagementActivity.class);
            // startActivity(intent);
        });

        layoutLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}