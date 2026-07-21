package com.rituraj.sevamitra.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminUserManagementActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar toolbar;

    // Statistics
    private TextView tvTotalUsers, tvSevaMitraCount, tvWorkerCount, tvFounderCount, tvOfficerCount;
    private CardView cardTotal, cardSevaMitra, cardWorker, cardFounder, cardOfficer;

    // Search and Filter
    private EditText etSearch;
    private Spinner spinnerUserTypeFilter;

    // RecyclerView
    private RecyclerView rvUsers;
    private TextView tvNoData;
    private ProgressBar progressBar;

    // Edit User Form
    private CardView cardEditForm;
    private TextInputEditText etFullName, etEmail, etPhone, etAddress, etState, etCity;
    private TextInputEditText etDepartment, etDesignation;
    private TextInputEditText etCompanyName, etGstNumber, etOfficeAddress;
    private TextInputEditText etDistrict, etDivision;
    private Spinner spinnerUserType, spinnerStatus, spinnerOfficerDepartment;
    private LinearLayout skillsLayout;
    private CheckBox cbPlumber, cbElectrician, cbAC, cbCCTV, cbCarpenter, cbPainter, cbMechanic;
    private Spinner spinnerFounder;
    private MaterialButton btnUpdateUser, btnDeleteUser;
    private Button btnCloseForm;
    private LinearLayout formLayout;
    private TextView tvFormTitle, tvUserId, tvCreatedDate;
    private final List<CheckBox> skillCheckBoxes = new ArrayList<>();

    // Data
    private List<UserData> userList = new ArrayList<>();
    private List<UserData> userListFull = new ArrayList<>();
    private UserAdapter userAdapter;
    private UserData editingUser = null;
    private ArrayList<String> selectedSkills = new ArrayList<>();

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Filters
    private String selectedUserTypeFilter = "all";
    private String[] departmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSpinners();
        setupSearchAndFilter();
        loadUsersFromFirebase();
        setupClickListeners();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Statistics
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvSevaMitraCount = findViewById(R.id.tvSevaMitraCount);
        tvWorkerCount = findViewById(R.id.tvWorkerCount);
        tvFounderCount = findViewById(R.id.tvFounderCount);
        tvOfficerCount = findViewById(R.id.tvOfficerCount);
        cardTotal = findViewById(R.id.cardTotal);
        cardSevaMitra = findViewById(R.id.cardSevaMitra);
        cardWorker = findViewById(R.id.cardWorker);
        cardFounder = findViewById(R.id.cardFounder);
        cardOfficer = findViewById(R.id.cardOfficer);

        // Search and Filter
        etSearch = findViewById(R.id.etSearch);
        spinnerUserTypeFilter = findViewById(R.id.spinnerUserTypeFilter);

        // RecyclerView
        rvUsers = findViewById(R.id.rvUsers);
        tvNoData = findViewById(R.id.tvNoData);
        progressBar = findViewById(R.id.progressBar);

        // Edit Form
        cardEditForm = findViewById(R.id.cardEditForm);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etState = findViewById(R.id.etState);
        etCity = findViewById(R.id.etCity);
        etDepartment = findViewById(R.id.etDepartment);
        etDesignation = findViewById(R.id.etDesignation);
        etCompanyName = findViewById(R.id.etCompanyName);
        etGstNumber = findViewById(R.id.etGstNumber);
        etOfficeAddress = findViewById(R.id.etOfficeAddress);
        etDistrict = findViewById(R.id.etDistrict);
        etDivision = findViewById(R.id.etDivision);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerOfficerDepartment = findViewById(R.id.spinnerOfficerDepartment);
        spinnerFounder = findViewById(R.id.spinnerFounder);
        skillsLayout = findViewById(R.id.skillsLayout);
        cbPlumber = findViewById(R.id.cbPlumber);
        cbElectrician = findViewById(R.id.cbElectrician);
        cbAC = findViewById(R.id.cbAC);
        cbCCTV = findViewById(R.id.cbCCTV);
        cbCarpenter = findViewById(R.id.cbCarpenter);
        cbPainter = findViewById(R.id.cbPainter);
        cbMechanic = findViewById(R.id.cbMechanic);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        btnCloseForm = findViewById(R.id.btnCloseForm);
        formLayout = findViewById(R.id.formLayout);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvUserId = findViewById(R.id.tvUserId);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        departmentList = getResources().getStringArray(R.array.department_category);
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
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(userList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserData user) {
                editUser(user);
            }

            @Override
            public void onEditClick(UserData user) {
                editUser(user);
            }

            @Override
            public void onDeleteClick(UserData user) {
                showDeleteConfirmation(user);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void setupSpinners() {
        // User Type Filter Spinner
        String[] userTypes = {"All Users", "SevaSarthi", "Worker", "Founder", "Officer", "Other"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userTypes);
        spinnerUserTypeFilter.setAdapter(filterAdapter);
        spinnerUserTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedUserTypeFilter = "all";
                } else {
                    selectedUserTypeFilter = userTypes[position].toUpperCase();
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // User Type Spinner (Edit Form)
        String[] editUserTypes = {"Select User Type", "SEVASARTHI", "WORKER", "FOUNDER", "OFFICER", "OTHER"};
        ArrayAdapter<String> editAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, editUserTypes);
        spinnerUserType.setAdapter(editAdapter);
        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && editingUser != null) {
                    editingUser.setUserType(editUserTypes[position]);
                    updateFormFields(editUserTypes[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerFounder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSelectedProblemType(departmentList[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Status Spinner
        String[] statuses = {"Select Status", Status.ACTIVE, Status.INACTIVE, Status.SUSPENDED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupSearchAndFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyFilters() {
        List<UserData> filteredList = new ArrayList<>();

        for (UserData user : userListFull) {
            boolean typeMatch = false;

            if ("all".equals(selectedUserTypeFilter)) {
                typeMatch = true;
            } else {
                typeMatch = selectedUserTypeFilter.equalsIgnoreCase(user.getUserType());
            }

            if (typeMatch) {
                filteredList.add(user);
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        userAdapter.notifyDataSetChanged();
        updateStatistics();
        updateNoDataVisibility();
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserData user : userListFull) {
                if (user.getFullName().toLowerCase().contains(lowerCaseQuery) || user.getEmail().toLowerCase().contains(lowerCaseQuery) || user.getPhone().contains(lowerCaseQuery) || (user.getCity() != null && user.getCity().toLowerCase().contains(lowerCaseQuery)) || (user.getState() != null && user.getState().toLowerCase().contains(lowerCaseQuery)) || (user.getUserType() != null && user.getUserType().toLowerCase().contains(lowerCaseQuery))) {
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
        updateNoDataVisibility();
    }

    private void getSevaSarthiData() {
        DatabaseReference reference = database.getReference().child("UserData").child("SEVASARTHI");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            userList.add(userData);
                            userListFull.add(userData);
                        }
                    }
                    updateStatistics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFounderData() {
        DatabaseReference reference = database.getReference().child("UserData").child("FOUNDER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            userList.add(userData);
                            userListFull.add(userData);
                        }
                    }
                    updateStatistics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getWorkerData() {
        DatabaseReference reference = database.getReference().child("UserData").child("WORKER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            userList.add(userData);
                            userListFull.add(userData);
                        }
                    }
                    updateStatistics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getOfficerData() {
        DatabaseReference reference = database.getReference().child("UserData").child("OFFICER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            userList.add(userData);
                            userListFull.add(userData);
                        }
                    }
                    updateStatistics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getOtherUserData() {
        DatabaseReference reference = database.getReference().child("UserData").child("OTHER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            userData.setId(dataSnapshot.getKey());
                            userList.add(userData);
                            userListFull.add(userData);
                        }
                    }
                    updateStatistics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadUsersFromFirebase() {
        userList.clear();
        userListFull.clear();
        getSevaSarthiData();
        getFounderData();
        getWorkerData();
        getOfficerData();
        getOtherUserData();
    }

    private void updateStatistics() {
        int total = userListFull.size();
        int sevaMitraCount = 0, workerCount = 0, founderCount = 0, officerCount = 0;

        for (UserData user : userListFull) {
            switch (user.getUserType()) {
                case "SEVASARTHI":
                    sevaMitraCount++;
                    break;
                case "WORKER":
                    workerCount++;
                    break;
                case "FOUNDER":
                    founderCount++;
                    break;
                case "OFFICER":
                    officerCount++;
                    break;
            }
        }

        tvTotalUsers.setText(String.valueOf(total));
        tvSevaMitraCount.setText(String.valueOf(sevaMitraCount));
        tvWorkerCount.setText(String.valueOf(workerCount));
        tvFounderCount.setText(String.valueOf(founderCount));
        tvOfficerCount.setText(String.valueOf(officerCount));
    }

    private void updateNoDataVisibility() {
        if (userList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        btnCloseForm.setOnClickListener(v -> hideForm());
        btnUpdateUser.setOnClickListener(v -> updateUser());
        btnDeleteUser.setOnClickListener(v -> {
            if (editingUser != null) {
                showDeleteConfirmation(editingUser);
            }
        });
    }

    private void editUser(UserData user) {
        editingUser = user;
        tvFormTitle.setText("Edit User");
        tvUserId.setText("User ID: " + user.getId());
        tvCreatedDate.setText("Joined: " + getCurrentDate());

        // Fill common fields
        etFullName.setText(user.getFullName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhone());
        etAddress.setText(user.getAddress());
        etState.setText(user.getState());
        etCity.setText(user.getCity());

        // Set user type
        String[] userTypes = {"Select User Type", "SEVASARTHI", "WORKER", "FOUNDER", "OFFICER", "OTHER"};
        for (int i = 0; i < userTypes.length; i++) {
            if (userTypes[i].equals(user.getUserType())) {
                spinnerUserType.setSelection(i);
                break;
            }
        }

        if (user.getState() != null) {
            String[] statuses = {"Select Status", Status.ACTIVE, Status.INACTIVE, Status.SUSPENDED};
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equalsIgnoreCase(user.getState())) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        } else spinnerStatus.setSelection(0);

        // Show/hide appropriate fields based on user type
        updateFormFields(user.getUserType());

        // Fill type-specific fields
        switch (user.getUserType()) {
            case "SEVASARTHI":
                etDepartment.setText(user.getDepartment());
                etDesignation.setText(user.getDesignation());
                break;
            case "WORKER":
                if (user.getFounderId() != null) {
                    // Set founder spinner
                    String[] founders = departmentList;
                    for (int i = 0; i < founders.length; i++) {
                        if (founders[i].equals(user.getDepartment())) {
                            spinnerFounder.setSelection(i);
                            setSelectedProblemType(departmentList[i]);
                            break;
                        }
                    }
                }
                break;
            case "FOUNDER":
                etCompanyName.setText(user.getCompanyName());
                etGstNumber.setText(user.getGstNumber());
                etOfficeAddress.setText(user.getOfficeAddress());
                break;
            case "OFFICER":
                String[] departments = departmentList;
                for (int i = 0; i < departments.length; i++) {
                    if (departments[i].equals(user.getDepartment())) {
                        spinnerOfficerDepartment.setSelection(i);
                        break;
                    }
                }
                etDistrict.setText(user.getDistrict());
                etDivision.setText(user.getDivision());
                break;
        }

        rvUsers.setVisibility(View.GONE);
        cardEditForm.setVisibility(View.VISIBLE);
        formLayout.setVisibility(View.VISIBLE);
        btnUpdateUser.setVisibility(View.VISIBLE);
        btnDeleteUser.setVisibility(View.VISIBLE);
    }

    private void setSelectedProblemType(String selectedProblemType) {
        int arrayResId;
        switch (selectedProblemType) {
            case "Beauty & Personal Care":
                arrayResId = R.array.beauty_personal_care_issues;
                break;
            case "Carpenter":
                arrayResId = R.array.carpenter_issues;
                break;
            case "Computer":
                arrayResId = R.array.computer_issues;
                break;
            case "Computer & IT Services":
                arrayResId = R.array.computer_it_services_issues;
                break;
            case "Dairy Services":
                arrayResId = R.array.dairy_services_issues;
                break;
            case "Decoration":
                arrayResId = R.array.decoration_issues;
                break;
            case "Electrician":
                arrayResId = R.array.electrician_issues;
                break;
            case "Home Services":
                arrayResId = R.array.home_services_issues;
                break;
            case "Mechanic":
                arrayResId = R.array.mechanic_issues;
                break;
            case "Painter":
                arrayResId = R.array.painter_issues;
                break;
            case "Plumber":
                arrayResId = R.array.plumber_issues;
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
        setSkillsList(issueList);
    }

    private void setSkillsList(String[] skills) {
        if (skills != null && skills.length > 1)
            skills = Arrays.copyOfRange(skills, 1, skills.length);
        skillsLayout.removeAllViews();
        skillCheckBoxes.clear();
        for (String skill : skills) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(skill);
            checkBox.setTextColor(getResources().getColor(R.color.logo_white));

            skillsLayout.addView(checkBox);
            skillCheckBoxes.add(checkBox);
            if (editingUser.getSkills() != null && editingUser.getSkills().contains(skill)) {
                checkBox.setChecked(true);
            }
        }
    }

    private void updateFormFields(String userType) {
        // Hide all type-specific fields first
        findViewById(R.id.sevaMitraFields).setVisibility(View.GONE);
        findViewById(R.id.workerFields).setVisibility(View.GONE);
        findViewById(R.id.founderFields).setVisibility(View.GONE);
        findViewById(R.id.officerFields).setVisibility(View.GONE);

        // Show relevant fields based on user type
        switch (userType) {
            case "SEVASARTHI":
                findViewById(R.id.sevaMitraFields).setVisibility(View.VISIBLE);
                break;
            case "WORKER":
                findViewById(R.id.workerFields).setVisibility(View.VISIBLE);
                break;
            case "FOUNDER":
                findViewById(R.id.founderFields).setVisibility(View.VISIBLE);
                break;
            case "OFFICER":
                findViewById(R.id.officerFields).setVisibility(View.VISIBLE);
                break;
        }
    }

    private ArrayList<String> getSelectedSkills() {
        ArrayList<String> skills = new ArrayList<>();
        for (CheckBox skill : skillCheckBoxes) {
            if (skill.isChecked())
                skills.add(skill.getText().toString());
        }
        return skills;
    }

    private void updateUser() {
        if (editingUser == null || !validateForm()) return;

        // Update common fields
        editingUser.setFullName(etFullName.getText().toString().trim());
        editingUser.setEmail(etEmail.getText().toString().trim());
        editingUser.setPhone(etPhone.getText().toString().trim());
        editingUser.setAddress(etAddress.getText().toString().trim());
        editingUser.setState(etState.getText().toString().trim());
        editingUser.setCity(etCity.getText().toString().trim());

        // Update type-specific fields
        switch (editingUser.getUserType()) {
            case "SEVASARTHI":
                editingUser.setDepartment(etDepartment.getText().toString().trim());
                editingUser.setDesignation(etDesignation.getText().toString().trim());
                break;
            case "WORKER":
                editingUser.setSkills(getSelectedSkills());
                editingUser.setDepartment(spinnerFounder.getSelectedItem().toString());
                break;
            case "FOUNDER":
                editingUser.setCompanyName(etCompanyName.getText().toString().trim());
                editingUser.setGstNumber(etGstNumber.getText().toString().trim());
                editingUser.setOfficeAddress(etOfficeAddress.getText().toString().trim());
                break;
            case "OFFICER":
                editingUser.setDepartment(spinnerOfficerDepartment.getSelectedItem().toString());
                editingUser.setDistrict(etDistrict.getText().toString().trim());
                editingUser.setDivision(etDivision.getText().toString().trim());
                break;
        }

        progressBar.setVisibility(View.VISIBLE);
        database.getReference().child("UserData").child(editingUser.getUserType().toUpperCase()).child(editingUser.getId()).setValue(editingUser).addOnSuccessListener((aVoid) -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
            hideForm();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteUser(UserData user) {
        progressBar.setVisibility(View.VISIBLE);
        database.getReference().child("UserData").child(user.getUserType()).child(user.getId()).removeValue().addOnSuccessListener((aVoid) -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User Delete successfully!", Toast.LENGTH_SHORT).show();
            hideForm();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to Delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmation(UserData user) {
        new android.app.AlertDialog.Builder(this).setTitle("Delete User").setMessage("Are you sure you want to delete " + user.getFullName() + "?").setPositiveButton("Delete", (dialog, which) -> deleteUser(user)).setNegativeButton("Cancel", null).show();
    }

    private boolean validateForm() {
        if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            return false;
        }
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return false;
        }

        // Validate type-specific fields
        switch (editingUser.getUserType()) {
            case "WORKER":
                if (getSelectedSkills().isEmpty()) {
                    Toast.makeText(this, "Please select at least one skill", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case "OFFICER":
                if (spinnerOfficerDepartment.getSelectedItem().toString().equals("Select Department")) {
                    Toast.makeText(this, "Please select department", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }

        return true;
    }

    private void hideForm() {
        rvUsers.setVisibility(View.VISIBLE);
        cardEditForm.setVisibility(View.GONE);
        formLayout.setVisibility(View.GONE);
        editingUser = null;
        btnUpdateUser.setVisibility(View.GONE);
        btnDeleteUser.setVisibility(View.GONE);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Adapter Inner Class
    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private List<UserData> users;
        private OnUserClickListener listener;

        public interface OnUserClickListener {
            void onUserClick(UserData user);

            void onEditClick(UserData user);

            void onDeleteClick(UserData user);
        }

        public UserAdapter(List<UserData> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            UserData user = users.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            private CardView cardView;
            private TextView tvName, tvEmail, tvPhone, tvUserType, tvLocation, tvDetails;
            private ImageView ivUserType, ivEdit, ivDelete;

            public UserViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardView);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvUserType = itemView.findViewById(R.id.tvUserType);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvDetails = itemView.findViewById(R.id.tvDetails);
                ivUserType = itemView.findViewById(R.id.ivUserType);
                ivEdit = itemView.findViewById(R.id.ivEdit);
                ivDelete = itemView.findViewById(R.id.ivDelete);
            }

            public void bind(UserData user) {
                tvName.setText(user.getFullName());
                tvEmail.setText(user.getEmail());
                tvPhone.setText(user.getPhone());
                tvLocation.setText((user.getCity() != null ? user.getCity() : "") + (user.getState() != null ? ", " + user.getState() : ""));

                switch (user.getUserType()) {
                    case "SEVASARTHI":
                        tvUserType.setText("🌟 SevaSarthi");
                        tvUserType.setTextColor(itemView.getContext().getColor(R.color.logo_gold));
                        ivUserType.setImageResource(R.drawable.ic_sevamitra);
                        tvDetails.setText(user.getDepartment() + " | " + user.getDesignation());
                        break;
                    case "WORKER":
                        tvUserType.setText("🔧 Worker");
                        tvUserType.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                        ivUserType.setImageResource(R.drawable.ic_worker);
                        String skills = user.getSkills() != null ? TextUtils.join(", ", user.getSkills()) : "No skills";
                        tvDetails.setText(skills);
                        break;
                    case "FOUNDER":
                        tvUserType.setText("👔 Founder");
                        tvUserType.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                        ivUserType.setImageResource(R.drawable.ic_founder);
                        tvDetails.setText(user.getCompanyName());
                        break;
                    case "OFFICER":
                        tvUserType.setText("📋 Officer");
                        tvUserType.setTextColor(itemView.getContext().getColor(R.color.logo_gold_light));
                        ivUserType.setImageResource(R.drawable.ic_officer);
                        tvDetails.setText(user.getDepartment() + " | " + user.getDistrict());
                        break;
                }
                cardView.setOnClickListener(v -> listener.onUserClick(user));
                ivEdit.setOnClickListener(v -> listener.onEditClick(user));
                ivDelete.setOnClickListener(v -> listener.onDeleteClick(user));
            }
        }
    }
}