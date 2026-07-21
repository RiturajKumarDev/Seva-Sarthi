package com.rituraj.sevamitra.ui.officer;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.OfficerAdapter;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class OfficerListActivity extends AppCompatActivity implements OfficerAdapter.OnOfficerClickListener {

    // Views
    private Toolbar toolbar;
    private RecyclerView rvOfficers;
    private ProgressBar progressBar;
    private TextView tvNoData, tvTotalOfficers;
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
    private OfficerAdapter officerAdapter;
    private List<UserData> officerList = new ArrayList<>();
    private List<UserData> officerListFull = new ArrayList<>();

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
        setContentView(R.layout.activity_officer_list);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupSortAndFilter();
        loadOfficersFromFirebase();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        toolbar = findViewById(R.id.toolbar);
        rvOfficers = findViewById(R.id.rvOfficers);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        tvTotalOfficers = findViewById(R.id.tvTotalOfficers);
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
            getSupportActionBar().setTitle("Officers List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        officerAdapter = new OfficerAdapter(officerList, this);
        rvOfficers.setLayoutManager(new LinearLayoutManager(this));
        rvOfficers.setAdapter(officerAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOfficers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortAndFilter() {
        swipeRefresh.setOnRefreshListener(OfficerListActivity.this::loadOfficersFromFirebase);
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
        LinearLayout sortByDistrict = findViewById(R.id.sortByDistrict);
        LinearLayout sortByDivision = findViewById(R.id.sortByDivision);
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

        sortByDistrict.setOnClickListener(v -> {
            currentSort = "district_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByDivision.setOnClickListener(v -> {
            currentSort = "division_asc";
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

        for (UserData officer : officerListFull) {
            boolean statusMatch = false;

            switch (currentStatusFilter) {
                case "all":
                    statusMatch = true;
                    break;
                case "active":
                    statusMatch = true; // You can add active/inactive logic
                    break;
                case "inactive":
                    statusMatch = false; // You can add active/inactive logic
                    break;
            }

            if (statusMatch) {
                filteredList.add(officer);
            }
        }

        officerList.clear();
        officerList.addAll(filteredList);
        applySort();
        updateStatistics();
        updateNoDataVisibility();
    }

    private void filterOfficers(String query) {
        officerList.clear();
        if (query.isEmpty()) {
            officerList.addAll(officerListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserData officer : officerListFull) {
                if (officer.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        officer.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        officer.getPhone().contains(lowerCaseQuery) ||
                        (officer.getDistrict() != null && officer.getDistrict().toLowerCase().contains(lowerCaseQuery)) ||
                        (officer.getDivision() != null && officer.getDivision().toLowerCase().contains(lowerCaseQuery)) ||
                        (officer.getCity() != null && officer.getCity().toLowerCase().contains(lowerCaseQuery)) ||
                        (officer.getState() != null && officer.getState().toLowerCase().contains(lowerCaseQuery))) {
                    officerList.add(officer);
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
                officerList.sort((o1, o2) -> o1.getFullName().compareTo(o2.getFullName()));
                break;
            case "name_desc":
                officerList.sort((o1, o2) -> o2.getFullName().compareTo(o1.getFullName()));
                break;
            case "district_asc":
                officerList.sort((o1, o2) -> {
                    String d1 = o1.getDistrict() != null ? o1.getDistrict() : "";
                    String d2 = o2.getDistrict() != null ? o2.getDistrict() : "";
                    return d1.compareTo(d2);
                });
                break;
            case "division_asc":
                officerList.sort((o1, o2) -> {
                    String d1 = o1.getDivision() != null ? o1.getDivision() : "";
                    String d2 = o2.getDivision() != null ? o2.getDivision() : "";
                    return d1.compareTo(d2);
                });
                break;
            case "city_asc":
                officerList.sort((o1, o2) -> {
                    String c1 = o1.getCity() != null ? o1.getCity() : "";
                    String c2 = o2.getCity() != null ? o2.getCity() : "";
                    return c1.compareTo(c2);
                });
                break;
        }
        officerAdapter.notifyDataSetChanged();
    }

    private void loadOfficersFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        officerList.clear();
        officerListFull.clear();
        reference = database.getReference().child("UserData").child("OFFICER");
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
                        rvOfficers.setVisibility(View.VISIBLE);
                        officer.setId(snapshot.getKey());
                        officerList.add(officer);
                        officerListFull.add(officer);
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
                Toast.makeText(OfficerListActivity.this, "Error loading officers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (officerListFull.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int total = officerListFull.size();
        tvTotalOfficers.setText("Total Officers: " + total);
        tvTotalCount.setText(String.valueOf(total));
        tvActiveCount.setText(String.valueOf(total));
        tvInactiveCount.setText("0");
    }

    private void updateNoDataVisibility() {
        if (officerList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvOfficers.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvOfficers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOfficerClick(UserData officer) {
        showOfficerDetails(officer);
    }

    @Override
    public void onContactClick(UserData officer) {
        Toast.makeText(this, "Contact: " + officer.getPhone(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClick(UserData officer) {
        showOfficerDetails(officer);
    }

    private void showOfficerDetails(UserData officer) {
        String message = "👤 Officer Details\n\n" +
                "Name: " + officer.getFullName() + "\n" +
                "Email: " + officer.getEmail() + "\n" +
                "Phone: " + officer.getPhone() + "\n" +
                "District: " + (officer.getDistrict() != null ? officer.getDistrict() : "N/A") + "\n" +
                "Division: " + (officer.getDivision() != null ? officer.getDivision() : "N/A") + "\n" +
                "City: " + (officer.getCity() != null ? officer.getCity() : "N/A") + "\n" +
                "State: " + (officer.getState() != null ? officer.getState() : "N/A") + "\n" +
                "Address: " + (officer.getAddress() != null ? officer.getAddress() : "N/A");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Officer Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}