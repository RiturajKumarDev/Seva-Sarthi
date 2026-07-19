package com.rituraj.sevamitra.ui.dailyItems;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.DailyItemAdapter;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class DailyItemsActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar toolbar;

    // Stats Views
    private TextView tvTotalItems, tvTotalMilk, tvTotalWater;
    private LinearLayout cardStats;
    private TextInputEditText etCalender;
    private Calendar calendar;

    // Search
    private EditText etSearch;
    private ImageView ivFilter;

    // Filter Chips
    private Chip chipAll, chipMilk, chipWater;
    private ChipGroup chipGroup;

    // RecyclerView
    private RecyclerView rvItems;
    private TextView tvNoData;

    // Data
    private List<DailyItemModel> itemList = new ArrayList<>();
    private DailyItemAdapter itemAdapter;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String userId;
    private String userType;
    private UserData currentUser;

    // Filter
    private String currentCategoryFilter = "all";
    private int currentMonthFilter, currentYearFilter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_items);

        calendar = Calendar.getInstance();
        // Get user data
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }
        userId = firebaseUser.getUid();
        userType = firebaseUser.getPhotoUrl().toString();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadItemsFromFirebase();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // Stats
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalMilk = findViewById(R.id.tvTotalMilk);
        tvTotalWater = findViewById(R.id.tvTotalWater);
        cardStats = findViewById(R.id.cardStats);

        etCalender = findViewById(R.id.etCalender);

        // Search
        etSearch = findViewById(R.id.etSearch);
        ivFilter = findViewById(R.id.ivFilter);

        // Filter Chips
        chipAll = findViewById(R.id.chipAll);
        chipMilk = findViewById(R.id.chipMilk);
        chipWater = findViewById(R.id.chipWater);
        chipGroup = findViewById(R.id.chipGroup);

        // RecyclerView
        rvItems = findViewById(R.id.rvItems);
        tvNoData = findViewById(R.id.tvNoData);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Items");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        itemAdapter = new DailyItemAdapter(itemList, new DailyItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DailyItemModel item) {
                setSelectedProblemType(item);
            }

            @Override
            public void onDeleteClick(DailyItemModel item) {
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
    }

    private void setSelectedProblemType(DailyItemModel dailyItemModel) {
        dailyItemModel.setCreatedBy(userId);
        int arrayResId;
        switch (dailyItemModel.getProblemType()) {
            case "Beauty & Personal Care":
                arrayResId = R.array.beauty_personal_care_issues;
                break;
            case "Dairy Services":
                arrayResId = R.array.dairy_services_issues;
                break;
            case "Decoration":
                arrayResId = R.array.decoration_issues;
                break;
            case "Home Services":
                arrayResId = R.array.home_services_issues;
                break;
            case "Sanitation":
                arrayResId = R.array.sanitation_issues;
                break;
            case "Water Supply":
                arrayResId = R.array.water_supply_issues;
                break;
            case "Laundry Services":
                arrayResId = R.array.laundry_services_issues;
                break;
            default:
                arrayResId = R.array.other_issues;
                break;
        }
        String[] issueList = getResources().getStringArray(arrayResId);
        DailyItemDialog dailyItemDialog = new DailyItemDialog(DailyItemsActivity.this, userType, issueList, dailyItemModel);
        dailyItemDialog.show();
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(DailyItemsActivity.this::loadItemsFromFirebase);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter Chips
        chipAll.setOnClickListener(v -> {
            currentCategoryFilter = "all";
            updateChipSelection(chipAll);
            applyFilters();
        });

        chipMilk.setOnClickListener(v -> {
            currentCategoryFilter = "Milk";
            updateChipSelection(chipMilk);
            applyFilters();
        });

        chipWater.setOnClickListener(v -> {
            currentCategoryFilter = "Water";
            updateChipSelection(chipWater);
            applyFilters();
        });

        etCalender.setOnClickListener((v) -> {
            MonthYearPickerDialogFragment dialog = MonthYearPickerDialogFragment
                    .getInstance(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));

            dialog.setOnDateSetListener((year, monthOfYear) -> {
                currentMonthFilter = monthOfYear + 1;
                currentYearFilter = year;
                String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
                etCalender.setText(String.valueOf(monthName + " " + year));
                applyFilters();
            });
            dialog.show(getSupportFragmentManager(), "MonthYearPicker");
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipMilk.setChecked(false);
        chipWater.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void applyFilters() {
        List<DailyItemModel> filteredList = new ArrayList<>();

        for (DailyItemModel item : itemList) {
            boolean categoryMatch = false;
            boolean dateMatch = true;

            // Category filter
            if ("all".equals(currentCategoryFilter)) {
                categoryMatch = true;
            } else {
                categoryMatch = item.getCategory().contains(currentCategoryFilter);
            }

            String[] date = item.getDate().trim().split("/");
            if (date.length >= 3 && currentYearFilter != 0) {
                try {
                    int itemMonth = Integer.parseInt(date[1]);
                    int itemYear = Integer.parseInt(date[2]);
                    dateMatch = (itemMonth == currentMonthFilter) && (itemYear == currentYearFilter);
                } catch (NumberFormatException e) {
                    dateMatch = false;
                }
            }

            if (categoryMatch && dateMatch) {
                filteredList.add(item);
            }
        }

        itemAdapter.updateList(filteredList);
        updateNoDataVisibility(filteredList.isEmpty());
    }

    private void filterItems(String query) {
        List<DailyItemModel> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (DailyItemModel item : itemList) {
                if (item.getItemName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getCategory().toLowerCase().contains(lowerCaseQuery) ||
                        item.getSupplierDetail().toLowerCase().contains(lowerCaseQuery) ||
                        item.getDate().contains(lowerCaseQuery)) {
                    filteredList.add(item);
                }
            }
        }

        itemAdapter.updateList(filteredList);
        updateNoDataVisibility(filteredList.isEmpty());
    }

    private void loadItemsFromFirebase() {
        itemList.clear();
        reference = database.getReference().child("DailyWorks");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    DailyItemModel dailyItemModel = snapshot.getValue(DailyItemModel.class);
                    if (dailyItemModel != null) {
                        dailyItemModel.setId(snapshot.getKey());
                        if (dailyItemModel.getCreatedBy().equalsIgnoreCase(userId)) {
                            itemList.add(dailyItemModel);
                            itemAdapter.updateList(itemList);
                            updateNoDataVisibility(itemList.isEmpty());
                            updateStatistics();
                        } else if (userType.equalsIgnoreCase("WORKER")) {
                            if (dailyItemModel.getSupplierId().equalsIgnoreCase(userId)) {
                                itemList.add(dailyItemModel);
                                itemAdapter.updateList(itemList);
                                updateNoDataVisibility(itemList.isEmpty());
                                updateStatistics();
                            }
                        } else if (userType.equalsIgnoreCase("FOUNDER")) {
                            itemList.add(dailyItemModel);
                            itemAdapter.updateList(itemList);
                            updateNoDataVisibility(itemList.isEmpty());
                            updateStatistics();
                        }
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
            }
        });
    }

    private void updateStatistics() {
        int total = itemList.size();
        int milkCount = 0, waterCount = 0;

        for (DailyItemModel item : itemList) {
            if (item.getCategory().contains("Milk")) milkCount++;
            if (item.getCategory().contains("Water")) waterCount++;
        }

        tvTotalItems.setText(String.valueOf(total));
        tvTotalMilk.setText(String.valueOf(milkCount));
        tvTotalWater.setText(String.valueOf(waterCount));
    }

    private void updateNoDataVisibility(boolean isEmpty) {
        if (isEmpty) {
            tvNoData.setVisibility(View.VISIBLE);
            rvItems.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvItems.setVisibility(View.VISIBLE);
        }
    }
}