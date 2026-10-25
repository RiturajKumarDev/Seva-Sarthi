package com.rituraj.sevamitra.ui.dailyItems;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.rituraj.sevamitra.ui.support.SupportActivity;
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
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class DailyItemsActivity extends AppCompatActivity {

    private ImageView ivBack, ivSupport;

    // Stats Views
    private TextView tvTotalItems, tvTotalMilk, tvTotalWater;
    private LinearLayout cardStats;
    private EditText etCalender;
    private Calendar calendar;

    // Search
    private EditText etSearch;
    private ImageView ivClearSearch;

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

        setupStatusBar();

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
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // Stats
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalMilk = findViewById(R.id.tvTotalMilk);
        tvTotalWater = findViewById(R.id.tvTotalWater);
        cardStats = findViewById(R.id.cardStats);

        etCalender = findViewById(R.id.etCalender);

        // Search
        etSearch = findViewById(R.id.etSearch);
        ivClearSearch = findViewById(R.id.ivClearSearch);

        // Filter Chips
        chipAll = findViewById(R.id.chipAll);
        chipMilk = findViewById(R.id.chipMilk);
        chipWater = findViewById(R.id.chipWater);
        chipGroup = findViewById(R.id.chipGroup);

        // RecyclerView
        rvItems = findViewById(R.id.rvItems);
        tvNoData = findViewById(R.id.tvNoData);
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
            ivSupport.setOnClickListener(v -> startActivity(new Intent(DailyItemsActivity.this, SupportActivity.class)));
        }
    }

    private void setupRecyclerView() {
        itemAdapter = new DailyItemAdapter(itemList, new DailyItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DailyItemModel item) {
                if (item != null && item.getId() != null) {
                    Intent intent = new Intent(DailyItemsActivity.this, DailyItemDetailsActivity.class);
                    intent.putExtra("ITEM_ID", item.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(DailyItemModel item) {
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(DailyItemsActivity.this::loadItemsFromFirebase);

        if (ivClearSearch != null) {
            ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ivClearSearch != null) {
                    if (s != null && s.length() > 0) {
                        ivClearSearch.setVisibility(View.VISIBLE);
                    } else {
                        ivClearSearch.setVisibility(View.GONE);
                    }
                }
                applyFilters();
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

        View.OnClickListener monthPickerListener = (v) -> {
            MonthYearPickerDialogFragment dialog = MonthYearPickerDialogFragment
                    .getInstance(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));

            dialog.setOnDateSetListener((year, monthOfYear) -> {
                currentMonthFilter = monthOfYear + 1;
                currentYearFilter = year;
                String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
                etCalender.setText(monthName + " " + year);
                applyFilters();
            });
            dialog.show(getSupportFragmentManager(), "MonthYearPicker");
        };

        etCalender.setOnClickListener(monthPickerListener);
        CardView cardMonthPicker = findViewById(R.id.cardMonthPicker);
        if (cardMonthPicker != null) {
            cardMonthPicker.setOnClickListener(monthPickerListener);
        }
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipMilk.setChecked(false);
        chipWater.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void applyFilters() {
        List<DailyItemModel> filteredList = new ArrayList<>();
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";

        for (DailyItemModel item : itemList) {
            boolean categoryMatch = false;
            boolean dateMatch = true;
            boolean searchMatch = true;

            // Category filter
            if ("all".equalsIgnoreCase(currentCategoryFilter)) {
                categoryMatch = true;
            } else if (item.getCategory() != null) {
                categoryMatch = item.getCategory().toLowerCase().contains(currentCategoryFilter.toLowerCase());
            }

            // Month/Year Date filter
            String itemDateStr = item.getDate() != null ? item.getDate().trim() : "";
            String[] date = itemDateStr.split("/");
            if (date.length >= 3 && currentYearFilter != 0) {
                try {
                    int itemMonth = Integer.parseInt(date[1]);
                    int itemYear = Integer.parseInt(date[2]);
                    dateMatch = (itemMonth == currentMonthFilter) && (itemYear == currentYearFilter);
                } catch (NumberFormatException e) {
                    dateMatch = false;
                }
            }

            // Search query match
            if (!query.isEmpty()) {
                String itemName = item.getItemName() != null ? item.getItemName().toLowerCase() : "";
                String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                String supplier = item.getSupplierDetail() != null ? item.getSupplierDetail().toLowerCase() : "";
                String dateStr = item.getDate() != null ? item.getDate().toLowerCase() : "";
                String problemType = item.getProblemType() != null ? item.getProblemType().toLowerCase() : "";

                searchMatch = itemName.contains(query) || category.contains(query) ||
                        supplier.contains(query) || dateStr.contains(query) ||
                        problemType.contains(query);
            }

            if (categoryMatch && dateMatch && searchMatch) {
                filteredList.add(item);
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