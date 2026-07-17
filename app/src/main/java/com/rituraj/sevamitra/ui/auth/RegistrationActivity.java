package com.rituraj.sevamitra.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.User;
import com.rituraj.sevamitra.models.UserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    // User Type Spinner
    private Spinner spinnerUserType;
    private CardView cardSevaMitraFields, cardWorkerFields, cardFounderFields, cardOfficerFields;

    // Common Fields
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputEditText etAddress;
    private Spinner spinnerState, spinnerCity;

    // SevaMitra (Earlier Officer) Specific Fields
    private TextInputEditText etDepartment, etDesignation;
    private TextView otherIfo;

    // Worker Specific Fields
    private LinearLayout workerSkillsLayout;
    private int FRONT_REQUEST_IMAGE = 100, BACK_REQUEST_IMAGE = 101;
    private Spinner spinnerWorkerCategory, spinnerOfficerDepartment, spinnerFounder;
    private ArrayList<UserData> founderList = new ArrayList<>();
    private ImageView aadharCardBackImg, aadharCardFrontImg;
    private Uri frontAadharImgUri, backAadharImgUri;
    private final List<CheckBox> skillCheckBoxes = new ArrayList<>();

    // Founder Specific Fields
    private TextInputEditText etCompanyName, etGstNumber, etOfficeAddress;

    // Officer (Earlier SDM) Specific Fields
    private TextInputEditText etDistrict, etDivision;

    // Buttons
    private MaterialButton btnRegister;
    private TextView btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Selected User Type
    private String selectedUserType = "";

    // State-City Mapping
    private Map<String, String[]> stateCityMap = new HashMap<>();

    // User Types Array
    private String[] userTypes = {"Select User Type", "SevaSarthi", "Worker", "Officer", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        if (mAuth.getCurrentUser() != null)
            userTypes = new String[]{"Select User Type", "SevaSarthi", "Worker", "Founder", "Officer", "Other"};

        initViews();
        setupStateCityMap();
        setupSpinners();
        setupUserTypeSpinner();
        setupClickListeners();
    }

    private void initViews() {
        // User Type Spinner
        spinnerUserType = findViewById(R.id.spinnerUserType);

        // Cards
        cardSevaMitraFields = findViewById(R.id.cardSevaMitraFields);
        cardWorkerFields = findViewById(R.id.cardWorkerFields);
        cardFounderFields = findViewById(R.id.cardFounderFields);
        cardOfficerFields = findViewById(R.id.cardOfficerFields);

        // Common Fields
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerCity = findViewById(R.id.spinnerCity);

        // SevaMitra Fields (Earlier Officer)
        etDepartment = findViewById(R.id.etDepartment);
        etDesignation = findViewById(R.id.etDesignation);
        otherIfo = findViewById(R.id.otherIfo);

        // Worker Fields
        workerSkillsLayout = findViewById(R.id.workerSkillsLayout);
        spinnerWorkerCategory = findViewById(R.id.spinnerWorkerCategory);
        spinnerFounder = findViewById(R.id.spinnerFounder);

        aadharCardFrontImg = findViewById(R.id.aadharCardFrontImg);
        aadharCardBackImg = findViewById(R.id.aadharCardBackImg);

        // Founder Fields
        etCompanyName = findViewById(R.id.etCompanyName);
        etGstNumber = findViewById(R.id.etGstNumber);
        etOfficeAddress = findViewById(R.id.etOfficeAddress);

        // Officer Fields (Earlier SDM)
        etDistrict = findViewById(R.id.etDistrict);
        etDivision = findViewById(R.id.etDivision);
        spinnerOfficerDepartment = findViewById(R.id.spinnerOfficerDepartment);

        // Buttons
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
    }

    private void setupStateCityMap() {
        stateCityMap.put("Bihar", new String[]{"Ara", "Araria", "Aurangabad", "Bagaha", "Banka", "Barh", "Barsoi", "Begusarai", "Bettiah", "Bhagalpur", "Bihar Sharif", "Bikramganj", "Bodh Gaya", "Buxar", "Chapra", "Dalsinghsarai", "Danapur", "Darbhanga", "Dehri", "Forbesganj", "Gaya", "Gopalganj", "Hajipur", "Hilsa", "Islampur", "Jamui", "Jehanabad", "Jhajha", "Kahalgaon", "Katihar", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura", "Madhubani", "Maner", "Manihari", "Masaurhi", "Mokama", "Motihari", "Munger", "Muzaffarpur", "Nabinagar", "Narkatiaganj", "Nawada", "Patna", "Purnia", "Rafiganj", "Raxaul", "Revelganj", "Rosera", "Saharsa", "Samastipur", "Sasaram", "Sheikhpura", "Sherghati", "Sitamarhi", "Siwan", "Sultanganj", "Supaul", "Teghra", "Vaishali", "Warisaliganj"});
        stateCityMap.put("Uttar Pradesh", new String[]{"Lucknow", "Kanpur", "Varanasi", "Agra", "Prayagraj", "Noida", "Ghaziabad"});
        stateCityMap.put("Maharashtra", new String[]{"Mumbai", "Pune", "Nagpur", "Nashik", "Thane"});
        stateCityMap.put("Delhi", new String[]{"New Delhi", "South Delhi", "East Delhi", "West Delhi", "North Delhi"});
        stateCityMap.put("Gujarat", new String[]{"Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar"});
        stateCityMap.put("Rajasthan", new String[]{"Jaipur", "Jodhpur", "Udaipur", "Kota", "Ajmer"});
        stateCityMap.put("Madhya Pradesh", new String[]{"Bhopal", "Indore", "Gwalior", "Jabalpur", "Ujjain"});
        stateCityMap.put("West Bengal", new String[]{"Kolkata", "Howrah", "Durgapur", "Siliguri", "Darjeeling"});
        stateCityMap.put("Tamil Nadu", new String[]{"Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem"});
        stateCityMap.put("Karnataka", new String[]{"Bengaluru", "Mysore", "Hubli", "Mangalore", "Belgaum"});
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

        spinnerWorkerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = position == 0 ? "" : (String) spinnerWorkerCategory.getSelectedItem();
                setSelectedProblemType(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getFounderList();
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
        workerSkillsLayout.removeAllViews();
        skillCheckBoxes.clear();
        for (String skill : skills) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(skill);
            checkBox.setTextColor(getResources().getColor(R.color.logo_white));

            workerSkillsLayout.addView(checkBox);
            skillCheckBoxes.add(checkBox);
        }
    }

    private void getFounderList() {
        reference = database.getReference().child("UserData").child("FOUNDER");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    founderList.clear();
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

    private void setupUserTypeSpinner() {
        // Create custom adapter for user type spinner
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, userTypes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (position == 0) {
                    ((TextView) v).setTextColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    ((TextView) v).setTextColor(getResources().getColor(R.color.logo_white));
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    ((TextView) v).setTextColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    ((TextView) v).setTextColor(getResources().getColor(R.color.logo_white));
                }
                return v;
            }
        };
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(userTypeAdapter);

        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Hide all cards
                    cardSevaMitraFields.setVisibility(View.GONE);
                    cardWorkerFields.setVisibility(View.GONE);
                    cardFounderFields.setVisibility(View.GONE);
                    cardOfficerFields.setVisibility(View.GONE);
                    selectedUserType = "";
                    return;
                }

                String selected = parent.getItemAtPosition(position).toString();

                // Hide all cards first
                cardSevaMitraFields.setVisibility(View.GONE);
                cardWorkerFields.setVisibility(View.GONE);
                cardFounderFields.setVisibility(View.GONE);
                cardOfficerFields.setVisibility(View.GONE);

                switch (selected) {
                    case "Worker":
                        selectedUserType = "WORKER";
                        cardWorkerFields.setVisibility(View.VISIBLE);
                        break;
                    case "Founder":
                        selectedUserType = "FOUNDER";
                        cardFounderFields.setVisibility(View.VISIBLE);
                        break;
                    case "Officer":
                        selectedUserType = "OFFICER";
                        cardOfficerFields.setVisibility(View.VISIBLE);
                        break;
                    case "Other":
                        selectedUserType = "OTHER";
                        otherIfo.setText("Other User Details");
                        cardSevaMitraFields.setVisibility(View.VISIBLE);
                        break;
                    case "SevaSarthi":
                        selectedUserType = "SEVASARTHI";
                        otherIfo.setText("SevaSarthi Details");
                        cardSevaMitraFields.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserType = "";
            }
        });
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> validateAndRegister());
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        aadharCardFrontImg.setOnClickListener((v) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");   // 🔥 only images from gallery
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(Intent.createChooser(intent, "Select Image"), FRONT_REQUEST_IMAGE);
        });
        aadharCardBackImg.setOnClickListener((v) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");   // 🔥 only images from gallery
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(Intent.createChooser(intent, "Select Image"), BACK_REQUEST_IMAGE);
        });
    }

    private void validateAndRegister() {
        // Check if user type is selected
        if (TextUtils.isEmpty(selectedUserType)) {
            showError("Please select user type");
            return;
        }

        // Common Validation
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String address = etAddress.getText().toString().trim();
        String state = spinnerState.getSelectedItem().toString();
        String city = spinnerCity.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName)) {
            showError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            showError("Valid 10-digit phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            showError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            showError("Address is required");
            etAddress.requestFocus();
            return;
        }

        // Create UserData object
        UserData userData = new UserData();
        userData.setFullName(fullName);
        userData.setEmail(email);
        userData.setPhone(phone);
        userData.setAddress(address);
        userData.setState(state);
        userData.setCity(city);
        userData.setUserType(selectedUserType);

        // Type specific validation and data
        switch (selectedUserType) {
            case "WORKER":
                if (!validateWorkerData(userData)) return;
                break;
            case "FOUNDER":
                if (!validateFounderData(userData)) return;
                break;
            case "OFFICER":
                if (!validateOfficerData(userData)) return;
                break;
            default:
                if (!validateSevaMitraData(userData)) return;
                break;
        }

        // Register with Firebase
        registerWithFirebase(email, password, userData);
    }

    private boolean validateSevaMitraData(UserData userData) {
        String department = etDepartment.getText().toString().trim();
        if (TextUtils.isEmpty(department)) {
            showError("Department is required");
            etDepartment.requestFocus();
            return false;
        }

        String designation = etDesignation.getText().toString().trim();
        if (TextUtils.isEmpty(designation)) {
            showError("Designation is required");
            etDesignation.requestFocus();
            return false;
        }

        userData.setDepartment(department);
        userData.setDesignation(designation);

        return true;
    }

    private boolean validateWorkerData(UserData userData) {
        String primaryCategory = spinnerWorkerCategory.getSelectedItem().toString();
        if (primaryCategory.equals("Select Department")) {
            showError("Please select Department");
            return false;
        }

        ArrayList<String> arraySkills = new ArrayList<>();
        for (CheckBox skill : skillCheckBoxes) {
            if (skill.isChecked())
                arraySkills.add(skill.getText().toString());
        }
        if (arraySkills.isEmpty()) {
            showError("Skills is required");
            return false;
        }

        userData.setFounderId(founderList.get(spinnerFounder.getSelectedItemPosition()).getId());
        userData.setDepartment(primaryCategory);
        userData.setSkills(arraySkills);
        userData.setIsSelected(Status.INACTIVE);

        return true;
    }

    private boolean validateFounderData(UserData userData) {
        String companyName = etCompanyName.getText().toString().trim();
        if (TextUtils.isEmpty(companyName)) {
            showError("Company name is required");
            etCompanyName.requestFocus();
            return false;
        }

        String gstNumber = etGstNumber.getText().toString().trim();
        if (TextUtils.isEmpty(gstNumber)) {
            showError("GST number is required");
            etGstNumber.requestFocus();
            return false;
        }

        String officeAddress = etOfficeAddress.getText().toString().trim();
        if (TextUtils.isEmpty(officeAddress)) {
            showError("Office address is required");
            etOfficeAddress.requestFocus();
            return false;
        }

        userData.setCompanyName(companyName);
        userData.setGstNumber(gstNumber);
        userData.setOfficeAddress(officeAddress);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            if (requestCode == FRONT_REQUEST_IMAGE) frontAadharImgUri = uri;
            else if (requestCode == BACK_REQUEST_IMAGE) backAadharImgUri = uri;
        }
    }

    private boolean validateOfficerData(UserData userData) {
        String district = etDistrict.getText().toString().trim();
        if (TextUtils.isEmpty(district)) {
            showError("District is required");
            etDistrict.requestFocus();
            return false;
        }

        String division = etDivision.getText().toString().trim();
        if (TextUtils.isEmpty(division)) {
            showError("Division is required");
            etDivision.requestFocus();
            return false;
        }

        String officerDepartment = spinnerOfficerDepartment.getSelectedItem().toString();
        if (officerDepartment.equals("Select Department")) {
            showError("Select Department");
            spinnerOfficerDepartment.requestFocus();
            return false;
        }

        userData.setDistrict(district);
        userData.setDivision(division);
        userData.setDepartment(officerDepartment);

        return true;
    }

    private void registerWithFirebase(String email, String password, UserData userData) {
        setLoading(true);
        tvError.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Registration success
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                User user = new User();
                if (firebaseUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userData.getFullName()).setPhotoUri(Uri.parse(selectedUserType)).build();
                    firebaseUser.updateProfile(profileUpdates);
                    user.setEmail(email);
                    user.setUserName(userData.getFullName());
                    user.setProfileUrl(selectedUserType);
                    user.setUserId(mAuth.getUid());
                    database.getReference().child("Users").child(user.getUserId()).setValue(user);
                    // Save user data to Firestore
                    saveUserToFirebase(user.getUserId(), userData);
                    mAuth.signOut();
                }
            } else {
                setLoading(false);
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                showError(errorMessage);
            }
        });
    }

    private void saveUserToFirebase(String userId, UserData userData) {
        reference = database.getReference().child("UserData").child(selectedUserType).child(userId);
        reference.keepSynced(true);
        reference.setValue(userData).addOnSuccessListener(aVoid -> {
            setLoading(false);
            showSuccessAndNavigate(userData);
        }).addOnFailureListener(e -> {
            setLoading(false);
            showError("Failed to save user data: " + e.getMessage());
        });
    }

    private void showSuccessAndNavigate(UserData userData) {
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show();

        // Navigate to respective dashboard
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnRegister.setText("Register");
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);

        // Auto hide after 3 seconds
        new android.os.Handler().postDelayed(() -> tvError.setVisibility(View.GONE), 3000);
    }
}