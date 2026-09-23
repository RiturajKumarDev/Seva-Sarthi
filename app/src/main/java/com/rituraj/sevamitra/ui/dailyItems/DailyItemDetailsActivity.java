package com.rituraj.sevamitra.ui.dailyItems;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
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
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DailyItemDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTodayQuestion, tvTodayDate, tvTodayStatus;
    private MaterialButton btnReceiveToday;
    private TextView tvProgressText, tvTotalQuantityInfo;
    private ProgressBar pbDaysProgress;
    private TextView tvItemName, tvDepartmentCategory, tvSupplierDetail, tvOrderDateTime, tvItemStatus, tvNotes;
    private LinearLayout layoutDaysContainer;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String itemId;
    private DailyItemModel currentItem;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_item_details);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (!getIntent().hasExtra("ITEM_ID")) {
            Toast.makeText(this, "Item ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        itemId = getIntent().getStringExtra("ITEM_ID");
        database = FirebaseDatabase.getInstance();

        initViews();
        setupToolbar();
        loadItemDetails();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTodayQuestion = findViewById(R.id.tvTodayQuestion);
        tvTodayDate = findViewById(R.id.tvTodayDate);
        tvTodayStatus = findViewById(R.id.tvTodayStatus);
        btnReceiveToday = findViewById(R.id.btnReceiveToday);

        tvProgressText = findViewById(R.id.tvProgressText);
        tvTotalQuantityInfo = findViewById(R.id.tvTotalQuantityInfo);
        pbDaysProgress = findViewById(R.id.pbDaysProgress);

        tvItemName = findViewById(R.id.tvItemName);
        tvDepartmentCategory = findViewById(R.id.tvDepartmentCategory);
        tvSupplierDetail = findViewById(R.id.tvSupplierDetail);
        tvOrderDateTime = findViewById(R.id.tvOrderDateTime);
        tvItemStatus = findViewById(R.id.tvItemStatus);
        tvNotes = findViewById(R.id.tvNotes);

        layoutDaysContainer = findViewById(R.id.layoutDaysContainer);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Item Details");
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

    private void loadItemDetails() {
        reference = database.getReference().child("DailyWorks").child(itemId);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentItem = snapshot.getValue(DailyItemModel.class);
                    if (currentItem != null) {
                        currentItem.setId(snapshot.getKey());
                        updateUI(currentItem);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyItemDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(DailyItemModel item) {
        tvItemName.setText(item.getItemName());
        tvDepartmentCategory.setText((item.getProblemType() != null ? item.getProblemType() : "") + " • " + (item.getCategory() != null ? item.getCategory() : ""));
        tvSupplierDetail.setText("Supplier: " + (item.getSupplierDetail() != null ? item.getSupplierDetail() : "N/A"));
        tvOrderDateTime.setText("Order Date: " + (item.getDate() != null ? item.getDate() : "") + " " + (item.getTime() != null ? item.getTime() : ""));
        tvItemStatus.setText("Status: " + (item.getStatus() != null ? item.getStatus() : "Pending"));
        tvNotes.setText("Notes: " + (item.getNotes() != null && !item.getNotes().isEmpty() ? item.getNotes() : "None"));

        int totalDays = 1;
        if (item.getNumberOfDays() != null && !item.getNumberOfDays().trim().isEmpty()) {
            try {
                totalDays = Integer.parseInt(item.getNumberOfDays().trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String itemsPerDay = item.getItemsPerDay() != null && !item.getItemsPerDay().trim().isEmpty() ? item.getItemsPerDay() : item.getQuantity();
        tvTotalQuantityInfo.setText("Quantity: " + item.getQuantity() + " " + (item.getUnit() != null ? item.getUnit() : "") + " (" + totalDays + " Days, " + itemsPerDay + "/day)");

        SimpleDateFormat dateKeySdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar todayCal = Calendar.getInstance();
        String todayKey = dateKeySdf.format(todayCal.getTime());
        String todayDisplay = displaySdf.format(todayCal.getTime());

        tvTodayDate.setText("Today (" + todayDisplay + ")");

        Map<String, Long> receivedMap = item.getDailyReceivedTimestamps();
        if (receivedMap == null) {
            receivedMap = new HashMap<>();
        }

        // Today Status
        if (receivedMap.containsKey(todayKey)) {
            Long timestamp = receivedMap.get(todayKey);
            String timeStr = timestamp != null ? formatTime(timestamp) : "";
            tvTodayStatus.setText("Status: Received Today at " + timeStr + " ✓");
            tvTodayStatus.setTextColor(getColor(R.color.logo_green));
            btnReceiveToday.setEnabled(false);
            btnReceiveToday.setText("Already Received Today ✓");
            btnReceiveToday.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.logo_green)));
        } else {
            tvTodayStatus.setText("Status: Pending for Today ⏳");
            tvTodayStatus.setTextColor(getColor(R.color.logo_orange));
            btnReceiveToday.setEnabled(true);
            btnReceiveToday.setText("Yes, Received Today");
            btnReceiveToday.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.logo_gold)));
            btnReceiveToday.setOnClickListener(v -> markDateAsReceived(todayKey));
        }

        // Build Daily Schedule
        layoutDaysContainer.removeAllViews();
        Calendar startCal = Calendar.getInstance();
        if (item.getDate() != null && !item.getDate().isEmpty()) {
            try {
                Date parsed = displaySdf.parse(item.getDate().trim());
                if (parsed != null) {
                    startCal.setTime(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        int receivedCount = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < totalDays; i++) {
            Calendar dayCal = (Calendar) startCal.clone();
            dayCal.add(Calendar.DAY_OF_MONTH, i);

            String dayKey = dateKeySdf.format(dayCal.getTime());
            String dayDisplay = displaySdf.format(dayCal.getTime());

            View dayView = inflater.inflate(R.layout.item_daily_received_day, layoutDaysContainer, false);
            ImageView ivDayStatus = dayView.findViewById(R.id.ivDayStatus);
            TextView tvDayTitle = dayView.findViewById(R.id.tvDayTitle);
            TextView tvDayStatusText = dayView.findViewById(R.id.tvDayStatusText);
            MaterialButton btnMarkReceived = dayView.findViewById(R.id.btnMarkReceived);

            tvDayTitle.setText("Day " + (i + 1) + " - " + dayDisplay);

            if (receivedMap.containsKey(dayKey)) {
                receivedCount++;
                ivDayStatus.setImageResource(R.drawable.circle_shape_green);
                Long receivedTime = receivedMap.get(dayKey);
                tvDayStatusText.setText("Received " + (receivedTime != null ? "(" + formatTime(receivedTime) + ")" : ""));
                tvDayStatusText.setTextColor(getColor(R.color.logo_green));
                btnMarkReceived.setVisibility(View.GONE);
            } else {
                if (dayKey.equals(todayKey)) {
                    tvDayStatusText.setText("Pending Today");
                    tvDayStatusText.setTextColor(getColor(R.color.logo_orange));
                    btnMarkReceived.setVisibility(View.VISIBLE);
                    btnMarkReceived.setOnClickListener(v -> markDateAsReceived(dayKey));
                } else if (dayCal.before(todayCal)) {
                    tvDayStatusText.setText("Not Received");
                    tvDayStatusText.setTextColor(getColor(R.color.logo_red));
                    btnMarkReceived.setVisibility(View.VISIBLE);
                    btnMarkReceived.setOnClickListener(v -> markDateAsReceived(dayKey));
                } else {
                    tvDayStatusText.setText("Upcoming");
                    tvDayStatusText.setTextColor(getColor(R.color.logo_gold_light));
                    btnMarkReceived.setVisibility(View.GONE);
                }
            }

            layoutDaysContainer.addView(dayView);
        }

        tvProgressText.setText(receivedCount + " / " + totalDays + " Days Received");
        int progressPercent = totalDays > 0 ? (receivedCount * 100 / totalDays) : 0;
        pbDaysProgress.setProgress(progressPercent);
    }

    private void markDateAsReceived(String dateKey) {
        if (itemId == null) return;
        if (firebaseUser.getPhotoUrl() == null) return;
        if (String.valueOf(firebaseUser.getPhotoUrl()).equalsIgnoreCase("VENDOR")) return;
        DatabaseReference itemRef = database.getReference().child("DailyWorks").child(itemId);
        itemRef.child("dailyReceivedTimestamps").child(dateKey).setValue(System.currentTimeMillis()).addOnSuccessListener(aVoid -> Toast.makeText(DailyItemDetailsActivity.this, "Marked as Received!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(DailyItemDetailsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}