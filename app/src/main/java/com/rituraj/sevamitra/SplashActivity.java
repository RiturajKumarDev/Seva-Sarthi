package com.rituraj.sevamitra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.rituraj.sevamitra.ui.BaseDashboardActivity;
import com.rituraj.sevamitra.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private static final int SPLASH_DURATION = 3000;

    private View circle1, circle2, circle3, logoContainer, gradientLine, progressBar;
    private View chip1, chip2, chip3;
    private ImageView logoIcon, logoBg;
    private TextView appNameText, taglineText, tvServiceBadge, tvFooterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        setupStatusBar();

        try {
            com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
        }

        auth = FirebaseAuth.getInstance();

        initViews();
        loadMultipleAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
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

    private void initViews() {
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        circle3 = findViewById(R.id.circle3);
        logoContainer = findViewById(R.id.logoContainer);
        logoIcon = findViewById(R.id.logoIcon);
        logoBg = findViewById(R.id.logoBg);
        appNameText = findViewById(R.id.appNameText);
        taglineText = findViewById(R.id.taglineText);
        tvServiceBadge = findViewById(R.id.tvServiceBadge);
        tvFooterText = findViewById(R.id.tvFooterText);
        gradientLine = findViewById(R.id.gradientLine);
        progressBar = findViewById(R.id.progressBar);

        chip1 = findViewById(R.id.chip1);
        chip2 = findViewById(R.id.chip2);
        chip3 = findViewById(R.id.chip3);
    }

    private void loadMultipleAnimations() {
        Animation logoPulse = AnimationUtils.loadAnimation(this, R.anim.logo_pulse);

        // 1. Smooth Continuous Rotation for Rotating Ring (logoBg)
        if (logoBg != null) {
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(logoBg, "rotation", 0f, 360f);
            rotateAnim.setDuration(12000);
            rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnim.setInterpolator(new LinearInterpolator());
            rotateAnim.start();
        }

        // 2. Background Floating Circles Animation
        if (circle1 != null) {
            circle1.animate()
                    .rotation(360f)
                    .setDuration(22000)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isFinishing() && circle1 != null) circle1.setRotation(0f);
                        }
                    });
            circle1.startAnimation(logoPulse);
        }

        if (circle2 != null) {
            circle2.startAnimation(logoPulse);
        }

        if (circle3 != null) {
            circle3.animate()
                    .rotation(-360f)
                    .setDuration(18000)
                    .setInterpolator(new LinearInterpolator())
                    .start();
        }

        // 3. Service Pill Badge Fade In
        if (tvServiceBadge != null) {
            tvServiceBadge.setAlpha(0f);
            tvServiceBadge.setTranslationY(-30f);
            tvServiceBadge.animate()
                    .alpha(1.0f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 4. Logo Container Clean Spring Pop Entrance
        if (logoContainer != null) {
            logoContainer.setScaleX(0.0f);
            logoContainer.setScaleY(0.0f);
            logoContainer.setAlpha(0f);
            logoContainer.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setStartDelay(150)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isFinishing() && logoIcon != null) {
                                ObjectAnimator pulseX = ObjectAnimator.ofFloat(logoIcon, "scaleX", 1.0f, 1.06f, 1.0f);
                                ObjectAnimator pulseY = ObjectAnimator.ofFloat(logoIcon, "scaleY", 1.0f, 1.06f, 1.0f);
                                pulseX.setDuration(1600);
                                pulseY.setDuration(1600);
                                pulseX.setRepeatCount(ValueAnimator.INFINITE);
                                pulseY.setRepeatCount(ValueAnimator.INFINITE);
                                pulseX.setInterpolator(new DecelerateInterpolator());
                                pulseY.setInterpolator(new DecelerateInterpolator());
                                pulseX.start();
                                pulseY.start();
                            }
                        }
                    })
                    .start();
        }

        // 5. App Name Fade In Down
        if (appNameText != null) {
            appNameText.setTranslationY(-50f);
            appNameText.setAlpha(0f);
            appNameText.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setStartDelay(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 6. Tagline Fade In Up
        if (taglineText != null) {
            taglineText.setTranslationY(40f);
            taglineText.setAlpha(0f);
            taglineText.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 7. Feature Chips Staggered Cascade
        View[] chips = new View[]{chip1, chip2, chip3};
        long chipDelay = 650;
        for (View chip : chips) {
            if (chip != null) {
                chip.setAlpha(0f);
                chip.setScaleX(0.5f);
                chip.setScaleY(0.5f);
                chip.animate()
                        .alpha(1.0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(400)
                        .setStartDelay(chipDelay)
                        .setInterpolator(new OvershootInterpolator(1.4f))
                        .start();
                chipDelay += 90;
            }
        }

        // 8. Decorative Gold Line Scale Animation
        if (gradientLine != null) {
            gradientLine.setScaleX(0f);
            gradientLine.setAlpha(0f);
            gradientLine.animate()
                    .scaleX(1.0f)
                    .alpha(1.0f)
                    .setDuration(700)
                    .setStartDelay(900)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 9. Progress Bar & Footer Text
        if (progressBar != null) {
            progressBar.setAlpha(0f);
            progressBar.animate().alpha(1.0f).setDuration(600).setStartDelay(1000).start();
            Animation progressAnim = AnimationUtils.loadAnimation(this, R.anim.progress_animation);
            progressBar.startAnimation(progressAnim);
        }

        if (tvFooterText != null) {
            tvFooterText.setAlpha(0f);
            tvFooterText.animate().alpha(1.0f).setDuration(600).setStartDelay(1100).start();
        }
    }

    private void navigateToNextScreen() {
        Intent intent;
        if (auth.getCurrentUser() != null) {
            intent = new Intent(SplashActivity.this, BaseDashboardActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
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