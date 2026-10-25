package com.rituraj.sevamitra.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.ui.BaseDashboardActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private Spinner spinnerLanguage;
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin, btnRegister;
    private TextView tvForgotPassword, tvSignUp, tvError, tvAppName, tvSupport;
    private ProgressBar progressBar;
    private CardView cardLogin;
    private ImageView ivShowPassword;
    private View bgCircle1, bgCircle2, bgCircle3, logoContainer;

    // Animations
    private Animation fadeIn, slideUp, shake;

    // Firebase
    private FirebaseAuth mAuth;

    // Password visibility
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupStatusBar();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            navigateToDashboard();
        }

        initViews();
        loadAnimations();
        setupClickListeners();
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
        bgCircle1 = findViewById(R.id.bgCircle1);
        bgCircle2 = findViewById(R.id.bgCircle2);
        bgCircle3 = findViewById(R.id.bgCircle3);
        logoContainer = findViewById(R.id.logoContainer);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvSupport = findViewById(R.id.tvSupport);
        tvError = findViewById(R.id.tvError);
        tvAppName = findViewById(R.id.tvAppName);
        progressBar = findViewById(R.id.progressBar);
        cardLogin = findViewById(R.id.cardLogin);
        ivShowPassword = findViewById(R.id.ivShowPassword);

        ArrayAdapter<LanguageModel> arrayAdapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_list_item_1, LanguageModel.getLanguageModelArrayList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(arrayAdapter);
    }

    private void saveLanguage(Context context, LanguageModel language) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("language_name", language.name);
        editor.putString("language_code", language.code);
        editor.apply();
    }

    private void loadAnimations() {
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);

        // 1. Background Floating Circles Animation (Rotation + Scale Pulse)
        if (bgCircle1 != null) {
            bgCircle1.animate()
                    .rotation(360f)
                    .setDuration(25000)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isFinishing() && bgCircle1 != null) bgCircle1.setRotation(0f);
                        }
                    });
            bgCircle1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.logo_pulse));
        }

        if (bgCircle2 != null) {
            bgCircle2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.logo_pulse));
        }

        // 2. Logo Spring Scale Entrance
        if (logoContainer != null) {
            logoContainer.setScaleX(0.2f);
            logoContainer.setScaleY(0.2f);
            logoContainer.setAlpha(0f);
            logoContainer.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setStartDelay(100)
                    .setInterpolator(new OvershootInterpolator(1.4f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isFinishing() && logoContainer != null) {
                                logoContainer.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.logo_pulse));
                            }
                        }
                    })
                    .start();
        }

        // 3. App Name Fade In Down
        if (tvAppName != null) {
            tvAppName.setTranslationY(-40f);
            tvAppName.setAlpha(0f);
            tvAppName.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(700)
                    .setStartDelay(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 4. Card Login Spring Slide Up
        if (cardLogin != null) {
            cardLogin.setTranslationY(120f);
            cardLogin.setAlpha(0f);
            cardLogin.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setStartDelay(400)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
        }

        // 5. Staggered Form Elements Entrance
        View[] formItems = new View[]{spinnerLanguage, tilEmail, tilPassword, btnLogin, btnRegister};
        long delay = 500;
        for (View item : formItems) {
            if (item != null) {
                item.setAlpha(0f);
                item.setTranslationY(30f);
                item.animate()
                        .alpha(1.0f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(delay)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                delay += 80;
            }
        }

        // 6. Touch Micro-Interactions on Buttons
        setupButtonTouchAnimations(btnLogin);
        setupButtonTouchAnimations(btnRegister);
    }

    private void setupButtonTouchAnimations(View button) {
        if (button == null) return;
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(120).setInterpolator(new DecelerateInterpolator()).start();
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).setInterpolator(new OvershootInterpolator(1.2f)).start();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).setInterpolator(new OvershootInterpolator(1.2f)).start();
                    break;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            saveLanguage(LoginActivity.this, (LanguageModel) spinnerLanguage.getSelectedItem());
            loginUser();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password? Contact Admin", Toast.LENGTH_SHORT).show();
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        tvSupport.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SupportActivity.class);
            startActivity(intent);
        });

        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShowPassword.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            // Show password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivShowPassword.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        // Move cursor to end
        etPassword.setSelection(etPassword.getText().length());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading
        setLoading(true);
        tvError.setVisibility(View.GONE);

        // Login with Firebase
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                // Login success
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    navigateToDashboard();
                }
            } else {
                // Login failed
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                showError(errorMessage);
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, BaseDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!isValid) {
            cardLogin.startAnimation(shake);
        }

        return isValid;
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        cardLogin.startAnimation(shake);

        // Auto hide error after 3 seconds
        new Handler().postDelayed(() -> tvError.setVisibility(View.GONE), 3000);
    }
}