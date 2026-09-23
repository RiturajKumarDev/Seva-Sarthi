package com.rituraj.sevamitra.ui.worker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemDetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyOrderListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private Chip chipAll, chipPending, chipAccepted, chipCompleted;
    private TextView tvTotalOrdersCount, tvActiveOrdersCount, tvCompletedOrdersCount;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOrders;
    private TextView tvNoData;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String vendorUserId;

    private List<DailyItemModel> masterOrderList = new ArrayList<>();
    private List<DailyItemModel> filteredOrderList = new ArrayList<>();
    private Map<String, UserData> customerUserMap = new HashMap<>();
    private VendorOrderAdapter adapter;

    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_order_list);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }
        vendorUserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFiltersAndSearch();

        loadCustomerDataAndOrders();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipAccepted = findViewById(R.id.chipAccepted);
        chipCompleted = findViewById(R.id.chipCompleted);

        tvTotalOrdersCount = findViewById(R.id.tvTotalOrdersCount);
        tvActiveOrdersCount = findViewById(R.id.tvActiveOrdersCount);
        tvCompletedOrdersCount = findViewById(R.id.tvCompletedOrdersCount);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvOrders = findViewById(R.id.rvOrders);
        tvNoData = findViewById(R.id.tvNoData);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Vendor Daily Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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

    private void setupRecyclerView() {
        adapter = new VendorOrderAdapter(filteredOrderList, customerUserMap, new VendorOrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(DailyItemModel item) {
                Intent intent = new Intent(DailyOrderListActivity.this, DailyItemDetailsActivity.class);
                intent.putExtra("ITEM_ID", item.getId());
                startActivity(intent);
            }

            @Override
            public void onAcceptClick(DailyItemModel item) {
                acceptOrderInFirebase(item);
            }
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupFiltersAndSearch() {
        swipeRefresh.setOnRefreshListener(this::loadOrdersFromFirebase);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipAll.setOnClickListener(v -> {
            updateChipSelection(chipAll);
            currentStatusFilter = "ALL";
            applyFilters();
        });

        chipPending.setOnClickListener(v -> {
            updateChipSelection(chipPending);
            currentStatusFilter = Status.PENDING;
            applyFilters();
        });

        chipAccepted.setOnClickListener(v -> {
            updateChipSelection(chipAccepted);
            currentStatusFilter = Status.ACCEPTED;
            applyFilters();
        });

        chipCompleted.setOnClickListener(v -> {
            updateChipSelection(chipCompleted);
            currentStatusFilter = "COMPLETED";
            applyFilters();
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipPending.setChecked(false);
        chipAccepted.setChecked(false);
        chipCompleted.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void loadCustomerDataAndOrders() {
        String[] categories = {"SEVASARTHI", "WORKER", "VENDOR", "FOUNDER", "OFFICER", "OTHER"};
        DatabaseReference userNodeRef = database.getReference().child("UserData");

        for (String cat : categories) {
            userNodeRef.child(cat).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            UserData userData = child.getValue(UserData.class);
                            if (userData != null) {
                                userData.setId(child.getKey());
                                customerUserMap.put(child.getKey(), userData);
                            }
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        loadOrdersFromFirebase();
    }

    private void loadOrdersFromFirebase() {
        reference = database.getReference().child("DailyWorks");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefresh.setRefreshing(false);
                masterOrderList.clear();

                int total = 0;
                int active = 0;
                int completed = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DailyItemModel item = child.getValue(DailyItemModel.class);
                        if (item != null) {
                            item.setId(child.getKey());

                            boolean isMyVendorOrder = (item.getSupplierId() != null && item.getSupplierId().equalsIgnoreCase(vendorUserId)) ||
                                    (item.getCreatedBy() != null && item.getCreatedBy().equalsIgnoreCase(vendorUserId));

                            if (isMyVendorOrder) {
                                masterOrderList.add(item);
                                total++;
                                if (Status.PENDING.equalsIgnoreCase(item.getStatus()) || Status.ACCEPTED.equalsIgnoreCase(item.getStatus())) {
                                    active++;
                                } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                                    completed++;
                                }
                            }
                        }
                    }
                }

                tvTotalOrdersCount.setText(String.valueOf(total));
                tvActiveOrdersCount.setText(String.valueOf(active));
                tvCompletedOrdersCount.setText(String.valueOf(completed));

                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void applyFilters() {
        filteredOrderList.clear();
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";

        for (DailyItemModel item : masterOrderList) {
            boolean statusMatch = false;
            if ("ALL".equalsIgnoreCase(currentStatusFilter)) {
                statusMatch = true;
            } else if (item.getStatus() != null && item.getStatus().equalsIgnoreCase(currentStatusFilter)) {
                statusMatch = true;
            }

            boolean searchMatch = true;
            if (!query.isEmpty()) {
                UserData customer = customerUserMap.get(item.getCreatedBy());
                String custName = customer != null && customer.getFullName() != null ? customer.getFullName().toLowerCase() : "";
                String itemName = item.getItemName() != null ? item.getItemName().toLowerCase() : "";
                String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                String date = item.getDate() != null ? item.getDate().toLowerCase() : "";

                searchMatch = custName.contains(query) || itemName.contains(query) || category.contains(query) || date.contains(query);
            }

            if (statusMatch && searchMatch) {
                filteredOrderList.add(item);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredOrderList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    private void acceptOrderInFirebase(DailyItemModel item) {
        if (item == null || item.getId() == null) return;
        DatabaseReference itemRef = database.getReference().child("DailyWorks").child(item.getId());
        itemRef.child("status").setValue(Status.ACCEPTED)
                .addOnSuccessListener(aVoid -> Toast.makeText(DailyOrderListActivity.this, "Order Accepted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DailyOrderListActivity.this, "Failed to accept order", Toast.LENGTH_SHORT).show());
    }

    // RecyclerView Adapter
    private static class VendorOrderAdapter extends RecyclerView.Adapter<VendorOrderAdapter.ViewHolder> {

        private List<DailyItemModel> orderList;
        private Map<String, UserData> userMap;
        private OnOrderClickListener listener;

        public interface OnOrderClickListener {
            void onOrderClick(DailyItemModel item);
            void onAcceptClick(DailyItemModel item);
        }

        public VendorOrderAdapter(List<DailyItemModel> orderList, Map<String, UserData> userMap, OnOrderClickListener listener) {
            this.orderList = orderList;
            this.userMap = userMap;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DailyItemModel item = orderList.get(position);
            holder.bind(item, userMap.get(item.getCreatedBy()), listener);
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardOrder;
            ImageView ivStatusDot;
            TextView tvItemName, tvCategory;
            TextView tvCustomerName, tvCustomerContact, tvCustomerAddress;
            TextView tvQuantityInfo, tvOrderDate, tvOrderStatus;
            MaterialButton btnAcceptOrder;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardOrder = itemView.findViewById(R.id.cardOrder);
                ivStatusDot = itemView.findViewById(R.id.ivStatusDot);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvCategory = itemView.findViewById(R.id.tvCategory);

                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvCustomerContact = itemView.findViewById(R.id.tvCustomerContact);
                tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);

                tvQuantityInfo = itemView.findViewById(R.id.tvQuantityInfo);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
                btnAcceptOrder = itemView.findViewById(R.id.btnAcceptOrder);
            }

            public void bind(DailyItemModel item, UserData customer, OnOrderClickListener listener) {
                tvItemName.setText(item.getItemName() != null ? item.getItemName() : "Daily Order");
                tvCategory.setText(item.getCategory() != null ? item.getCategory() : "Order");

                // Customer Info
                if (customer != null) {
                    tvCustomerName.setText("Ordered By: " + (customer.getFullName() != null ? customer.getFullName() : "Customer"));
                    tvCustomerContact.setText("Phone: " + (customer.getPhone() != null ? customer.getPhone() : "N/A") + " | Email: " + (customer.getEmail() != null ? customer.getEmail() : "N/A"));
                    String addr = (customer.getAddress() != null ? customer.getAddress() : "") + (customer.getCity() != null ? ", " + customer.getCity() : "");
                    tvCustomerAddress.setText("Address: " + (!addr.trim().isEmpty() ? addr : "N/A"));
                } else {
                    tvCustomerName.setText("Ordered By Customer ID: " + (item.getCreatedBy() != null ? item.getCreatedBy() : "N/A"));
                    tvCustomerContact.setText("Contact info loading...");
                    tvCustomerAddress.setText("Address loading...");
                }

                // Quantity & Duration
                String qInfo = item.getQuantity() + " " + (item.getUnit() != null ? item.getUnit() : "");
                if (item.getNumberOfDays() != null && !item.getNumberOfDays().trim().isEmpty()) {
                    qInfo += " (" + item.getNumberOfDays() + " Days, " + (item.getItemsPerDay() != null ? item.getItemsPerDay() : item.getQuantity()) + "/day)";
                }
                tvQuantityInfo.setText(qInfo);

                tvOrderDate.setText((item.getDate() != null ? item.getDate() : "") + " " + (item.getTime() != null ? item.getTime() : ""));
                tvOrderStatus.setText("Status: " + (item.getStatus() != null ? item.getStatus() : "Pending"));

                // Status formatting
                if (Status.ACCEPTED.equalsIgnoreCase(item.getStatus())) {
                    tvOrderStatus.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                    ivStatusDot.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
                    btnAcceptOrder.setVisibility(View.GONE);
                } else if (Status.PENDING.equalsIgnoreCase(item.getStatus())) {
                    tvOrderStatus.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                    ivStatusDot.setColorFilter(itemView.getContext().getColor(R.color.logo_orange));
                    btnAcceptOrder.setVisibility(View.VISIBLE);
                    btnAcceptOrder.setOnClickListener(v -> listener.onAcceptClick(item));
                } else {
                    tvOrderStatus.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                    ivStatusDot.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
                    btnAcceptOrder.setVisibility(View.GONE);
                }

                cardOrder.setOnClickListener(v -> listener.onOrderClick(item));
            }
        }
    }
}