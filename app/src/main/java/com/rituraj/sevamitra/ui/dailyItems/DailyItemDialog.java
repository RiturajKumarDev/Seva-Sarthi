package com.rituraj.sevamitra.ui.dailyItems;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DailyItemDialog extends Dialog {
    private DatabaseReference reference;
    private Context context;
    private Activity activity;
    private DailyItemModel dailyItemModel;
    private Calendar calendar;
    private TextInputEditText itemName, etQuantity, itemDescription, etDate, etTime, etStatus;
    private Spinner spinnerCategory, spinnerSupplier, spinnerUnit;
    private Button btnUpdateItem, btnCloseItem;
    private String[] issueList;
    private String userType;

    public DailyItemDialog(@NonNull Context context, String userType, String[] issueList, DailyItemModel dailyItemModel) {
        super(context);
        this.context = context;
        this.activity = (Activity) context;
        this.userType = userType;
        this.dailyItemModel = dailyItemModel;
        calendar = Calendar.getInstance();
        this.issueList = issueList;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        setContentView(R.layout.dialog_daily_item_detail);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        initViews();
    }

    private void initViews() {
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
        btnCloseItem = findViewById(R.id.btnCloseItem);

        setupSpinners();
        setupDateAndTime();

        itemName.setText(dailyItemModel.getItemName());
        etQuantity.setText(dailyItemModel.getQuantity());
        etDate.setText(dailyItemModel.getDate());
        etTime.setText(dailyItemModel.getTime());
        etStatus.setText(dailyItemModel.getStatus());
        itemDescription.setText(dailyItemModel.getNotes());
        setSpinnerSelection(spinnerUnit, String.valueOf(dailyItemModel.getUnit()));
        setSpinnerSelection(spinnerCategory, String.valueOf(dailyItemModel.getCategory()));


        btnCloseItem.setOnClickListener(v -> dismiss());
        btnUpdateItem.setOnClickListener(v -> uploadItem());
    }

    private void setupSpinners() {
        // Issue Type Spinner
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, issueList);
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
                dailyItemModel.setUnit(parent.getSelectedItem().toString());
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
                    ArrayAdapter<UserData> workerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, workerList);
                    spinnerSupplier.setAdapter(workerAdapter);
                    setSpinnerSelection(spinnerSupplier, String.valueOf(dailyItemModel.getSupplierDetail()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        value = value.trim();
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateDisplay();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
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
            Toast.makeText(context, "Please select category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dailyItemModel.getUnit() != null && dailyItemModel.getUnit().isEmpty()) {
            Toast.makeText(context, "Please select unit", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerSupplier.getSelectedItem() == null || spinnerSupplier.getSelectedItem().toString().isEmpty()) {
            Toast.makeText(context, "Please select supplier", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etQuantity.getText().toString().trim().isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return false;
        }

        if (dailyItemModel.getStatus() != null && dailyItemModel.getStatus().isEmpty()) {
            Toast.makeText(context, "Please select status", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "Please select Issue", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerUnit.getSelectedItem().toString().isEmpty() || spinnerUnit.getSelectedItem().toString().startsWith("Select")) {
            Toast.makeText(context, "Please select unit", Toast.LENGTH_SHORT).show();
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
        dailyItemModel.setCreatedBy(dailyItemModel.getCreatedBy());
        dailyItemModel.setStatus(Status.PENDING);
        dailyItemModel.setNotes(itemDescription.getText().toString());
        return true;
    }

    private void uploadItem() {
        if (!validateForm()) return;
        btnUpdateItem.setEnabled(false);
        reference = FirebaseDatabase.getInstance().getReference();
        if (userType.equalsIgnoreCase("WORKER")) {
            reference.child("DailyWorks").child(String.valueOf(dailyItemModel.getId())).child("status").setValue(Status.ACCEPTED).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                    activity.finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        long timestamp = System.currentTimeMillis();
        dailyItemModel.setTimestamp(timestamp);
        reference.child("DailyWorks").child(String.valueOf(timestamp)).setValue(dailyItemModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                dismiss();
                activity.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
