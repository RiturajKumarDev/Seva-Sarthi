package com.rituraj.sevamitra.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.User;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.dashboard.BaseDashboardActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private DatabaseReference reference;

    // User Type Selection
    private RadioGroup userTypeRadioGroup;
    private CardView officerCard, workerCard, founderCard, sdmCard;

    // Common Fields
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputEditText etAddress, etAadharNumber;
    private Spinner spinnerState, spinnerCity;

    // Officer Specific Fields
    private TextInputEditText etDepartment, etDesignation, etEmployeeId;

    // Worker Specific Fields
    private Spinner spinnerWorkerCategory, spinnerFounder;
    private TextInputEditText etExperience, etSpecialization, etHourlyRate;
    private LinearLayout workerCategoriesLayout;
    private CheckBox cbCCTV, cbPlumber, cbAC, cbElectrician, cbCarpenter, cbPainter, cbMechanic;

    // Founder Specific Fields
    private TextInputEditText etCompanyName, etGstNumber, etOfficeAddress;

    // SDM Specific Fields
    private TextInputEditText etDistrict, etDivision, etGovtId;

    // Submit Button
    private MaterialButton btnRegister;

    // Selected User Type
    private String selectedUserType = "";

    // Worker Categories List
    private ArrayList<String> selectedCategories = new ArrayList<>();
    private ArrayList<UserData> founderList = new ArrayList<>();

    // State-City Mapping
    private Map<String, String[]> stateCityMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        setupStateCityMap();
        setupUserTypeSelection();
        setupSpinners();
        setupSubmitButton();
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        progressDialog = new ProgressDialog(RegistrationActivity.this);
        progressDialog.setCancelable(false);

        // User Type Selection
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        officerCard = findViewById(R.id.officerCard);
        workerCard = findViewById(R.id.workerCard);
        founderCard = findViewById(R.id.founderCard);
        sdmCard = findViewById(R.id.sdmCard);

        // Common Fields
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        etAadharNumber = findViewById(R.id.etAadharNumber);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerCity = findViewById(R.id.spinnerCity);

        // Officer Fields
        etDepartment = findViewById(R.id.etDepartment);
        etDesignation = findViewById(R.id.etDesignation);
        etEmployeeId = findViewById(R.id.etEmployeeId);

        // Worker Fields
        spinnerWorkerCategory = findViewById(R.id.spinnerWorkerCategory);
        spinnerFounder = findViewById(R.id.spinnerFounder);
        etExperience = findViewById(R.id.etExperience);
        etSpecialization = findViewById(R.id.etSpecialization);
        etHourlyRate = findViewById(R.id.etHourlyRate);
        workerCategoriesLayout = findViewById(R.id.workerCategoriesLayout);

        // Multiple Categories Selection
        cbCCTV = findViewById(R.id.cbCCTV);
        cbPlumber = findViewById(R.id.cbPlumber);
        cbAC = findViewById(R.id.cbAC);
        cbElectrician = findViewById(R.id.cbElectrician);
        cbCarpenter = findViewById(R.id.cbCarpenter);
        cbPainter = findViewById(R.id.cbPainter);
        cbMechanic = findViewById(R.id.cbMechanic);

        // Founder Fields
        etCompanyName = findViewById(R.id.etCompanyName);
        etGstNumber = findViewById(R.id.etGstNumber);
        etOfficeAddress = findViewById(R.id.etOfficeAddress);

        // SDM Fields
        etDistrict = findViewById(R.id.etDistrict);
        etDivision = findViewById(R.id.etDivision);
        etGovtId = findViewById(R.id.etGovtId);

        // Button
        btnRegister = findViewById(R.id.btnRegister);

        etFullName.setText(firebaseUser.getDisplayName());
        etEmail.setText(firebaseUser.getEmail());
//        getUser();
    }

    private void getUser() {
        progressDialog.setTitle("Loading ...");
        progressDialog.setMessage("Loading ...");
        progressDialog.show();
        if (firebaseUser == null)
            return;
        reference = database.getReference().child("Users").child(firebaseUser.getUid());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snapshot.getKey());
//                        getUserData(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void getUserData(User user) {
        if (user.getUserType() == null || user.getUserId() == null)
            return;
        reference = database.getReference().child("UserData").child(user.getUserType()).child(user.getUserId());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(RegistrationActivity.this, BaseDashboardActivity.class);
                        intent.putExtra("UserType", userData.getUserType());
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void setupStateCityMap() {
        stateCityMap.put("Uttar Pradesh", new String[]{"Lucknow", "Kanpur", "Varanasi", "Agra", "Prayagraj"});
        stateCityMap.put("Maharashtra", new String[]{"Mumbai", "Pune", "Nagpur", "Nashik"});
        stateCityMap.put("Delhi", new String[]{"New Delhi", "South Delhi", "East Delhi", "West Delhi"});
        stateCityMap.put("Bihar", new String[]{"Ara", "Araria", "Bagaha", "Banka", "Begusarai", "Bettiah", "Bhagalpur", "Bihar Sharif", "Buxar", "Chhapra", "Darbhanga", "Dehri", "Forbesganj", "Gaya", "Gopalganj", "Hajipur", "Jamalpur", "Jamui", "Jehanabad", "Katihar", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura", "Madhubani", "Motihari", "Munger", "Muzaffarpur", "Nawada", "Patna", "Purnia", "Raxaul", "Saharsa", "Samastipur", "Sasaram", "Sheikhpura", "Sitamarhi", "Siwan", "Supaul"});
        stateCityMap.put("Gujarat", new String[]{"Ahmedabad", "Surat", "Vadodara", "Rajkot"});
    }

    private void setupSpinners() {
        // State Spinner
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(stateCityMap.keySet()));
        spinnerState.setAdapter(stateAdapter);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = parent.getItemAtPosition(position).toString();
                String[] cities = stateCityMap.get(selectedState);
                if (cities != null) {
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(RegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, cities);
                    spinnerCity.setAdapter(cityAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Worker Category Spinner
        String[] categories = {"Select Primary Category", "Plumber", "Electrician", "AC Technician", "CCTV Technician", "Carpenter", "Painter", "Mechanic", "Other"};
        ArrayAdapter<String> workerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerWorkerCategory.setAdapter(workerAdapter);
        setSpinnerFounder();
    }

    private void setSpinnerFounder() {
        reference = database.getReference().child("UserData").child("FOUNDER");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            founderList.add(userData);
                        }
                    }
                    ArrayAdapter<UserData> founderAdapter = new ArrayAdapter<>(RegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, founderList);
                    spinnerFounder.setAdapter(founderAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupUserTypeSelection() {
        userTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Hide all cards first
            officerCard.setVisibility(View.GONE);
            workerCard.setVisibility(View.GONE);
            founderCard.setVisibility(View.GONE);
            sdmCard.setVisibility(View.GONE);

            if (checkedId == R.id.radioOfficer) {
                selectedUserType = "OFFICER";
                officerCard.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioWorker) {
                selectedUserType = "WORKER";
                workerCard.setVisibility(View.VISIBLE);
                workerCategoriesLayout.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioFounder) {
                selectedUserType = "FOUNDER";
                founderCard.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioSDM) {
                selectedUserType = "SDM";
                sdmCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSubmitButton() {
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        // Common Validation
        String fullName = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String department = etDepartment.getText().toString().trim();

        if (selectedUserType.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Please select a user type", Toast.LENGTH_LONG).show();
            return;
        }

        if (phone.isEmpty() || phone.length() != 10) {
            etPhone.setError("Valid 10-digit phone number is required");
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            return;
        }

        String aadhar = etAadharNumber.getText().toString().trim();
        if (aadhar.isEmpty() || aadhar.length() != 12) {
            etAadharNumber.setError("Valid 12-digit Aadhar number is required");
            return;
        }

//        if (password.isEmpty() || password.length() < 6) {
//            etPassword.setError("Password must be at least 6 characters");
//            return;
//        }
//
//        if (!password.equals(confirmPassword)) {
//            etConfirmPassword.setError("Passwords do not match");
//            return;
//        }

        // Get selected categories for worker
        if (selectedUserType.equals("WORKER")) {
            selectedCategories.clear();
            if (cbCCTV.isChecked()) selectedCategories.add("CCTV Technician");
            if (cbPlumber.isChecked()) selectedCategories.add("Plumber");
            if (cbAC.isChecked()) selectedCategories.add("AC Technician");
            if (cbElectrician.isChecked()) selectedCategories.add("Electrician");
            if (cbCarpenter.isChecked()) selectedCategories.add("Carpenter");
            if (cbPainter.isChecked()) selectedCategories.add("Painter");
            if (cbMechanic.isChecked()) selectedCategories.add("Mechanic");
        }

        // Create User Data Object
        UserData userData = new UserData();
        userData.setId(firebaseUser.getUid());
        userData.setProfileUrl(firebaseUser.getPhotoUrl().toString());
        userData.setUserType(selectedUserType);
        userData.setFullName(fullName);
        userData.setEmail(email);
        userData.setPhone(phone);
        userData.setAddress(etAddress.getText().toString().trim());
        userData.setAadharNumber(etAadharNumber.getText().toString().trim());
        userData.setState(spinnerState.getSelectedItem().toString());
        userData.setCity(spinnerCity.getSelectedItem().toString());

        // Type specific data
        switch (selectedUserType) {
            case "OFFICER":
                if (department.isEmpty()) {
                    etDepartment.setError("Department is required");
                    return;
                }
                String designation = etDesignation.getText().toString().trim();
                if (designation.isEmpty()) {
                    etDesignation.setError("Designation is required");
                    return;
                }
                String employeeId = etEmployeeId.getText().toString().trim();
                if (employeeId.isEmpty()) {
                    etEmployeeId.setError("Employee ID is required");
                    return;
                }
                userData.setDepartment(department);
                userData.setDesignation(designation);
                userData.setEmployeeId(employeeId);
                break;

            case "WORKER":
                userData.setState("Pending");
                userData.setPrimaryCategory(spinnerWorkerCategory.getSelectedItem().toString());
                userData.setCategories(new ArrayList<>(selectedCategories));
                String experience = etExperience.getText().toString().trim();
                if (experience.isEmpty()) {
                    etExperience.setError("Experience is required!");
                    return;
                }
                String specialization = etSpecialization.getText().toString().trim();
                if (specialization.isEmpty()) {
                    etSpecialization.setError("Specialization is required!");
                    return;
                }
                String hourlyRate = etHourlyRate.getText().toString().trim();
                if (hourlyRate.isEmpty()) {
                    etHourlyRate.setError("Hourly rate is required!");
                    return;
                }

                userData.setExperience(etExperience.getText().toString().trim());
                userData.setSpecialization(etSpecialization.getText().toString().trim());
                userData.setHourlyRate(etHourlyRate.getText().toString().trim());
                userData.setFounderId(founderList.get(spinnerFounder.getSelectedItemPosition()).getId());
                break;

            case "FOUNDER":
                String companyName = etCompanyName.getText().toString().trim();
                if (companyName.isEmpty()) {
                    etCompanyName.setError("CompanyName is required!");
                    return;
                }
                String gstNumber = etGstNumber.getText().toString().trim();
                if (gstNumber.isEmpty()) {
                    etGstNumber.setError("GST Number is required!");
                    return;
                }
                String officeAddress = etOfficeAddress.getText().toString().trim();
                if (officeAddress.isEmpty()) {
                    etOfficeAddress.setError("Office Address is required!");
                    return;
                }
                userData.setCompanyName(etCompanyName.getText().toString().trim());
                userData.setGstNumber(etGstNumber.getText().toString().trim());
                userData.setOfficeAddress(etOfficeAddress.getText().toString().trim());
                break;

            case "SDM":
                String district = etDistrict.getText().toString().trim();
                if (district.isEmpty()) {
                    etDistrict.setError("District Name is required!");
                    return;
                }
                String division = etDivision.getText().toString().trim();
                if (division.isEmpty()) {
                    etDivision.setError("Division is required!");
                    return;
                }
                String govtId = etGovtId.getText().toString().trim();
                if (govtId.isEmpty()) {
                    etGovtId.setError("Government ID is required!");
                    return;
                }
                userData.setDistrict(district);
                userData.setDivision(division);
                userData.setGovtId(govtId);
                break;
        }
        saveUserToDatabase(userData);
    }

    private void saveUserToDatabase(UserData userData) {
        progressDialog.show();
        progressDialog.setMessage("Uploading ...");
        progressDialog.setTitle("Uploading ...");
        database.getReference().child("Users").child(userData.getId()).child("userType").setValue(userData.getUserType());
        reference = database.getReference().child("UserData").child(userData.getUserType());
        reference.keepSynced(true);
        reference.child(userData.getId()).setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful!\nUser Type: " + userData.getUserType(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(RegistrationActivity.this, BaseDashboardActivity.class);
                        intent.putExtra("UserType", userData.getUserType());
                        startActivity(intent);
                        finish();
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(RegistrationActivity.this, "Registration Cancel!!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}