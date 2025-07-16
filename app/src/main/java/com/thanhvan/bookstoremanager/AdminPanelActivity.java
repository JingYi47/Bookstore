package com.thanhvan.bookstoremanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thanhvan.bookstoremanager.CategoryManagementFragment;
import com.thanhvan.bookstoremanager.ProductManagementFragment;
/*import com.thanhvan.bookstoremanager.OrderManagementFragment;*/
import com.thanhvan.bookstoremanager.AdminSettingsFragment;

public class AdminPanelActivity extends AppCompatActivity {
    private BottomNavigationView adminBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        adminBottomNavigation = findViewById(R.id.admin_bottom_navigation);
        adminBottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);
        if (savedInstanceState == null) {
            loadFragment(new ProductManagementFragment());
            adminBottomNavigation.setSelectedItemId(R.id.nav_admin_products);
        }
    }
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null; int itemId = item.getItemId();
        if (itemId == R.id.nav_admin_categories) { fragment = new CategoryManagementFragment(); }
        else if (itemId == R.id.nav_admin_products) { fragment = new ProductManagementFragment(); }
        else if (itemId == R.id.nav_admin_orders) { fragment = new OrderListByStatusFragment();}


        else if (itemId == R.id.nav_admin_settings) { fragment = new AdminSettingsFragment(); }
        if (fragment != null) { loadFragment(fragment); return true; } return false;
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.admin_fragment_container, fragment);
        fragmentTransaction.commit();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {

            new AlertDialog.Builder(this)
                    .setTitle("Thoát ứng dụng")
                    .setMessage("Bạn có muốn thoát khỏi trang Admin không?")
                    .setPositiveButton("Có", (dialog, which) -> finish())
                    .setNegativeButton("Không", null)
                    .show();
        }
    }
}