package com.rituraj.sevamitra.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.ui.auth.LoginActivity;
import com.rituraj.sevamitra.ui.dashboard.fragments.ProfileFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.FounderHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.SevaMitraHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.OfficerHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.WorkerHomeFragment;

public class BaseDashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private String userType = "";
    private Fragment selectedFragment = null;
    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_dashboard);

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
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = getHomeFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = getProfileFragment();
            }

            if (fragment == null) return true;

            if (fragment.getClass().equals(selectedFragment != null ? selectedFragment.getClass() : null)) {
                return true;
            }
            this.selectedFragment = fragment;
            loadFragment(fragment);
            return true;
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
        if (userType.equalsIgnoreCase("FOUNDER")) return new FounderHomeFragment();
        else if (userType.equalsIgnoreCase("SEVAMITRA")) return new SevaMitraHomeFragment();
        else if (userType.equalsIgnoreCase("OFFICER")) return new OfficerHomeFragment();
        else return new WorkerHomeFragment();
    }

    protected Fragment getProfileFragment() {
        return new ProfileFragment();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}