package com.rituraj.sevamitra.ui.issues;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Priority;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemDialog;

import java.util.*;

public class AddIssueActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Toolbar
    private Toolbar toolbar;

    // Problem Details
    private TextInputEditText etProblemTitle, etProblemDescription, etLocation;
    private TextInputLayout tilProblemTitle, tilProblemDescription, tilLocation;

    // Problem Type Spinner
    private Spinner spinnerFounder;
    private String selectedProblemType = "";

    // Issue Type Spinner
    private Spinner spinnerIssueType;
    private String selectedIssueType = "";

    // Priority Spinner
    private Spinner spinnerPriority;
    private String selectedPriority = "";
    private Calendar calendar;

    // Submit Button
    private MaterialButton btnSubmitIssue;
    private ProgressBar progressBar;
    private TextView tvError;

    // User ID (from login)
    private String userId;
    private ArrayList<UserData> founderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        userId = firebaseUser.getUid();

        if (getIntent().hasExtra("REQUEST_DEPARTMENT"))
            selectedProblemType = getIntent().getStringExtra("REQUEST_DEPARTMENT");

        // Initialize Firebase
        calendar = Calendar.getInstance();

        initViews();
        setupToolbar();
        setupSpinners();
        setSelectedProblemType();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Problem Details
        etProblemTitle = findViewById(R.id.etProblemTitle);
        etProblemDescription = findViewById(R.id.etProblemDescription);
        etLocation = findViewById(R.id.etLocation);
        tilProblemTitle = findViewById(R.id.tilProblemTitle);
        tilProblemDescription = findViewById(R.id.tilProblemDescription);
        tilLocation = findViewById(R.id.tilLocation);

        // Spinners
        spinnerFounder = findViewById(R.id.spinnerFounder);
        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        spinnerPriority = findViewById(R.id.spinnerPriority);

        // Buttons
        btnSubmitIssue = findViewById(R.id.btnSubmitIssue);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
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

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Register New Issue");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Priority Spinner
        String[] priorities = {"Select Priority", Priority.CRITICAL, Priority.HIGH, Priority.MEDIUM, Priority.LOW};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = position == 0 ? "" : priorities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getFounders();
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
        setSpinnerIssueType(issueList);
    }

    private void setSpinnerIssueType(String[] issueList) {
        // Issue Type Spinner
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issueList);
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssueType.setAdapter(issueAdapter);
        spinnerIssueType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedIssueType = position == 0 ? "" : (String) spinnerIssueType.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void getFounders() {
        reference = database.getReference().child("UserData").child("FOUNDER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
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
                    ArrayAdapter<UserData> founderAdapter = new ArrayAdapter<>(AddIssueActivity.this, android.R.layout.simple_spinner_dropdown_item, founderList);
                    spinnerFounder.setAdapter(founderAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupClickListeners() {
        btnSubmitIssue.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // Clear previous errors
        clearErrors();

        // Get values
        String problemTitle = etProblemTitle.getText().toString().trim();
        String problemDescription = etProblemDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(problemTitle)) {
            tilProblemTitle.setError("Problem title is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(problemDescription)) {
            tilProblemDescription.setError("Problem description is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(location)) {
            tilLocation.setError("Location is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(selectedProblemType)) {
            showError("Please select problem type");
            isValid = false;
        }

        if (TextUtils.isEmpty(selectedIssueType)) {
            showError("Please select issue type");
            isValid = false;
        }

        if (TextUtils.isEmpty(selectedPriority)) {
            showError("Please select priority");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create Issue Model
        long timestamp = System.currentTimeMillis();
        IssueModel issue = new IssueModel();
        issue.setUserType(firebaseUser.getPhotoUrl() != null ? String.valueOf(firebaseUser.getPhotoUrl()) : "Other");
        issue.setId(String.valueOf(timestamp));
        issue.setCreatedTimestamp(timestamp);
        issue.setProblemTitle(problemTitle);
        issue.setProblemDescription(problemDescription);
        issue.setLocation(location);
        issue.setFounderId("8X1ffJpBZmSftPyvww2fJxzBliD2");
        issue.setProblemType(selectedProblemType);
        issue.setIssue(selectedIssueType);
        issue.setPriority(selectedPriority);
        issue.setStatus(Status.PENDING);
        issue.setCreatedBy(userId);

        // Submit to Firebase
        submitIssueToFirebase(issue);
    }

    private void submitIssueToFirebase(IssueModel issue) {
        setLoading(true);
        reference = database.getReference().child("Issues").child(issue.getId());
        reference.keepSynced(true);
        reference.setValue(issue).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                setLoading(false);
                showSuccessAndFinish(issue);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setLoading(false);
                showError("Failed to submit issue: " + e.getMessage());
            }
        });
    }

    private void showSuccessAndFinish(IssueModel issue) {
        Toast.makeText(this, "Issue registered successfully!\nID: " + issue.getId(), Toast.LENGTH_LONG).show();

        // Navigate back or show success dialog
        finish();
    }

    private void clearErrors() {
        tilProblemTitle.setError(null);
        tilProblemDescription.setError(null);
        tilLocation.setError(null);
        tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);

        // Auto hide after 3 seconds
        new android.os.Handler().postDelayed(() -> tvError.setVisibility(View.GONE), 3000);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmitIssue.setEnabled(false);
            btnSubmitIssue.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmitIssue.setEnabled(true);
            btnSubmitIssue.setText("Register Issue");
        }
    }
}