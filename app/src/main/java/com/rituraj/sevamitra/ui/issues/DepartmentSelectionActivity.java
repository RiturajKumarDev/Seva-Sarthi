package com.rituraj.sevamitra.ui.issues;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.util.HashMap;
import java.util.Map;

public class DepartmentSelectionActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private GridLayout gridDepartments;
    private String selectedDepartment = "";

    // Department icons mapping
    private Map<String, Integer> departmentIcons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_selection);

        initViews();
        translationViews();
        setupToolbar();
        setupDepartmentIcons();
        setupDepartmentGrid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        gridDepartments = findViewById(R.id.gridDepartments);
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
            getSupportActionBar().setTitle("Select Department");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDepartmentIcons() {
        departmentIcons.put("Carpenter", R.drawable.ic_carpenter);
        departmentIcons.put("Computer", R.drawable.ic_computer);
        departmentIcons.put("Computer & IT Services", R.drawable.ic_computer);
        departmentIcons.put("Electrician", R.drawable.ic_electrician);
        departmentIcons.put("Mechanic", R.drawable.ic_mechanic);
        departmentIcons.put("Painter", R.drawable.ic_painter);
        departmentIcons.put("Plumber", R.drawable.ic_plumber);

        departmentIcons.put("Beauty & Personal Care", R.drawable.ic_beauty);
        departmentIcons.put("Dairy Services", R.drawable.ic_dairy);
        departmentIcons.put("Decoration", R.drawable.ic_decoration);
        departmentIcons.put("Home Services", R.drawable.ic_home_services);
        departmentIcons.put("Laundry Services", R.drawable.ic_laundry);
        departmentIcons.put("Sanitation", R.drawable.ic_sanitation);
        departmentIcons.put("Water Supply", R.drawable.ic_water);
        departmentIcons.put("Other", R.drawable.ic_other);
    }

    private void setupDepartmentGrid() {
        String[] departments = {
                "Carpenter", "Computer", "Computer & IT Services",
                "Electrician", "Mechanic", "Painter", "Plumber",
                "Beauty & Personal Care", "Dairy Services",
                "Decoration", "Home Services", "Laundry Services",
                "Sanitation", "Water Supply", "Other"
        };

        // Clear existing views
        gridDepartments.removeAllViews();

        // Set column count based on screen width
        gridDepartments.setColumnCount(3);

        for (String department : departments) {
            View itemView = getLayoutInflater().inflate(R.layout.item_department_button, null);

            MaterialCardView cardView = itemView.findViewById(R.id.cardDepartment);
            ImageView ivIcon = itemView.findViewById(R.id.ivDepartmentIcon);
            TextView tvName = itemView.findViewById(R.id.tvDepartmentName);

            // Set icon
            Integer iconRes = departmentIcons.get(department);
            if (iconRes != null) {
                ivIcon.setImageResource(iconRes);
            } else {
                ivIcon.setImageResource(R.drawable.ic_other);
            }

            tvName.setText(department);

            // Set click listener
            cardView.setOnClickListener(v -> {
                selectDepartment(department, cardView);
            });

            // Add to grid
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            itemView.setLayoutParams(params);

            gridDepartments.addView(itemView);
        }
    }

    private void selectDepartment(String department, MaterialCardView selectedCard) {
        // Deselect all cards
        for (int i = 0; i < gridDepartments.getChildCount(); i++) {
            View child = gridDepartments.getChildAt(i);
            MaterialCardView card = child.findViewById(R.id.cardDepartment);
            card.setCardBackgroundColor(getColor(R.color.card_bg_default));
            card.setStrokeColor(getColor(R.color.logo_gold_light));
            card.setStrokeWidth(1);
        }

        // Select the clicked card
        selectedCard.setCardBackgroundColor(getColor(R.color.card_bg_selected));
        selectedCard.setStrokeColor(getColor(R.color.logo_gold));
        selectedCard.setStrokeWidth(3);

        int index = gridDepartments.indexOfChild(selectedCard);

        Toast.makeText(this, "Department: " + index, Toast.LENGTH_SHORT).show();

        selectedDepartment = department;
        Intent intent;
        if (index == 14)
            intent = new Intent(DepartmentSelectionActivity.this, AddIssueActivity.class);
        else if (index > 6)
            intent = new Intent(DepartmentSelectionActivity.this, AddDailyIssueActivity.class);
        else
            intent = new Intent(DepartmentSelectionActivity.this, AddIssueActivity.class);
        intent.putExtra("REQUEST_DEPARTMENT", selectedDepartment);
        startActivity(intent);
    }
}