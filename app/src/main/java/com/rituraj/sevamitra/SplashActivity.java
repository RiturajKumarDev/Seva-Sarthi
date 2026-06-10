package com.rituraj.sevamitra;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.models.User;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.auth.LoginActivity;
import com.rituraj.sevamitra.ui.auth.RegistrationActivity;
import com.rituraj.sevamitra.ui.dashboard.BaseDashboardActivity;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private static final int SPLASH_DURATION = 2800;
    private ImageView logoIcon, logoBg;
    private TextView appNameText, taglineText;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        // Initialize views
        logoIcon = findViewById(R.id.logoIcon);
        logoBg = findViewById(R.id.logoBg);
        appNameText = findViewById(R.id.appNameText);
        taglineText = findViewById(R.id.taglineText);
        progressBar = findViewById(R.id.progressBar);

        // Load animations
        Animation logoRotate = AnimationUtils.loadAnimation(this, R.anim.logo_rotate);
        Animation logoPulse = AnimationUtils.loadAnimation(this, R.anim.logo_pulse);
        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        Animation fadeInDown = AnimationUtils.loadAnimation(this, R.anim.fade_in_down);
        Animation progressAnimation = AnimationUtils.loadAnimation(this, R.anim.progress_animation);

        // Apply animations with sequence
        logoBg.startAnimation(logoRotate);
        logoIcon.startAnimation(logoPulse);
        appNameText.startAnimation(fadeInDown);
        taglineText.startAnimation(fadeInUp);
        progressBar.startAnimation(progressAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseUser == null) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    if (!isInternetAvailable()) {
                        Toast.makeText(SplashActivity.this, "No Internet Connection!!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    getUser();
                }
            }
        }, SPLASH_DURATION);
    }

    private void getUser() {
        reference = database.getReference().child("Users").child(firebaseUser.getUid());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getUserId() == null) {
                            user.setUserId(snapshot.getKey());
                        }
                        if (user.getUserType() != null) {
                            getUserData(user);
                        } else {
                            Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUserData(User user) {
        if (user.getUserType() == null || user.getUserId() == null) {
            Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        reference = database.getReference().child("UserData").child(user.getUserType()).child(user.getUserId());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        Intent intent = new Intent(SplashActivity.this, BaseDashboardActivity.class);
                        intent.putExtra("UserType", userData.getUserType());
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null)
            return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}