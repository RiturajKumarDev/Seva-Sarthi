package com.rituraj.sevamitra.ui.selections;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.VendorAdapter;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.issues.AddDailyIssueActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;

import java.util.ArrayList;

public class VendorSelectionActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String userId;
    private String selectedProblemType = "";

    private ImageView ivBack, ivSupport;
    private TextView tvTopSubtitle, tvEmptyMessage;
    private EditText etSearch;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private RecyclerView rvVendors;

    private VendorAdapter vendorAdapter;
    private ArrayList<UserData> vendorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vender_selection);

        setupStatusBar();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        }

        if (!getIntent().hasExtra("REQUEST_DEPARTMENT")) {
            finish();
            return;
        }
        selectedProblemType = getIntent().getStringExtra("REQUEST_DEPARTMENT");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        getVendorsData();

        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
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
        ivBack = findViewById(R.id.ivBack);
        ivSupport = findViewById(R.id.ivSupport);
        tvTopSubtitle = findViewById(R.id.tvTopSubtitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        rvVendors = findViewById(R.id.rvVendors);

        if (selectedProblemType != null && !selectedProblemType.isEmpty()) {
            tvTopSubtitle.setText("Category: " + selectedProblemType);
        }
    }

    private void setupToolbar() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        if (ivSupport != null) {
            ivSupport.setOnClickListener(v -> startActivity(new Intent(VendorSelectionActivity.this, SupportActivity.class)));
        }
    }

    private void setupRecyclerView() {
        rvVendors.setLayoutManager(new LinearLayoutManager(this));
        vendorAdapter = new VendorAdapter(vendorList, new VendorAdapter.OnVendorClickListener() {
            @Override
            public void onVendorClick(UserData vendor) {
                navigateToAddDailyIssue(vendor);
            }

            @Override
            public void onContactClick(UserData vendor) {
                if (vendor.getPhone() != null && !vendor.getPhone().isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vendor.getPhone()));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(VendorSelectionActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvVendors.setAdapter(vendorAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vendorAdapter != null) {
                    vendorAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void navigateToAddDailyIssue(UserData vendor) {
        Intent intent = new Intent(VendorSelectionActivity.this, AddDailyIssueActivity.class);
        intent.putExtra("REQUEST_DEPARTMENT", selectedProblemType);
        if (vendor != null) {
            intent.putExtra("SELECTED_VENDOR_ID", vendor.getId());
            intent.putExtra("SELECTED_VENDOR_NAME", vendor.getFullName());
        }
        startActivity(intent);
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");
        return new LanguageModel(name, code);
    }

    private void translationViews() {
        LanguageManager.init(getSavedLanguage(this).code, () -> LanguageManager.translateView(getWindow().getDecorView()));
    }

    private void getVendorsData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        rvVendors.setVisibility(View.GONE);

        reference = FirebaseDatabase.getInstance().getReference().child("UserData").child("VENDOR");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                vendorList.clear();
                ArrayList<UserData> allVendors = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            allVendors.add(userData);

                            if (userData.getDepartment() != null && selectedProblemType != null &&
                                    (userData.getDepartment().equalsIgnoreCase(selectedProblemType) || userData.getDepartment().equalsIgnoreCase("other"))) {
                                vendorList.add(userData);
                            }
                        }
                    }
                }

                // Fallback: If no vendors matched the specific department, show all available vendors
//                if (vendorList.isEmpty() && !allVendors.isEmpty()) {
//                    vendorList.addAll(allVendors);
//                }

                if (vendorList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    rvVendors.setVisibility(View.GONE);
                    if (selectedProblemType != null && !selectedProblemType.isEmpty()) {
                        tvEmptyMessage.setText("No vendors available for " + selectedProblemType);
                    }
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    rvVendors.setVisibility(View.VISIBLE);
                    vendorAdapter.updateData(vendorList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvVendors.setVisibility(View.GONE);
                Toast.makeText(VendorSelectionActivity.this, "Failed to load vendors: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
