package com.rituraj.sevamitra.ui.sevaSarthi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.SevaSarthiAdapter;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class SevaSarthiListActivity extends AppCompatActivity implements SevaSarthiAdapter.OnSevaMitraClickListener {

    // Views
    private Toolbar toolbar;
    private RecyclerView rvSevaMitra;
    private ProgressBar progressBar;
    private TextView tvNoData, tvTotalSevaMitra;
    private EditText etSearch;
    private ImageView ivSort, ivFilter;
    private ChipGroup chipGroup;
    private Chip chipAll, chipActive, chipInactive;
    private LinearLayout sortOptionsLayout;
    private CardView cardFilter;
    private Button btnClearFilters;

    // Statistics
    private TextView tvTotalCount, tvActiveCount, tvInactiveCount;
    private CardView cardTotal, cardActive, cardInactive;

    // Adapter and Data
    private SevaSarthiAdapter sevaMitraAdapter;
    private List<UserData> sevaMitraList = new ArrayList<>();
    private List<UserData> sevaMitraListFull = new ArrayList<>();

    // Filters
    private String currentStatusFilter = "all";
    private String currentSort = "name_asc";

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seva_mitra_list);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupSortAndFilter();
        loadSevaMitraFromFirebase();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        toolbar = findViewById(R.id.toolbar);
        rvSevaMitra = findViewById(R.id.rvSevaMitra);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        tvTotalSevaMitra = findViewById(R.id.tvTotalSevaMitra);
        etSearch = findViewById(R.id.etSearch);
        ivSort = findViewById(R.id.ivSort);
        ivFilter = findViewById(R.id.ivFilter);
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipInactive = findViewById(R.id.chipInactive);
        sortOptionsLayout = findViewById(R.id.sortOptionsLayout);
        cardFilter = findViewById(R.id.cardFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);

        // Statistics
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvInactiveCount = findViewById(R.id.tvInactiveCount);
        cardTotal = findViewById(R.id.cardTotal);
        cardActive = findViewById(R.id.cardActive);
        cardInactive = findViewById(R.id.cardInactive);
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

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SevaSarthi List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        sevaMitraAdapter = new SevaSarthiAdapter(sevaMitraList, this);
        rvSevaMitra.setLayoutManager(new LinearLayoutManager(this));
        rvSevaMitra.setAdapter(sevaMitraAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSevaMitra(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortAndFilter() {
        swipeRefresh.setOnRefreshListener(SevaSarthiListActivity.this::loadSevaMitraFromFirebase);
        // Sort button click
        ivSort.setOnClickListener(v -> {
            if (sortOptionsLayout.getVisibility() == View.VISIBLE) {
                sortOptionsLayout.setVisibility(View.GONE);
            } else {
                sortOptionsLayout.setVisibility(View.VISIBLE);
                cardFilter.setVisibility(View.GONE);
            }
        });

        // Filter button click
        ivFilter.setOnClickListener(v -> {
            if (cardFilter.getVisibility() == View.VISIBLE) {
                cardFilter.setVisibility(View.GONE);
            } else {
                cardFilter.setVisibility(View.VISIBLE);
                sortOptionsLayout.setVisibility(View.GONE);
            }
        });

        // Filter Chips
        chipAll.setOnClickListener(v -> {
            currentStatusFilter = "all";
            updateChipSelection(chipAll);
            applyFilters();
        });

        chipActive.setOnClickListener(v -> {
            currentStatusFilter = "active";
            updateChipSelection(chipActive);
            applyFilters();
        });

        chipInactive.setOnClickListener(v -> {
            currentStatusFilter = "inactive";
            updateChipSelection(chipInactive);
            applyFilters();
        });

        // Clear filters
        btnClearFilters.setOnClickListener(v -> {
            currentStatusFilter = "all";
            updateChipSelection(chipAll);
            applyFilters();
            cardFilter.setVisibility(View.GONE);
        });

        // Sort options
        setupSortOptions();
    }

    private void setupSortOptions() {
        LinearLayout sortByNameAsc = findViewById(R.id.sortByNameAsc);
        LinearLayout sortByNameDesc = findViewById(R.id.sortByNameDesc);
        LinearLayout sortByDepartment = findViewById(R.id.sortByDepartment);
        LinearLayout sortByDesignation = findViewById(R.id.sortByDesignation);
        LinearLayout sortByCity = findViewById(R.id.sortByCity);

        sortByNameAsc.setOnClickListener(v -> {
            currentSort = "name_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByNameDesc.setOnClickListener(v -> {
            currentSort = "name_desc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByDepartment.setOnClickListener(v -> {
            currentSort = "department_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByDesignation.setOnClickListener(v -> {
            currentSort = "designation_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByCity.setOnClickListener(v -> {
            currentSort = "city_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipActive.setChecked(false);
        chipInactive.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void applyFilters() {
        List<UserData> filteredList = new ArrayList<>();

        for (UserData sevaMitra : sevaMitraListFull) {
            boolean statusMatch = false;

            switch (currentStatusFilter) {
                case "all":
                    statusMatch = true;
                    break;
                case "active":
                    statusMatch = true;
                    break;
                case "inactive":
                    statusMatch = false;
                    break;
            }

            if (statusMatch) {
                filteredList.add(sevaMitra);
            }
        }

        sevaMitraList.clear();
        sevaMitraList.addAll(filteredList);
        applySort();
        updateStatistics();
        updateNoDataVisibility();
    }

    private void filterSevaMitra(String query) {
        sevaMitraList.clear();
        if (query.isEmpty()) {
            sevaMitraList.addAll(sevaMitraListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserData sevaMitra : sevaMitraListFull) {
                if (sevaMitra.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        sevaMitra.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        sevaMitra.getPhone().contains(lowerCaseQuery) ||
                        (sevaMitra.getDepartment() != null && sevaMitra.getDepartment().toLowerCase().contains(lowerCaseQuery)) ||
                        (sevaMitra.getDesignation() != null && sevaMitra.getDesignation().toLowerCase().contains(lowerCaseQuery)) ||
                        (sevaMitra.getCity() != null && sevaMitra.getCity().toLowerCase().contains(lowerCaseQuery)) ||
                        (sevaMitra.getState() != null && sevaMitra.getState().toLowerCase().contains(lowerCaseQuery))) {
                    sevaMitraList.add(sevaMitra);
                }
            }
        }
        applySort();
        updateStatistics();
        updateNoDataVisibility();
    }

    private void applySort() {
        switch (currentSort) {
            case "name_asc":
                sevaMitraList.sort((s1, s2) -> s1.getFullName().compareTo(s2.getFullName()));
                break;
            case "name_desc":
                sevaMitraList.sort((s1, s2) -> s2.getFullName().compareTo(s1.getFullName()));
                break;
            case "department_asc":
                sevaMitraList.sort((s1, s2) -> {
                    String d1 = s1.getDepartment() != null ? s1.getDepartment() : "";
                    String d2 = s2.getDepartment() != null ? s2.getDepartment() : "";
                    return d1.compareTo(d2);
                });
                break;
            case "designation_asc":
                sevaMitraList.sort((s1, s2) -> {
                    String d1 = s1.getDesignation() != null ? s1.getDesignation() : "";
                    String d2 = s2.getDesignation() != null ? s2.getDesignation() : "";
                    return d1.compareTo(d2);
                });
                break;
            case "city_asc":
                sevaMitraList.sort((s1, s2) -> {
                    String c1 = s1.getCity() != null ? s1.getCity() : "";
                    String c2 = s2.getCity() != null ? s2.getCity() : "";
                    return c1.compareTo(c2);
                });
                break;
        }
        sevaMitraAdapter.notifyDataSetChanged();
    }

    private void loadSevaMitraFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        sevaMitraList.clear();
        sevaMitraListFull.clear();
        reference = database.getReference().child("UserData").child("SEVASARTHI");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                swipeRefresh.setRefreshing(false);
                if (snapshot.exists()) {
                    UserData officer = snapshot.getValue(UserData.class);
                    if (officer != null) {
                        progressBar.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.GONE);
                        rvSevaMitra.setVisibility(View.VISIBLE);
                        officer.setId(snapshot.getKey());
                        sevaMitraList.add(officer);
                        sevaMitraListFull.add(officer);
                        updateStatistics();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SevaSarthiListActivity.this, "Error loading officers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (sevaMitraListFull.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int total = sevaMitraListFull.size();
        tvTotalSevaMitra.setText("Total SevaMitra: " + total);
        tvTotalCount.setText(String.valueOf(total));
        tvActiveCount.setText(String.valueOf(total));
        tvInactiveCount.setText("0");
    }

    private void updateNoDataVisibility() {
        if (sevaMitraList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvSevaMitra.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvSevaMitra.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSevaMitraClick(UserData sevaMitra) {
        showSevaMitraDetails(sevaMitra);
    }

    @Override
    public void onContactClick(UserData sevaMitra) {
        Toast.makeText(this, "Contact: " + sevaMitra.getPhone(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClick(UserData sevaMitra) {
        showSevaMitraDetails(sevaMitra);
    }

    private void showSevaMitraDetails(UserData sevaMitra) {
        String message = "🌟 SevaMitra Details\n\n" +
                "Name: " + sevaMitra.getFullName() + "\n" +
                "Email: " + sevaMitra.getEmail() + "\n" +
                "Phone: " + sevaMitra.getPhone() + "\n" +
                "Department: " + (sevaMitra.getDepartment() != null ? sevaMitra.getDepartment() : "N/A") + "\n" +
                "Designation: " + (sevaMitra.getDesignation() != null ? sevaMitra.getDesignation() : "N/A") + "\n" +
                "City: " + (sevaMitra.getCity() != null ? sevaMitra.getCity() : "N/A") + "\n" +
                "State: " + (sevaMitra.getState() != null ? sevaMitra.getState() : "N/A") + "\n" +
                "Address: " + (sevaMitra.getAddress() != null ? sevaMitra.getAddress() : "N/A");

        new android.app.AlertDialog.Builder(this)
                .setTitle("SevaMitra Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}