package com.rituraj.sevamitra.ui.worker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.rituraj.sevamitra.locations.LocationManager;
import com.rituraj.sevamitra.locations.PermissionManager;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.WorkerAdapter;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkerListActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerClickListener {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Views
    private ImageView ivBack, ivSupport;
    private RecyclerView rvWorkers;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private EditText etSearch;
    private ImageView ivSort, ivFilter;
    private Chip chipAll, chipAvailable, chipBusy;
    private Chip chipPlumber, chipElectrician, chipAC, chipCCTV;
    private LinearLayout sortOptionsLayout;
    private CardView cardFilter;
    private Button btnClearFilters;

    // Location Filter Views
    private MaterialButton btnFilterMyLocation, btnApplyLocationFilter;
    private TextInputEditText etFilterState, etFilterCity, etFilterPincode;

    // Location Managers
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private final String[] foregroundLocationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // Adapter and Data
    private WorkerAdapter workerAdapter;
    private List<UserData> workerList = new ArrayList<>();
    private List<UserData> workerListFull = new ArrayList<>();

    // Filters
    private String currentStatusFilter = "all";
    private String currentCategoryFilter = "all";
    private String currentSort = "name_asc";
    private String issueId;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        setupStatusBar();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        issueId = getIntent().getStringExtra("IssueId");

        initViews();
        takeUserLocation();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupSortAndFilter();
        loadWorkersFromFirebase();
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
        btnFilterMyLocation = findViewById(R.id.btnFilterMyLocation);
        btnApplyLocationFilter = findViewById(R.id.btnApplyLocationFilter);
        etFilterState = findViewById(R.id.etFilterState);
        etFilterCity = findViewById(R.id.etFilterCity);
        etFilterPincode = findViewById(R.id.etFilterPincode);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvWorkers = findViewById(R.id.rvWorkers);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        etSearch = findViewById(R.id.etSearch);
        ivSort = findViewById(R.id.ivSort);
        ivFilter = findViewById(R.id.ivFilter);
        chipAll = findViewById(R.id.chipAll);
        chipAvailable = findViewById(R.id.chipAvailable);
        chipBusy = findViewById(R.id.chipBusy);
        chipPlumber = findViewById(R.id.chipPlumber);
        chipElectrician = findViewById(R.id.chipElectrician);
        chipAC = findViewById(R.id.chipAC);
        chipCCTV = findViewById(R.id.chipCCTV);
        sortOptionsLayout = findViewById(R.id.sortOptionsLayout);
        cardFilter = findViewById(R.id.cardFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
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
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        if (ivSupport != null) {
            ivSupport.setOnClickListener(v -> startActivity(new Intent(WorkerListActivity.this, SupportActivity.class)));
        }
    }

    private void setupRecyclerView() {
        workerAdapter = new WorkerAdapter(workerList, issueId, firebaseUser, this);
        rvWorkers.setLayoutManager(new LinearLayoutManager(this));
        rvWorkers.setAdapter(workerAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortAndFilter() {
        swipeRefresh.setOnRefreshListener(WorkerListActivity.this::loadWorkersFromFirebase);
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

        // Status Filter Chips
        chipAll.setOnClickListener(v -> {
            currentStatusFilter = "all";
            updateChipSelection(chipAll);
            applyFilters();
        });

        chipAvailable.setOnClickListener(v -> {
            currentStatusFilter = Status.ACTIVE;
            updateChipSelection(chipAvailable);
            applyFilters();
        });

        chipBusy.setOnClickListener(v -> {
            currentStatusFilter = "busy";
            updateChipSelection(chipBusy);
            applyFilters();
        });

        // Category Filter Chips
        chipPlumber.setOnClickListener(v -> {
            currentCategoryFilter = "Plumber";
            updateCategoryChipSelection(chipPlumber);
            applyFilters();
        });

        chipElectrician.setOnClickListener(v -> {
            currentCategoryFilter = "Electrician";
            updateCategoryChipSelection(chipElectrician);
            applyFilters();
        });

        chipAC.setOnClickListener(v -> {
            currentCategoryFilter = "AC Technician";
            updateCategoryChipSelection(chipAC);
            applyFilters();
        });

        chipCCTV.setOnClickListener(v -> {
            currentCategoryFilter = "CCTV Technician";
            updateCategoryChipSelection(chipCCTV);
            applyFilters();
        });

        // Location Filter Listeners
        if (btnFilterMyLocation != null) {
            btnFilterMyLocation.setOnClickListener(v -> fetchUserLocation());
        }

        if (btnApplyLocationFilter != null) {
            btnApplyLocationFilter.setOnClickListener(v -> {
                applyFilters();
                cardFilter.setVisibility(View.GONE);
            });
        }

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> {
            currentStatusFilter = "all";
            currentCategoryFilter = "all";
            updateChipSelection(chipAll);
            updateCategoryChipSelection(null);
            if (etFilterState != null) etFilterState.setText("");
            if (etFilterCity != null) etFilterCity.setText("");
            if (etFilterPincode != null) etFilterPincode.setText("");
            applyFilters();
            cardFilter.setVisibility(View.GONE);
        });

        // Sort options
        setupSortOptions();
    }

    private void setupSortOptions() {
        LinearLayout sortByNameAsc = findViewById(R.id.sortByNameAsc);
        LinearLayout sortByNameDesc = findViewById(R.id.sortByNameDesc);
        LinearLayout sortByCategory = findViewById(R.id.sortByCategory);
        LinearLayout sortByExperience = findViewById(R.id.sortByExperience);
        LinearLayout sortByRateAsc = findViewById(R.id.sortByRateAsc);
        LinearLayout sortByRateDesc = findViewById(R.id.sortByRateDesc);

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

        sortByCategory.setOnClickListener(v -> {
            currentSort = "category_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByExperience.setOnClickListener(v -> {
            currentSort = "experience_desc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByRateAsc.setOnClickListener(v -> {
            currentSort = "rate_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByRateDesc.setOnClickListener(v -> {
            currentSort = "rate_desc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipAvailable.setChecked(false);
        chipBusy.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void updateCategoryChipSelection(Chip selectedChip) {
        // Reset all category chips
        chipPlumber.setChecked(false);
        chipElectrician.setChecked(false);
        chipAC.setChecked(false);
        chipCCTV.setChecked(false);

        if (selectedChip != null) {
            selectedChip.setChecked(true);
        } else {
            currentCategoryFilter = "all";
        }
    }

    private void applyFilters() {
        List<UserData> filteredList = new ArrayList<>();

        String stateFilter = etFilterState != null && etFilterState.getText() != null ? etFilterState.getText().toString().trim().toLowerCase() : "";
        String cityFilter = etFilterCity != null && etFilterCity.getText() != null ? etFilterCity.getText().toString().trim().toLowerCase() : "";
        String pincodeFilter = etFilterPincode != null && etFilterPincode.getText() != null ? etFilterPincode.getText().toString().trim().toLowerCase() : "";

        for (UserData worker : workerListFull) {
            boolean statusMatch = false;
            boolean categoryMatch = false;
            boolean stateMatch = true;
            boolean cityMatch = true;
            boolean pincodeMatch = true;

            // Status filter
            switch (currentStatusFilter) {
                case "all":
                    statusMatch = true;
                    break;
                case Status.ACTIVE:
                    statusMatch = Status.ACTIVE.equals(worker.getIsSelected());
                    break;
            }

            // Category filter
            if ("all".equals(currentCategoryFilter)) {
                categoryMatch = true;
            } else {
                categoryMatch = currentCategoryFilter.equalsIgnoreCase(worker.getDepartment());
            }

            // Location Filters (State, City, Pincode)
            if (!stateFilter.isEmpty()) {
                String workerState = worker.getState() != null ? worker.getState().toLowerCase() : "";
                stateMatch = workerState.contains(stateFilter);
            }

            if (!cityFilter.isEmpty()) {
                String workerCity = worker.getCity() != null ? worker.getCity().toLowerCase() : "";
                cityMatch = workerCity.contains(cityFilter);
            }

            if (!pincodeFilter.isEmpty()) {
                String workerPincode = worker.getPinCode() != null ? worker.getPinCode().toLowerCase() : "";
                pincodeMatch = workerPincode.contains(pincodeFilter);
            }

            if (statusMatch && categoryMatch && stateMatch && cityMatch && pincodeMatch) {
                filteredList.add(worker);
            }
        }

        workerList.clear();
        workerList.addAll(filteredList);
        applySort();
        updateNoDataVisibility();
    }

    private void filterWorkers(String query) {
        workerList.clear();
        if (query.isEmpty()) {
            workerList.addAll(workerListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserData worker : workerListFull) {
                if (worker.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getDepartment().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getCity().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getState().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getPhone().contains(lowerCaseQuery) ||
                        (worker.getSkills() != null && worker.getSkills().toString().toLowerCase().contains(lowerCaseQuery))) {
                    workerList.add(worker);
                }
            }
        }
        applySort();
        updateNoDataVisibility();
    }

    private void applySort() {
        switch (currentSort) {
            case "name_asc":
                workerList.sort((w1, w2) -> w1.getFullName().compareTo(w2.getFullName()));
                break;
            case "name_desc":
                workerList.sort((w1, w2) -> w2.getFullName().compareTo(w1.getFullName()));
                break;
            case "category_asc":
                workerList.sort((w1, w2) -> {
                    String cat1 = w1.getDepartment() != null ? w1.getDepartment() : "";
                    String cat2 = w2.getDepartment() != null ? w2.getDepartment() : "";
                    return cat1.compareTo(cat2);
                });
                break;
        }
        workerAdapter.notifyDataSetChanged();
    }

    private void loadWorkersFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        workerList.clear();
        workerListFull.clear();
        reference = database.getReference().child("UserData").child("WORKER");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                swipeRefresh.setRefreshing(false);
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);
                    rvWorkers.setVisibility(View.VISIBLE);
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        userData.setId(snapshot.getKey());
                        if (userData.getIsSelected().equalsIgnoreCase(Status.ACTIVE)) {
                            workerList.add(userData);
                            workerListFull.add(userData);
                        } else {
                            if (firebaseUser != null && firebaseUser.getPhotoUrl() != null && firebaseUser.getPhotoUrl().toString().equals("FOUNDER")) {
                                workerList.add(userData);
                                workerListFull.add(userData);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                UserData updatedUser = snapshot.getValue(UserData.class);
                if (updatedUser != null) {
                    updatedUser.setId(snapshot.getKey());
                    for (int i = 0; i < workerList.size(); i++) {
                        if (workerList.get(i).getId().equals(updatedUser.getId())) {
                            workerList.set(i, updatedUser);
                            workerListFull.set(i, updatedUser);
                            workerAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateNoDataVisibility() {
        if (workerList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvWorkers.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvWorkers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWorkerClick(UserData worker) {
        Toast.makeText(this, "Selected: " + worker.getFullName(), Toast.LENGTH_SHORT).show();
        // Navigate to worker details
    }

    @Override
    public void onContactClick(UserData worker) {
        Toast.makeText(this, "Contact: " + worker.getPhone(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onManageClick(UserData worker) {
        showManageDialog(worker);
    }

    @Override
    public void onAssignClick(UserData worker) {
        if (worker.getState().equalsIgnoreCase(Status.ACTIVE)) {
            long timestamp = System.currentTimeMillis();
            reference = database.getReference().child("Issues").child(issueId);
            reference.child("assignedTo").setValue(worker.getId());
            reference.child("workAssignTimestamp").setValue(timestamp);
            reference.child("status").setValue(Status.PROCESS);
            Toast.makeText(this, "Work assigned to: " + worker.getFullName(), Toast.LENGTH_SHORT).show();
            finish();
        } else
            Toast.makeText(WorkerListActivity.this, "Worker not accepted by founder!!", Toast.LENGTH_SHORT).show();
    }

    private void showManageDialog(UserData worker) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_worker, null);

        AlertDialog dialog = new AlertDialog.Builder(WorkerListActivity.this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivClose = dialogView.findViewById(R.id.ivClose);
        ImageView ivProfile = dialogView.findViewById(R.id.ivProfile);
        ImageView ivStatus = dialogView.findViewById(R.id.ivStatus);
        TextView tvName = dialogView.findViewById(R.id.tvName);
        TextView tvCategory = dialogView.findViewById(R.id.tvCategory);
        TextView tvCurrentStatus = dialogView.findViewById(R.id.tvCurrentStatus);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        TextView tvCityState = dialogView.findViewById(R.id.tvCityState);
        TextView tvSkills = dialogView.findViewById(R.id.tvSkills);

        MaterialButton btnActive = dialogView.findViewById(R.id.btnActive);
        MaterialButton btnInactive = dialogView.findViewById(R.id.btnInactive);
        MaterialButton btnSuspend = dialogView.findViewById(R.id.btnSuspend);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Populate details
        tvName.setText(worker.getFullName());
        tvCategory.setText(worker.getDepartment() != null ? worker.getDepartment() : "General");
        tvEmail.setText(worker.getEmail() != null ? worker.getEmail() : "Email not provided");
        tvPhone.setText(worker.getPhone() != null ? worker.getPhone() : "Phone not provided");

        String cityStateStr = (worker.getCity() != null ? worker.getCity() : "") +
                ((worker.getCity() != null && worker.getState() != null) ? ", " : "") +
                (worker.getState() != null ? worker.getState() : "");
        tvCityState.setText(!cityStateStr.isEmpty() ? cityStateStr : "Location not provided");

        if (worker.getSkills() != null && !worker.getSkills().isEmpty()) {
            StringBuilder skillsStr = new StringBuilder();
            for (String skill : worker.getSkills()) {
                if (skillsStr.length() > 0) skillsStr.append(" • ");
                skillsStr.append(skill);
            }
            tvSkills.setText(skillsStr.toString());
        } else {
            tvSkills.setText("No additional skills listed");
        }

        String currentStatus = worker.getIsSelected() != null ? worker.getIsSelected() : Status.PENDING;
        tvCurrentStatus.setText("Status: " + currentStatus);

        if (Status.ACTIVE.equalsIgnoreCase(currentStatus)) {
            tvCurrentStatus.setTextColor(getColor(R.color.logo_green));
            ivStatus.setColorFilter(getColor(R.color.logo_green));
        } else if (Status.SUSPENDED.equalsIgnoreCase(currentStatus)) {
            tvCurrentStatus.setTextColor(getColor(R.color.logo_red));
            ivStatus.setColorFilter(getColor(R.color.logo_red));
        } else {
            tvCurrentStatus.setTextColor(getColor(R.color.logo_orange));
            ivStatus.setColorFilter(getColor(R.color.logo_orange));
        }

        if (worker.getProfileUrl() != null && !worker.getProfileUrl().isEmpty()) {
            Glide.with(this)
                    .load(worker.getProfileUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile);
        }

        DatabaseReference statusRef = database.getReference()
                .child("UserData").child("WORKER").child(worker.getId()).child("isSelected");

        ivClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnActive.setOnClickListener(v -> {
            statusRef.setValue(Status.ACTIVE);
            Toast.makeText(this, worker.getFullName() + " activated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnInactive.setOnClickListener(v -> {
            statusRef.setValue(Status.INACTIVE);
            Toast.makeText(this, worker.getFullName() + " marked as inactive", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnSuspend.setOnClickListener(v -> {
            statusRef.setValue(Status.SUSPENDED);
            Toast.makeText(this, worker.getFullName() + " suspended", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void takeUserLocation() {
        permissionManager = PermissionManager.getInstance(WorkerListActivity.this);
        locationManager = LocationManager.getInstance(WorkerListActivity.this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public void fetchUserLocation() {
        if (permissionManager == null) {
            permissionManager = PermissionManager.getInstance(WorkerListActivity.this);
        }
        if (locationManager == null) {
            locationManager = LocationManager.getInstance(WorkerListActivity.this);
        }

        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(WorkerListActivity.this, foregroundLocationPermissions, 100);
        } else {
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastLocation();
                if (location != null) {
                    getAddressFromLocation(WorkerListActivity.this, location.getLatitude(), location.getLongitude());
                } else {
                    locationManager.startLocationUpdates();
                    Toast.makeText(this, "Fetching GPS location...", Toast.LENGTH_SHORT).show();
                }
            } else {
                locationManager.createLocationRequest();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationManager != null) {
            locationManager.startLocationUpdates();
        }
        if (localBroadcastIntentFilter != null) {
            LocalBroadcastManager.getInstance(WorkerListActivity.this).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
        }
    }

    @Override
    public void onPause() {
        if (localBroadcastIntentFilter != null) {
            LocalBroadcastManager.getInstance(WorkerListActivity.this).unregisterReceiver(foregroundLocationBroadCastReceiver);
        }
        if (locationManager != null) {
            locationManager.stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null && permissionManager.handlePermissionResult(WorkerListActivity.this, 100, permissions, grantResults)) {
            if (locationManager != null) {
                locationManager.createLocationRequest();
                fetchUserLocation();
            }
        } else {
            Toast.makeText(WorkerListActivity.this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("Lat") && intent.hasExtra("Lng")) {
                try {
                    double lat = Double.parseDouble(intent.getStringExtra("Lat"));
                    double lng = Double.parseDouble(intent.getStringExtra("Lng"));
                    getAddressFromLocation(context, lat, lng);
                } catch (Exception ignored) {
                }
            }
        }
    };

    private void getAddressFromLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressObj = addresses.get(0);
                String city = addressObj.getLocality() != null ? addressObj.getLocality() : addressObj.getSubAdminArea();
                String state = addressObj.getAdminArea();
                String postalCode = addressObj.getPostalCode();

                if (state != null && etFilterState != null) {
                    etFilterState.setText(state);
                }

                if (city != null && etFilterCity != null) {
                    etFilterCity.setText(city);
                }

                if (postalCode != null && etFilterPincode != null) {
                    etFilterPincode.setText(postalCode);
                }
//                Toast.makeText(context, "GPS Location fetched! Click 'APPLY LOCATION FILTER' to filter workers.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Location fetched. Please verify filters.", Toast.LENGTH_SHORT).show();
        }
    }

}