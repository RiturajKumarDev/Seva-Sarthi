package com.rituraj.sevamitra.ui.auth;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.rituraj.sevamitra.locations.LocationManager;
import com.rituraj.sevamitra.locations.PermissionManager;

import java.util.List;
import java.util.Locale;
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
    private TextInputEditText etState, etCity, etPincode;
    private Button btnEditPersonal, btnSavePersonal;
    private MaterialButton btnFetchLocation;
    private LinearLayout personalInfoLayout;
    private boolean isPersonalEditing = false;

    // Location
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private final String[] foregroundLocationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

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
        etPincode = view.findViewById(R.id.etPincode);
        btnEditPersonal = view.findViewById(R.id.btnEditPersonal);
        btnSavePersonal = view.findViewById(R.id.btnSavePersonal);
        btnFetchLocation = view.findViewById(R.id.btnFetchLocation);
        personalInfoLayout = view.findViewById(R.id.personalInfoLayout);

        takeUserLocation();

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
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");

        return new LanguageModel(name, code);
    }

    private void translationViews() {
        LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(view));
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

        ivEditProfile.setVisibility(View.VISIBLE);
        ivEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile Picture", Toast.LENGTH_SHORT).show();
        });
    }

    private void setAvatarColor(String name) {
        if (getContext() == null || name == null || name.isEmpty()) return;
        Context context = getContext();
        int color;
        char firstChar = Character.toUpperCase(name.charAt(0));
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
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());
            etAddress.setText(currentUser.getAddress());
            etState.setText(currentUser.getState());
            etCity.setText(currentUser.getCity());
            if (currentUser.getPinCode() != null) etPincode.setText(currentUser.getPinCode());
        }

        // Make ONLY location address fields editable
        setPersonalInfoEditable(false);

        btnEditPersonal.setOnClickListener(v -> {
            if (isPersonalEditing) {
                // Save mode
                savePersonalInfo();
            } else {
                // Edit mode (ONLY location address editable)
                setPersonalInfoEditable(true);
            }
        });

        btnSavePersonal.setOnClickListener(v -> savePersonalInfo());

        if (btnFetchLocation != null) {
            btnFetchLocation.setOnClickListener(v -> fetchUserLocation());
        }
    }

    private void setPersonalInfoEditable(boolean editable) {
        // Name, Email, Phone are kept locked / read-only as requested
        etFullName.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);

        // ONLY Location Address fields are editable
        etAddress.setEnabled(editable);
        etState.setEnabled(editable);
        etCity.setEnabled(editable);
        etPincode.setEnabled(editable);

        if (!editable) {
            btnEditPersonal.setText("Edit Location");
            btnSavePersonal.setVisibility(View.GONE);
            if (btnFetchLocation != null) btnFetchLocation.setVisibility(View.GONE);
            isPersonalEditing = false;
        } else {
            btnEditPersonal.setText("Cancel");
            btnSavePersonal.setVisibility(View.VISIBLE);
            if (btnFetchLocation != null) btnFetchLocation.setVisibility(View.VISIBLE);
            isPersonalEditing = true;
        }
    }

    private void savePersonalInfo() {
        if (currentUser == null) return;

        currentUser.setAddress(etAddress.getText() != null ? etAddress.getText().toString().trim() : "");
        currentUser.setState(etState.getText() != null ? etState.getText().toString().trim() : "");
        currentUser.setCity(etCity.getText() != null ? etCity.getText().toString().trim() : "");
        currentUser.setPinCode(etPincode.getText() != null ? etPincode.getText().toString().trim() : "");

        if (reference != null) {
            reference.setValue(currentUser).addOnSuccessListener(aVoid -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Location address saved successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setPersonalInfoEditable(false);
    }

    public void takeUserLocation() {
        if (getContext() == null) return;
        permissionManager = PermissionManager.getInstance(requireContext());
        locationManager = LocationManager.getInstance(requireContext());

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public void fetchUserLocation() {
        if (getContext() == null || getActivity() == null) return;
        if (permissionManager == null) {
            permissionManager = PermissionManager.getInstance(requireContext());
        }
        if (locationManager == null) {
            locationManager = LocationManager.getInstance(requireContext());
        }

        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(requireActivity(), foregroundLocationPermissions, 100);
        } else {
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastLocation();
                if (location != null) {
                    getAddressFromLocation(requireContext(), location.getLatitude(), location.getLongitude());
                } else {
                    locationManager.startLocationUpdates();
                    Toast.makeText(requireContext(), "Fetching GPS location...", Toast.LENGTH_SHORT).show();
                }
            } else {
                locationManager.createLocationRequest();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationManager != null) {
            locationManager.startLocationUpdates();
        }
        if (localBroadcastIntentFilter != null && getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
        }
    }

    @Override
    public void onPause() {
        if (localBroadcastIntentFilter != null && getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(foregroundLocationBroadCastReceiver);
        }
        if (locationManager != null) {
            locationManager.stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getActivity() != null && permissionManager != null && permissionManager.handlePermissionResult(requireActivity(), 100, permissions, grantResults)) {
            if (locationManager != null) {
                locationManager.createLocationRequest();
                fetchUserLocation();
            }
        } else {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("Lat") && intent.hasExtra("Lng")) {
                try {
                    double lat = Double.parseDouble(intent.getStringExtra("Lat"));
                    double lng = Double.parseDouble(intent.getStringExtra("Lng"));
                    getAddressFromLocation(context, lat, lng);
                } catch (Exception ignored) {
                }
            }
        }
    };

    private void getAddressFromLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressObj = addresses.get(0);
                String fullAddress = addressObj.getAddressLine(0);
                String city = addressObj.getLocality() != null ? addressObj.getLocality() : addressObj.getSubAdminArea();
                String state = addressObj.getAdminArea();

                if (fullAddress != null && etAddress != null) {
                    etAddress.setText(fullAddress);
                }

                if (state != null && etState != null) {
                    etState.setText(state);
                }

                if (city != null && etCity != null) {
                    etCity.setText(city);
                }

                String postalCode = addressObj.getPostalCode();
                if (postalCode != null && etPincode != null) {
                    etPincode.setText(postalCode);
                }
//                Toast.makeText(context, "Location fetched successfully!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Location fetched. Please verify address.", Toast.LENGTH_SHORT).show();
        }
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
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Change password", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getContext() == null || getActivity() == null) return;

                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirmation, null);
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
                MaterialButton btnConfirmLogout = dialogView.findViewById(R.id.btnConfirmLogout);

                btnCancel.setOnClickListener(v1 -> dialog.dismiss());

                btnConfirmLogout.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    if (getActivity() != null) {
                        auth.signOut();
                        requireActivity().startActivity(new Intent(requireActivity(), LoginActivity.class));
                        getActivity().finish();
                    }
                });

                dialog.show();
            });
        }
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
                        translationViews();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}