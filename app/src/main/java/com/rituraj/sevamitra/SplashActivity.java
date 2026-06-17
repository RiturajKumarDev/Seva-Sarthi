package com.rituraj.sevamitra;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.ui.auth.LoginActivity;

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
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        }, SPLASH_DURATION);
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