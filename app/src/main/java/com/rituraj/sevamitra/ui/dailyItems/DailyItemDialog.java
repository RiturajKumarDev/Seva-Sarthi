package com.rituraj.sevamitra.ui.dailyItems;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.Status;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyItemDialog extends Dialog {
    private String userId;
    private DatabaseReference reference;
    private Context context;
    private DailyItemModel dailyItemModel;
    private Calendar calendar;
    private TextInputEditText itemName, etQuantity, itemPrice, etSupplier, itemDescription, etDate, etTime, etStatus;
    private Spinner spinnerCategory, spinnerUnit;
    private Button btnSaveItem, btnUpdateItem, btnCloseItem;

    public DailyItemDialog(@NonNull Context context, String userId, DailyItemModel dailyItemModel) {
        super(context);
        this.userId = userId;
        this.context = context;
        if (dailyItemModel == null) dailyItemModel = new DailyItemModel();
        else updateRestrict();
        this.dailyItemModel = dailyItemModel;
        calendar = Calendar.getInstance();
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
        itemPrice = findViewById(R.id.etPrice);
        etSupplier = findViewById(R.id.etSupplier);
        etStatus = findViewById(R.id.etStatus);
        itemDescription = findViewById(R.id.etNotes);

        etDate = findViewById(R.id.imgCalender);
        etTime = findViewById(R.id.etTime);

        btnSaveItem = findViewById(R.id.btnSaveItem);
        btnUpdateItem = findViewById(R.id.btnUpdateItem);
        btnCloseItem = findViewById(R.id.btnCloseItem);

        setupSpinners();
        setupDateAndTime();

        itemName.setText(dailyItemModel.getItemName());
        etQuantity.setText(dailyItemModel.getQuantity());
        itemPrice.setText(dailyItemModel.getPrice());
        etSupplier.setText(dailyItemModel.getSupplier());
        etDate.setText(dailyItemModel.getDate());
        etTime.setText(dailyItemModel.getTime());
        etStatus.setText(dailyItemModel.getStatus());
        itemDescription.setText(dailyItemModel.getNotes());
        setSpinnerSelection(spinnerUnit, dailyItemModel.getUnit() + "");
        setSpinnerSelection(spinnerCategory, dailyItemModel.getCategory() + "");


        btnCloseItem.setOnClickListener(v -> dismiss());
        btnUpdateItem.setOnClickListener(v -> updateValidateForm());
        btnSaveItem.setOnClickListener(v -> uploadItem());
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

    private void setupSpinners() {
        String[] categories = {"Select Category", "Milk", "Water", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dailyItemModel.setCategory(position == 0 ? "" : categories[position]);
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

    private void updateRestrict() {
//        itemName.set(false);
//        spinnerCategory.setEnabled(false);
//        spinnerUnit.setEnabled(false);
//        etSupplier.setEnabled(false);
//        etStatus.setEnabled(false);
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

        if (etQuantity.getText().toString().trim().isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return false;
        }

        if (itemPrice.getText().toString().trim().isEmpty()) {
            itemPrice.setError("Price is required");
            itemPrice.requestFocus();
            return false;
        }

        if (dailyItemModel.getStatus() != null && dailyItemModel.getStatus().isEmpty()) {
            Toast.makeText(context, "Please select status", Toast.LENGTH_SHORT).show();
            return false;
        }
        double totle = Integer.parseInt(itemPrice.getText().toString()) * Integer.parseInt(etQuantity.getText().toString());
        dailyItemModel.setCategory(spinnerCategory.getSelectedItem().toString());
        dailyItemModel.setUnit(spinnerUnit.getSelectedItem().toString());
        dailyItemModel.setItemName(itemName.getText().toString());
        dailyItemModel.setQuantity(etQuantity.getText().toString());
        dailyItemModel.setPrice(itemPrice.getText().toString());
        dailyItemModel.setTotalAmount(String.valueOf(totle));
        dailyItemModel.setDate(etDate.getText().toString());
        dailyItemModel.setTime(etTime.getText().toString());
        dailyItemModel.setCreatedBy(userId);
        dailyItemModel.setStatus(Status.PENDING);
        dailyItemModel.setNotes(itemDescription.getText().toString());
        return true;
    }

    private Boolean updateValidateForm() {
        if (etSupplier.getText().toString().isEmpty())
            return false;

        return true;
    }

    private void uploadItem() {
        if (!validateForm()) return;
        btnSaveItem.setEnabled(false);
        long timestamp = System.currentTimeMillis();
        dailyItemModel.setTimestamp(timestamp);
        reference.child("DailyWorks").child(String.valueOf(timestamp)).setValue(dailyItemModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dismiss();
            }
        });
    }
}
