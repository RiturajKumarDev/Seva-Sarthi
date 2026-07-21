package com.rituraj.sevamitra.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.auth.LoginActivity;
import com.rituraj.sevamitra.ui.dashboard.fragments.ProfileFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.FounderHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.SevaSarthiHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.OfficerHomeFragment;
import com.rituraj.sevamitra.ui.dashboard.fragments.home.WorkerHomeFragment;

public class BaseDashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private String userType = "";
    private Fragment selectedFragment = null;
    protected BottomNavigationView bottomNavigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_dashboard);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        toolbar = findViewById(R.id.toolbar);
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
        toolbar.setOnClickListener((v) -> {
            if (firebaseUser.getEmail().equalsIgnoreCase("riturajkumar1105@gmail.com") || firebaseUser.getEmail().equalsIgnoreCase("sujeetkumarbth09@gmail.com"))
                startActivity(new Intent(BaseDashboardActivity.this, AdminUserManagementActivity.class));
        });
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
        else if (userType.equalsIgnoreCase("WORKER")) return new WorkerHomeFragment();
        else if (userType.equalsIgnoreCase("OFFICER")) return new OfficerHomeFragment();
        else return new SevaSarthiHomeFragment();
    }

    protected Fragment getProfileFragment() {
        return new ProfileFragment();
    }
}