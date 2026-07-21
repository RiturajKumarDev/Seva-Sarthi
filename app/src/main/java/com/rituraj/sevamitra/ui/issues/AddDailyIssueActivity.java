package com.rituraj.sevamitra.ui.issues;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddDailyIssueActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String userId;
    private String selectedProblemType;
    private Calendar calendar;

    private TextInputEditText itemName, etQuantity, itemDescription, etDate, etTime, etStatus;
    private Spinner spinnerCategory, spinnerSupplier, spinnerUnit;
    private Button btnUpdateItem;
    private String[] issueList;
    private String userType;
    private DailyItemModel dailyItemModel = new DailyItemModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_issue);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        userId = firebaseUser.getUid();

        dailyItemModel.setCreatedBy(userId);

        if (!getIntent().hasExtra("REQUEST_DEPARTMENT")) {
            finish();
            return;
        }
        selectedProblemType = getIntent().getStringExtra("REQUEST_DEPARTMENT");
        dailyItemModel.setProblemType(selectedProblemType);

        // Initialize Firebase
        calendar = Calendar.getInstance();
        initViews();
        setupToolbar();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        itemName = findViewById(R.id.etItemName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerSupplier = findViewById(R.id.spinnerSupplier);
        itemDescription = findViewById(R.id.etNotes);
        etStatus = findViewById(R.id.etStatus);
        etStatus.setText(Status.PENDING);

        etDate = findViewById(R.id.imgCalender);
        etTime = findViewById(R.id.etTime);

        btnUpdateItem = findViewById(R.id.btnUpdateItem);
        findViewById(R.id.btnCloseItem).setVisibility(View.GONE);

        setSelectedProblemType();
        setupSpinners();
        setupDateAndTime();

        btnUpdateItem.setOnClickListener(v -> uploadItem());
        translationViews();
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


    private void setSelectedProblemType() {
        DailyItemModel dailyItemModel = new DailyItemModel();
        dailyItemModel.setCreatedBy(userId);
        dailyItemModel.setProblemType(selectedProblemType);
        int arrayResId;
        switch (selectedProblemType) {
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
        issueList = getResources().getStringArray(arrayResId);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Register New Daily Issue");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Issue Type Spinner
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issueList);
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(issueAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dailyItemModel.setCategory(position == 0 ? "" : issueList[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dailyItemModel.setUnit(position == 0 ? "" : parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getWorkerData(dailyItemModel.getProblemType());
    }

    private void getWorkerData(String workerDepartment) {
        reference = FirebaseDatabase.getInstance().getReference().child("UserData").child("WORKER");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<UserData> workerList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            if (userData.getDepartment() != null && userData.getDepartment().equalsIgnoreCase(workerDepartment))
                                workerList.add(userData);
                        }
                    }
                    ArrayAdapter<UserData> workerAdapter = new ArrayAdapter<>(AddDailyIssueActivity.this, android.R.layout.simple_spinner_dropdown_item, workerList);
                    spinnerSupplier.setAdapter(workerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupDateAndTime() {
        // Date Picker
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> showDatePicker());

        // Time Picker
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> showTimePicker());

        // Set default date and time
        updateDateDisplay();
        updateTimeDisplay();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateDisplay();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeDisplay();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        etTime.setText(sdf.format(calendar.getTime()));
    }

    private boolean validateForm() {
        if (itemName.getText().toString().trim().isEmpty()) {
            itemName.setError("Item name is required");
            itemName.requestFocus();
            return false;
        }

        if (dailyItemModel.getCategory() != null && dailyItemModel.getCategory().isEmpty()) {
            Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dailyItemModel.getUnit() != null && dailyItemModel.getUnit().isEmpty()) {
            Toast.makeText(this, "Please select unit", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerSupplier.getSelectedItem() == null || spinnerSupplier.getSelectedItem().toString().isEmpty()) {
            Toast.makeText(this, "Please select supplier", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etQuantity.getText().toString().trim().isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return false;
        }

        if (dailyItemModel.getStatus() != null && dailyItemModel.getStatus().isEmpty()) {
            Toast.makeText(this, "Please select status", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDate.getText().toString().trim().isEmpty()) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return false;
        }
        if (etTime.getText().toString().trim().isEmpty()) {
            etTime.setError("Time is required");
            etTime.requestFocus();
            return false;
        }
        if (itemDescription.getText().toString().trim().isEmpty()) {
            itemDescription.setError("Description is required");
            itemDescription.requestFocus();
            return false;
        }
        if (spinnerCategory.getSelectedItem().toString().isEmpty() || spinnerCategory.getSelectedItem().toString().startsWith("Select")) {
            Toast.makeText(this, "Please select Issue", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerUnit.getSelectedItem().toString().isEmpty() || spinnerUnit.getSelectedItem().toString().startsWith("Select")) {
            Toast.makeText(this, "Please select unit", Toast.LENGTH_SHORT).show();
            return false;
        }

        dailyItemModel.setSupplierId(((UserData) spinnerSupplier.getSelectedItem()).getId());
        dailyItemModel.setSupplierDetail(spinnerSupplier.getSelectedItem().toString());
        dailyItemModel.setCategory(spinnerCategory.getSelectedItem().toString());
        dailyItemModel.setUnit(spinnerUnit.getSelectedItem().toString());
        dailyItemModel.setItemName(itemName.getText().toString());
        dailyItemModel.setQuantity(etQuantity.getText().toString());
        dailyItemModel.setDate(etDate.getText().toString());
        dailyItemModel.setTime(etTime.getText().toString());
        dailyItemModel.setStatus(Status.PENDING);
        dailyItemModel.setNotes(itemDescription.getText().toString());
        return true;
    }

    private void uploadItem() {
        if (!validateForm()) return;
        btnUpdateItem.setEnabled(false);
        reference = FirebaseDatabase.getInstance().getReference();
        long timestamp = System.currentTimeMillis();
        dailyItemModel.setTimestamp(timestamp);
        reference.child("DailyWorks").child(String.valueOf(timestamp)).setValue(dailyItemModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddDailyIssueActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddDailyIssueActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}