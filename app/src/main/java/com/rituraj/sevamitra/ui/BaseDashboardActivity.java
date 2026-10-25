package com.rituraj.sevamitra.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.ui.auth.LoginActivity;
import com.rituraj.sevamitra.ui.auth.ProfileFragment;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.vendor.DailyOrderListActivity;
import com.rituraj.sevamitra.ui.dashboard.AdminUserManagementActivity;
import com.rituraj.sevamitra.ui.dashboard.FounderHomeFragment;
import com.rituraj.sevamitra.ui.vendor.VendorHomeFragment;
import com.rituraj.sevamitra.ui.officer.OfficerHomeFragment;
import com.rituraj.sevamitra.ui.sevaSarthi.SevaSarthiHomeFragment;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.WorkerHomeFragment;

public class BaseDashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private String userType = "";
    private Fragment selectedFragment = null;

    protected BottomNavigationView bottomNavigationView;
    private ImageView ivLogo, ivAdminPanel, ivSupport;
    private TextView tvAppTitle, tvAppSubtitle, tvUserBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_dashboard);

        setupStatusBar();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(BaseDashboardActivity.this, LoginActivity.class));
            finish();
            return;
        }

        if (firebaseUser.getPhotoUrl() != null) {
            userType = firebaseUser.getPhotoUrl().toString();
        }

        initTopBar();
        setupBottomNavigation();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getColor(R.color.logo_dark_blue));
        window.setNavigationBarColor(getColor(R.color.logo_dark_blue));

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
            insetsController.setAppearanceLightNavigationBars(false);
        }
    }

    private void initTopBar() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        tvAppSubtitle = findViewById(R.id.tvAppSubtitle);
        tvUserBadge = findViewById(R.id.tvUserBadge);
        ivAdminPanel = findViewById(R.id.ivAdminPanel);
        ivSupport = findViewById(R.id.ivSupport);

        // User badge text
        if (userType != null && !userType.trim().isEmpty()) {
            tvUserBadge.setText(userType.toUpperCase());
            tvUserBadge.setVisibility(View.VISIBLE);
        } else {
            tvUserBadge.setVisibility(View.GONE);
        }

        // Check if Admin user
        boolean isAdmin = firebaseUser != null && firebaseUser.getEmail() != null &&
                (firebaseUser.getEmail().equalsIgnoreCase("riturajkumar1105@gmail.com") ||
                        firebaseUser.getEmail().equalsIgnoreCase("sujeetkumarbth09@gmail.com"));

        if (isAdmin) {
            ivAdminPanel.setVisibility(View.VISIBLE);
            tvUserBadge.setText("ADMIN");
            ivAdminPanel.setOnClickListener(v ->
                    startActivity(new Intent(BaseDashboardActivity.this, AdminUserManagementActivity.class)));
        } else {
            ivAdminPanel.setVisibility(View.GONE);
        }

        // Support click
        if (ivSupport != null) {
            ivSupport.setOnClickListener(v ->
                    startActivity(new Intent(BaseDashboardActivity.this, SupportActivity.class)));
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (selectedFragment == null || !selectedFragment.getClass().equals(getHomeFragment().getClass())) {
                    selectedFragment = getHomeFragment();
                    loadFragment(selectedFragment);
                }
                return true;
            } else if (itemId == R.id.nav_orders) {
                boolean isVendor = userType != null &&
                        (userType.equalsIgnoreCase("VENDOR") || userType.equalsIgnoreCase("WATER VENDOR"));
                if (isVendor) {
                    startActivity(new Intent(BaseDashboardActivity.this, DailyOrderListActivity.class));
                } else {
                    startActivity(new Intent(BaseDashboardActivity.this, DailyItemsActivity.class));
                }
                return false;
            } else if (itemId == R.id.nav_support) {
                startActivity(new Intent(BaseDashboardActivity.this, SupportActivity.class));
                return false;
            } else if (itemId == R.id.nav_profile) {
                if (selectedFragment == null || !selectedFragment.getClass().equals(getProfileFragment().getClass())) {
                    selectedFragment = getProfileFragment();
                    loadFragment(selectedFragment);
                }
                return true;
            }
            return false;
        });
        selectedFragment = getHomeFragment();
        loadFragment(selectedFragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    protected Fragment getHomeFragment() {
        tvUserBadge.setText(userType);
        if (userType.equalsIgnoreCase("FOUNDER")) return new FounderHomeFragment();
        else if (userType.equalsIgnoreCase("WORKER")) return new WorkerHomeFragment();
        else if (userType.equalsIgnoreCase("VENDOR") || userType.equalsIgnoreCase("WATER VENDOR"))
            return new VendorHomeFragment();
        else if (userType.equalsIgnoreCase("OFFICER")) return new OfficerHomeFragment();
        else return new SevaSarthiHomeFragment();
    }

    protected Fragment getProfileFragment() {
        return new ProfileFragment();
    }
}