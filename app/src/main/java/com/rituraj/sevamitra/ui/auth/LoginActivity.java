package com.rituraj.sevamitra.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.ui.dashboard.BaseDashboardActivity;
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

    private void initViews() {
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
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);

        cardLogin.startAnimation(slideUp);
        tvAppName.startAnimation(fadeIn);
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