package com.rituraj.sevamitra.ui.dashboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private ImageView ivEditProfile;
    private CardView ivProfile;
    private TextView tvName, tvUserType, tvEmail, tvPhone, vtProfileLetter;
    private CardView cardProfileImage;

    // Personal Information Views
    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private TextInputEditText etState, etCity;
    private Button btnEditPersonal, btnSavePersonal;
    private LinearLayout personalInfoLayout;
    private boolean isPersonalEditing = false;

    // Officer Specific Views
    private CardView officerSection;
    private TextView tvDepartment, tvDesignation;
    private Button btnEditOfficer, btnSaveOfficer;
    private LinearLayout officerEditLayout;
    private TextInputEditText etDepartment, etDesignation, etEmployeeId;
    private boolean isOfficerEditing = false;

    // Worker Specific Views
    private CardView workerSection;
    private TextView tvPrimaryCategory, tvCategories;
    private TextView tvStatus, tvCompletedWorks, tvRating;
    private Button btnEditWorker, btnSaveWorker;
    private LinearLayout workerEditLayout;
    private TextInputEditText etPrimaryCategory, etSpecialization;
    private boolean isWorkerEditing = false;

    // Founder Specific Views
    private CardView founderSection;
    private TextView tvCompanyName, tvGstNumber, tvOfficeAddress;
    private Button btnEditFounder, btnSaveFounder;
    private LinearLayout founderEditLayout;
    private TextInputEditText etCompanyName, etGstNumber, etOfficeAddress;
    private boolean isFounderEditing = false;

    // SDM Specific Views
    private CardView sdmSection;
    private TextView tvDistrict, tvDivision, tvGovtId;
    private Button btnEditSDM, btnSaveSDM;
    private LinearLayout sdmEditLayout;
    private TextInputEditText etDistrict, etDivision, etGovtId;
    private boolean isSDMEditing = false;

    private Button btnChangePassword;
    private Button btnLogout;

    // Stats Views (for Worker)
    private UserData currentUser;
    private String userType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadSampleUserData(firebaseUser);

        setupBottomButtons(view);
        return view;
    }

    private void initViews(View view) {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        // Header
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivEditProfile = view.findViewById(R.id.ivEditProfile);
        tvName = view.findViewById(R.id.tvName);
        tvUserType = view.findViewById(R.id.tvUserType);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        cardProfileImage = view.findViewById(R.id.cardProfileImage);

        // Personal Information
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        etState = view.findViewById(R.id.etState);
        etCity = view.findViewById(R.id.etCity);
        btnEditPersonal = view.findViewById(R.id.btnEditPersonal);
        btnSavePersonal = view.findViewById(R.id.btnSavePersonal);
        personalInfoLayout = view.findViewById(R.id.personalInfoLayout);

        // Officer Section
        officerSection = view.findViewById(R.id.officerSection);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvDesignation = view.findViewById(R.id.tvDesignation);
        btnEditOfficer = view.findViewById(R.id.btnEditOfficer);
        btnSaveOfficer = view.findViewById(R.id.btnSaveOfficer);
        officerEditLayout = view.findViewById(R.id.officerEditLayout);
        etDepartment = view.findViewById(R.id.etDepartment);
        etDesignation = view.findViewById(R.id.etDesignation);
        etEmployeeId = view.findViewById(R.id.etEmployeeId);

        // Worker Section
        workerSection = view.findViewById(R.id.workerSection);
        tvPrimaryCategory = view.findViewById(R.id.tvPrimaryCategory);
        tvCategories = view.findViewById(R.id.tvCategories);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvCompletedWorks = view.findViewById(R.id.tvCompletedWorks);
        tvRating = view.findViewById(R.id.tvRating);
        btnEditWorker = view.findViewById(R.id.btnEditWorker);
        btnSaveWorker = view.findViewById(R.id.btnSaveWorker);
        workerEditLayout = view.findViewById(R.id.workerEditLayout);
        etPrimaryCategory = view.findViewById(R.id.etPrimaryCategory);
        etSpecialization = view.findViewById(R.id.etSpecialization);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Founder Section
        founderSection = view.findViewById(R.id.founderSection);
        tvCompanyName = view.findViewById(R.id.tvCompanyName);
        tvGstNumber = view.findViewById(R.id.tvGstNumber);
        tvOfficeAddress = view.findViewById(R.id.tvOfficeAddress);
        btnEditFounder = view.findViewById(R.id.btnEditFounder);
        btnSaveFounder = view.findViewById(R.id.btnSaveFounder);
        founderEditLayout = view.findViewById(R.id.founderEditLayout);
        etCompanyName = view.findViewById(R.id.etCompanyName);
        etGstNumber = view.findViewById(R.id.etGstNumber);
        etOfficeAddress = view.findViewById(R.id.etOfficeAddress);

        // SDM Section
        sdmSection = view.findViewById(R.id.sdmSection);
        tvDistrict = view.findViewById(R.id.tvDistrict);
        tvDivision = view.findViewById(R.id.tvDivision);
        tvGovtId = view.findViewById(R.id.tvGovtId);
        btnEditSDM = view.findViewById(R.id.btnEditSDM);
        btnSaveSDM = view.findViewById(R.id.btnSaveSDM);
        sdmEditLayout = view.findViewById(R.id.sdmEditLayout);
        etDistrict = view.findViewById(R.id.etDistrict);
        etDivision = view.findViewById(R.id.etDivision);
        etGovtId = view.findViewById(R.id.etGovtId);
        translationViews(view);
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");

        return new LanguageModel(name, code);
    }

    private void translationViews(View rootView) {
        LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(rootView));
    }

    private void setupHeader() {
        userType = currentUser.getUserType();
        tvName.setText(currentUser.getFullName());
        tvEmail.setText(currentUser.getEmail());
        tvPhone.setText(currentUser.getPhone());
        setAvatarColor(currentUser.getFullName());

        // Set user type with badge
        switch (userType) {
            case "SEVASARTHI":
                tvUserType.setText("👮 SevaSarthi");
                tvUserType.setBackgroundResource(R.drawable.user_type_badge_officer);
                break;
            case "WORKER":
                tvUserType.setText("🔧 Worker");
                tvUserType.setBackgroundResource(R.drawable.user_type_badge_worker);
                break;
            case "FOUNDER":
                tvUserType.setText("👔 Founder");
                tvUserType.setBackgroundResource(R.drawable.user_type_badge_founder);
                break;
            case "OFFICER":
                tvUserType.setText("📋 Officer");
                tvUserType.setBackgroundResource(R.drawable.user_type_badge_sdm);
                break;
        }

        ivEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change profile picture", Toast.LENGTH_SHORT).show();
        });
    }

    private void setAvatarColor(String name) {
        Context context = getContext();
        int color;
        char firstChar = name.charAt(0);
        vtProfileLetter.setText(String.valueOf(firstChar));

        if (firstChar >= 'A' && firstChar <= 'E') {
            color = context.getColor(R.color.avatar_color_1);
        } else if (firstChar >= 'F' && firstChar <= 'J') {
            color = context.getColor(R.color.avatar_color_2);
        } else if (firstChar >= 'K' && firstChar <= 'O') {
            color = context.getColor(R.color.avatar_color_3);
        } else if (firstChar >= 'P' && firstChar <= 'T') {
            color = context.getColor(R.color.avatar_color_4);
        } else {
            color = context.getColor(R.color.avatar_color_5);
        }
        ivProfile.setCardBackgroundColor(color);
    }

    private void setupPersonalInfo() {
        // Set personal info
        etFullName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        etAddress.setText(currentUser.getAddress());
        etState.setText(currentUser.getState());
        etCity.setText(currentUser.getCity());

        // Make fields non-editable initially
        setPersonalInfoEditable(false);

        btnEditPersonal.setOnClickListener(v -> {
            if (isPersonalEditing) {
                // Save mode
                savePersonalInfo();
            } else {
                // Edit mode
                setPersonalInfoEditable(true);
                btnEditPersonal.setText("Cancel");
                btnSavePersonal.setVisibility(View.VISIBLE);
                isPersonalEditing = true;
            }
        });

        btnSavePersonal.setOnClickListener(v -> {
            savePersonalInfo();
        });
    }

    private void setPersonalInfoEditable(boolean editable) {
        etPhone.setEnabled(editable);
        etAddress.setEnabled(editable);
        etState.setEnabled(editable);
        etCity.setEnabled(editable);

        if (!editable) {
            btnEditPersonal.setText("Edit");
            btnSavePersonal.setVisibility(View.GONE);
            isPersonalEditing = false;
        }
    }

    private void savePersonalInfo() {
        // Save personal info to database
        currentUser.setFullName(etFullName.getText().toString());
        currentUser.setEmail(etEmail.getText().toString());
        currentUser.setPhone(etPhone.getText().toString());
        currentUser.setAddress(etAddress.getText().toString());
        currentUser.setState(etState.getText().toString());
        currentUser.setCity(etCity.getText().toString());

        // Update header
        tvName.setText(currentUser.getFullName());
        tvEmail.setText(currentUser.getEmail());
        tvPhone.setText(currentUser.getPhone());

        setPersonalInfoEditable(false);
        Toast.makeText(getContext(), "Personal information saved", Toast.LENGTH_SHORT).show();
    }

    private void setupTypeSpecificSections() {
        // Hide all sections first
        officerSection.setVisibility(View.GONE);
        workerSection.setVisibility(View.GONE);
        founderSection.setVisibility(View.GONE);
        sdmSection.setVisibility(View.GONE);

        switch (userType) {
            case "SEVASARTHI":
                setupSevaMitraSection();
                break;
            case "WORKER":
                setupWorkerSection();
                break;
            case "FOUNDER":
                setupFounderSection();
                break;
            case "OFFICER":
                setupOfficerSection();
                break;
        }
    }

    private void setupSevaMitraSection() {
        officerSection.setVisibility(View.VISIBLE);

        tvDepartment.setText(currentUser.getDepartment());
        tvDesignation.setText(currentUser.getDesignation());

        etDepartment.setText(currentUser.getDepartment());
        etDesignation.setText(currentUser.getDesignation());

        setOfficerEditable(false);

        btnEditOfficer.setOnClickListener(v -> {
            if (isOfficerEditing) {
                saveOfficerInfo();
            } else {
                setOfficerEditable(true);
                btnEditOfficer.setText("Cancel");
                btnSaveOfficer.setVisibility(View.VISIBLE);
                isOfficerEditing = true;
            }
        });

        btnSaveOfficer.setOnClickListener(v -> saveOfficerInfo());
    }

    private void setOfficerEditable(boolean editable) {
        etDepartment.setEnabled(editable);
        etDesignation.setEnabled(editable);
        etEmployeeId.setEnabled(editable);

        if (editable) {
            officerEditLayout.setVisibility(View.VISIBLE);
        } else {
            officerEditLayout.setVisibility(View.GONE);
            btnEditOfficer.setText("Edit");
            btnSaveOfficer.setVisibility(View.GONE);
            isOfficerEditing = false;
        }
    }

    private void saveOfficerInfo() {
        currentUser.setDepartment(etDepartment.getText().toString());
        currentUser.setDesignation(etDesignation.getText().toString());
//        currentUser.setEmployeeId(etEmployeeId.getText().toString());

        tvDepartment.setText(currentUser.getDepartment());
        tvDesignation.setText(currentUser.getDesignation());
//        tvEmployeeId.setText(currentUser.getEmployeeId());

        setOfficerEditable(false);
        Toast.makeText(getContext(), "Officer information saved", Toast.LENGTH_SHORT).show();
    }

    private void setupWorkerSection() {
        workerSection.setVisibility(View.VISIBLE);

        tvPrimaryCategory.setText(currentUser.getDepartment());

        // Display categories
        StringBuilder categoriesStr = new StringBuilder();
        if (currentUser.getSkills() != null) {
            for (String category : currentUser.getSkills()) {
                if (categoriesStr.length() > 0) categoriesStr.append(", ");
                categoriesStr.append(category);
            }
        }
        tvCategories.setText(categoriesStr.toString());

        // Set status with color
        String status = currentUser.getIsSelected();
        tvStatus.setText(status);
        if ("Available".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.logo_green));
        } else if ("Busy".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.logo_orange));
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.logo_gold_dark));
        }

        // Sample stats - replace with actual data
        tvCompletedWorks.setText("156");
        tvRating.setText("4.8 ★");

        etPrimaryCategory.setText(currentUser.getDepartment());

        setWorkerEditable(false);

        btnEditWorker.setOnClickListener(v -> {
            if (isWorkerEditing) {
                saveWorkerInfo();
            } else {
                setWorkerEditable(true);
                btnEditWorker.setText("Cancel");
                btnSaveWorker.setVisibility(View.VISIBLE);
                isWorkerEditing = true;
            }
        });

        btnSaveWorker.setOnClickListener(v -> saveWorkerInfo());
    }

    private void setWorkerEditable(boolean editable) {
        etPrimaryCategory.setEnabled(editable);
        etSpecialization.setEnabled(editable);

        if (editable) {
            workerEditLayout.setVisibility(View.VISIBLE);
        } else {
            workerEditLayout.setVisibility(View.GONE);
            btnEditWorker.setText("Edit");
            btnSaveWorker.setVisibility(View.GONE);
            isWorkerEditing = false;
        }
    }

    private void saveWorkerInfo() {
        currentUser.setDepartment(etPrimaryCategory.getText().toString());

        tvPrimaryCategory.setText(currentUser.getDepartment());

        setWorkerEditable(false);
        Toast.makeText(getContext(), "Worker information saved", Toast.LENGTH_SHORT).show();
    }

    private void setupFounderSection() {
        founderSection.setVisibility(View.VISIBLE);

        tvCompanyName.setText(currentUser.getCompanyName());
        tvGstNumber.setText(currentUser.getGstNumber());
        tvOfficeAddress.setText(currentUser.getOfficeAddress());

        etCompanyName.setText(currentUser.getCompanyName());
        etGstNumber.setText(currentUser.getGstNumber());
        etOfficeAddress.setText(currentUser.getOfficeAddress());

        setFounderEditable(false);

        btnEditFounder.setOnClickListener(v -> {
            if (isFounderEditing) {
                saveFounderInfo();
            } else {
                setFounderEditable(true);
                btnEditFounder.setText("Cancel");
                btnSaveFounder.setVisibility(View.VISIBLE);
                isFounderEditing = true;
            }
        });

        btnSaveFounder.setOnClickListener(v -> saveFounderInfo());
    }

    private void setFounderEditable(boolean editable) {
        etCompanyName.setEnabled(editable);
        etGstNumber.setEnabled(editable);
        etOfficeAddress.setEnabled(editable);

        if (editable) {
            founderEditLayout.setVisibility(View.VISIBLE);
        } else {
            founderEditLayout.setVisibility(View.GONE);
            btnEditFounder.setText("Edit");
            btnSaveFounder.setVisibility(View.GONE);
            isFounderEditing = false;
        }
    }

    private void saveFounderInfo() {
        currentUser.setCompanyName(etCompanyName.getText().toString());
        currentUser.setGstNumber(etGstNumber.getText().toString());
        currentUser.setOfficeAddress(etOfficeAddress.getText().toString());

        tvCompanyName.setText(currentUser.getCompanyName());
        tvGstNumber.setText(currentUser.getGstNumber());
        tvOfficeAddress.setText(currentUser.getOfficeAddress());

        setFounderEditable(false);
        Toast.makeText(getContext(), "Founder information saved", Toast.LENGTH_SHORT).show();
    }

    private void setupOfficerSection() {
        sdmSection.setVisibility(View.VISIBLE);

        tvDistrict.setText(currentUser.getDistrict());
        tvDivision.setText(currentUser.getDivision());
        tvGovtId.setText(currentUser.getDepartment());

        etDistrict.setText(currentUser.getDistrict());
        etDivision.setText(currentUser.getDivision());
        etGovtId.setText(currentUser.getDepartment());

        setSDMEditable(false);

        btnEditSDM.setOnClickListener(v -> {
            if (isSDMEditing) {
                saveSDMInfo();
            } else {
                setSDMEditable(true);
                btnEditSDM.setText("Cancel");
                btnSaveSDM.setVisibility(View.VISIBLE);
                isSDMEditing = true;
            }
        });

        btnSaveSDM.setOnClickListener(v -> saveSDMInfo());
    }

    private void setSDMEditable(boolean editable) {
        etDistrict.setEnabled(editable);
        etDivision.setEnabled(editable);
        etGovtId.setEnabled(editable);

        if (editable) {
            sdmEditLayout.setVisibility(View.VISIBLE);
        } else {
            sdmEditLayout.setVisibility(View.GONE);
            btnEditSDM.setText("Edit");
            btnSaveSDM.setVisibility(View.GONE);
            isSDMEditing = false;
        }
    }

    private void saveSDMInfo() {
        currentUser.setDistrict(etDistrict.getText().toString());
        currentUser.setDivision(etDivision.getText().toString());
        currentUser.setDepartment(etGovtId.getText().toString());

        tvDistrict.setText(currentUser.getDistrict());
        tvDivision.setText(currentUser.getDivision());
        tvGovtId.setText(currentUser.getDepartment());

        setSDMEditable(false);
        Toast.makeText(getContext(), "SDM information saved", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomButtons(View view) {
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change password", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            // Logout logic
            if (getActivity() != null) {
                auth.signOut();
                requireActivity().startActivity(new Intent(requireActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    private void loadSampleUserData(FirebaseUser user) {
        String userType = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "FOUNDER";
        reference = database.getReference().child("UserData").child(userType).child(user.getUid());
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        currentUser = userData;
                        setupHeader();
                        setupPersonalInfo();
                        setupTypeSpecificSections();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}