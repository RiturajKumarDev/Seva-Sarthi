package com.rituraj.sevamitra.ui.issues;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Priority;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.auth.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class IssueDetailsActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Toolbar
    private Toolbar toolbar;

    // Issue Details Cards
    private CardView cardBasicInfo, cardDescription, cardLocation, cardTimeline, cardUserInfo;

    // Basic Info Views
    private TextView tvIssueId, tvProblemTitle, tvProblemType, tvIssueType, tvPriority, tvStatus;
    private View priorityIndicator, statusIndicator;

    // Description Views
    private TextView tvProblemDescription;

    // Location Views
    private TextView tvLocation;

    // Timeline Views
    private TextView tvCreatedTime, tvRejectTime, tvAssignedTime, tvCompletedTime, tvSevaApprovedTime, tvOfficerApprovedTime;

    // Resolution Views
    private TextView tvResolutionNotes, resolutionHeading;
    private CardView cardResolution, cardResolutionNote;

    // User Info Views (Created By)
    private TextView tvCreatedByName, tvCreatedByType, tvCreatedByEmail, tvCreatedByPhone;
    private ImageView ivCreatedByIcon;

    // Assigned Worker Views
    private TextView tvAssignedWorkerName, tvAssignedWorkerType, tvAssignedWorkerSkills, tvAssignedWorkerPhone;
    private CardView cardAssignedWorker;
    private ImageView ivAssignedWorkerIcon;
    private ArrayList<UserData> workerList = new ArrayList<>();
    private TextInputEditText etResolveNote;

    // Founder Views
    private TextView tvFounderName, tvFounderCompany, tvFounderEmail, tvFounderPhone;
    private CardView cardFounder;
    private ImageView ivFounderIcon;

    // Officer Views (Approved By)
    private TextView tvOfficerName, tvOfficerDistrict, tvOfficerEmail, tvOfficerPhone;
    private CardView cardOfficer;
    private ImageView ivOfficerIcon;

    // Action Buttons
    private LinearLayout btnAssign, btnApprove, btnReject, btnMarkComplete;
    private ProgressBar progressBar;

    // Data
    private IssueModel issue = new IssueModel();
    private String issueId = "";
    private String userType;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            if (firebaseUser.getPhotoUrl() != null) {
                userType = firebaseUser.getPhotoUrl().toString();
            } else {
                userType = "";
            }
        } else {
            userId = "";
            userType = "";
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        issueId = getIntent().getStringExtra("IssueId");

        initViews();
        setupToolbar();
        loadIssueData();
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 500);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Cards
        cardBasicInfo = findViewById(R.id.cardBasicInfo);
        cardDescription = findViewById(R.id.cardDescription);
        cardLocation = findViewById(R.id.cardLocation);
        cardTimeline = findViewById(R.id.cardTimeline);
        cardResolution = findViewById(R.id.cardResolution);
        cardUserInfo = findViewById(R.id.cardUserInfo);
        cardAssignedWorker = findViewById(R.id.cardAssignedWorker);
        cardFounder = findViewById(R.id.cardFounder);
        cardOfficer = findViewById(R.id.cardOfficer);

        // Basic Info
        etResolveNote = findViewById(R.id.etResolveNote);
        tvIssueId = findViewById(R.id.tvIssueId);
        tvProblemTitle = findViewById(R.id.tvProblemTitle);
        tvProblemType = findViewById(R.id.tvProblemType);
        tvIssueType = findViewById(R.id.tvIssueType);
        tvPriority = findViewById(R.id.tvPriority);
        tvStatus = findViewById(R.id.tvStatus);
        priorityIndicator = findViewById(R.id.priorityIndicator);
        statusIndicator = findViewById(R.id.statusIndicator);

        // Description
        tvProblemDescription = findViewById(R.id.tvProblemDescription);

        // Location
        tvLocation = findViewById(R.id.tvLocation);

        // Timeline
        tvCreatedTime = findViewById(R.id.tvCreatedTime);
        tvRejectTime = findViewById(R.id.tvRejectTime);
        tvAssignedTime = findViewById(R.id.tvAssignedTime);
        tvCompletedTime = findViewById(R.id.tvCompletedTime);
        tvSevaApprovedTime = findViewById(R.id.tvSevaApprovedTime);
        tvOfficerApprovedTime = findViewById(R.id.tvOfficerApprovedTime);

        // Resolution
        tvResolutionNotes = findViewById(R.id.tvResolutionNotes);
        resolutionHeading = findViewById(R.id.resolutionHeading);
        cardResolutionNote = findViewById(R.id.cardResolutionNote);

        // Created By User
        tvCreatedByName = findViewById(R.id.tvCreatedByName);
        tvCreatedByType = findViewById(R.id.tvCreatedByType);
        tvCreatedByEmail = findViewById(R.id.tvCreatedByEmail);
        tvCreatedByPhone = findViewById(R.id.tvCreatedByPhone);
        ivCreatedByIcon = findViewById(R.id.ivCreatedByIcon);

        // Assigned Worker
        tvAssignedWorkerName = findViewById(R.id.tvAssignedWorkerName);
        tvAssignedWorkerType = findViewById(R.id.tvAssignedWorkerType);
        tvAssignedWorkerSkills = findViewById(R.id.tvAssignedWorkerSkills);
        tvAssignedWorkerPhone = findViewById(R.id.tvAssignedWorkerPhone);
        ivAssignedWorkerIcon = findViewById(R.id.ivAssignedWorkerIcon);

        // Founder
        tvFounderName = findViewById(R.id.tvFounderName);
        tvFounderCompany = findViewById(R.id.tvFounderCompany);
        tvFounderEmail = findViewById(R.id.tvFounderEmail);
        tvFounderPhone = findViewById(R.id.tvFounderPhone);
        ivFounderIcon = findViewById(R.id.ivFounderIcon);

        // Officer
        tvOfficerName = findViewById(R.id.tvOfficerName);
        tvOfficerDistrict = findViewById(R.id.tvOfficerDistrict);
        tvOfficerEmail = findViewById(R.id.tvOfficerEmail);
        tvOfficerPhone = findViewById(R.id.tvOfficerPhone);
        ivOfficerIcon = findViewById(R.id.ivOfficerIcon);

        // Action Buttons
        btnAssign = findViewById(R.id.btnAssign);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        progressBar = findViewById(R.id.progressBar);
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
            getSupportActionBar().setTitle("Issue Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadIssueData() {
        if (issueId == null || issueId.isEmpty()) {
            Toast.makeText(IssueDetailsActivity.this, "Invalid Issue Id", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        reference = database.getReference().child("Issues").child(issueId);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    IssueModel remoteIssue = snapshot.getValue(IssueModel.class);
                    if (remoteIssue != null) {
                        issue = remoteIssue;
                        issue.setId(issueId);
                        displayIssueData();
                        loadUserData();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                showErrorAndFinish("Error loading issue: " + error.getMessage());
            }
        });
    }

    private void displayIssueData() {
        // Basic Info
        tvIssueId.setText("#" + (issue.getId() != null ? issue.getId() : ""));
        tvProblemTitle.setText(issue.getProblemTitle() != null ? issue.getProblemTitle() : "");
        tvProblemType.setText(issue.getProblemType() != null ? issue.getProblemType() : "");
        tvIssueType.setText(issue.getIssue() != null ? issue.getIssue() : "");
        tvPriority.setText(issue.getPriority() != null ? issue.getPriority() : Priority.MEDIUM);
        tvStatus.setText(issue.getStatus() != null ? issue.getStatus() : Status.PENDING);

        // Set Priority Color
        String priority = issue.getPriority();
        if (priority == null) priority = Priority.MEDIUM;
        switch (priority) {
            case Priority.CRITICAL:
                priorityIndicator.setBackgroundColor(getColor(R.color.logo_orange));
                tvPriority.setTextColor(getColor(R.color.logo_orange));
                break;
            case Priority.HIGH:
                priorityIndicator.setBackgroundColor(getColor(R.color.logo_gold));
                tvPriority.setTextColor(getColor(R.color.logo_gold));
                break;
            case Priority.MEDIUM:
                priorityIndicator.setBackgroundColor(getColor(R.color.logo_green));
                tvPriority.setTextColor(getColor(R.color.logo_green));
                break;
            default:
                priorityIndicator.setBackgroundColor(getColor(R.color.logo_gold_light));
                tvPriority.setTextColor(getColor(R.color.logo_gold_light));
                break;
        }

        // Set Status Color
        String status = issue.getStatus();
        if (status == null) status = Status.PENDING;
        switch (status) {
            case Status.PENDING:
                statusIndicator.setBackgroundColor(getColor(R.color.logo_orange));
                tvStatus.setTextColor(getColor(R.color.logo_orange));
                break;
            case Status.PROCESS:
                statusIndicator.setBackgroundColor(getColor(R.color.logo_gold));
                tvStatus.setTextColor(getColor(R.color.logo_gold));
                break;
            case Status.RESOLVED:
                statusIndicator.setBackgroundColor(getColor(R.color.logo_green));
                tvStatus.setTextColor(getColor(R.color.logo_green));
                break;
            case Status.REJECTED:
                statusIndicator.setBackgroundColor(getColor(R.color.logo_red));
                tvStatus.setTextColor(getColor(R.color.logo_red));
                break;
        }

        // Description
        tvProblemDescription.setText(issue.getProblemDescription());

        // Location
        tvLocation.setText(issue.getLocation());

        // Timeline
        tvCreatedTime.setText(formatTimestamp(issue.getCreatedTimestamp()));
        tvRejectTime.setText(formatTimestamp(issue.getRejectTimestamp()));
        tvAssignedTime.setText(formatTimestamp(issue.getWorkAssignTimestamp()));
        tvCompletedTime.setText(formatTimestamp(issue.getWorkCompleteTimestamp()));
        tvSevaApprovedTime.setText(formatTimestamp(issue.getSevaMitraApprovedTimestamp()));
        tvOfficerApprovedTime.setText(formatTimestamp(issue.getOfficerApprovedTimestamp()));
        tvOfficerApprovedTime.setText(formatTimestamp(issue.getOfficerApprovedTimestamp()));

        // Show/hide timeline items based on availability
        if (issue.getWorkAssignTimestamp() <= 0) {
            findViewById(R.id.assignedRow).setVisibility(View.GONE);
        }
        if (issue.getRejectTimestamp() <= 0) {
            findViewById(R.id.rejectRow).setVisibility(View.GONE);
        }
        if (issue.getWorkCompleteTimestamp() <= 0) {
            findViewById(R.id.completedRow).setVisibility(View.GONE);
        }
        if (issue.getSevaMitraApprovedTimestamp() <= 0) {
            findViewById(R.id.sevaRow).setVisibility(View.GONE);
        }
        if (issue.getOfficerApprovedTimestamp() <= 0) {
            findViewById(R.id.officerRow).setVisibility(View.GONE);
        }

        // Resolution Notes
        if (issue.getResolutionNotes() != null && !issue.getResolutionNotes().isEmpty()) {
            if (issue.getRejectTimestamp() > 0) {
                resolutionHeading.setText("Rejection Notes");
                resolutionHeading.setTextColor(
                        ContextCompat.getColor(this, R.color.logo_red)
                );
            }
            tvResolutionNotes.setText(issue.getResolutionNotes());
            cardResolution.setVisibility(View.VISIBLE);
        } else {
            cardResolution.setVisibility(View.GONE);
        }

        // Setup action buttons based on user type and issue status
        setupActionButtons();
    }

    private void loadUserData() {
        // Load Created By User (SevaMitra)
        if (issue.getCreatedBy() != null && !issue.getCreatedBy().isEmpty()) {
            reference = database.getReference().child("UserData").child(issue.getUserType()).child(issue.getCreatedBy());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserData user = snapshot.getValue(UserData.class);
                        if (user != null)
                            displayCreatedByUser(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        // Load Assigned Worker
        if (issue.getAssignedTo() != null && !issue.getAssignedTo().isEmpty()) {
            cardAssignedWorker.setVisibility(View.VISIBLE);
            reference = database.getReference().child("UserData").child("WORKER").child(issue.getAssignedTo());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserData worker = snapshot.getValue(UserData.class);
                        if (worker != null)
                            displayAssignedWorker(worker);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            cardAssignedWorker.setVisibility(View.GONE);
        }

        // Load Founder
        if (issue.getFounderId() != null && !issue.getFounderId().isEmpty()) {
            cardFounder.setVisibility(View.VISIBLE);
            reference = database.getReference().child("UserData").child("FOUNDER").child(issue.getFounderId());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserData founder = snapshot.getValue(UserData.class);
                        if (founder != null)
                            displayFounder(founder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            cardFounder.setVisibility(View.GONE);
        }

        // Load Officer (Approved By)
        if (issue.getApprovedBy() != null && !issue.getApprovedBy().isEmpty()) {
            cardOfficer.setVisibility(View.VISIBLE);
            reference = database.getReference().child("UserData").child("OFFICER").child(issue.getApprovedBy());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserData officer = snapshot.getValue(UserData.class);
                        if (officer != null)
                            displayOfficer(officer);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            cardOfficer.setVisibility(View.GONE);
        }
    }

    private void displayCreatedByUser(UserData user) {
        tvCreatedByName.setText(user.getFullName());
        tvCreatedByType.setText("SevaSarthi");
        tvCreatedByEmail.setText(user.getEmail());
        tvCreatedByPhone.setText(user.getPhone());
        ivCreatedByIcon.setImageResource(R.drawable.ic_profile);
    }

    private void displayAssignedWorker(UserData worker) {
        tvAssignedWorkerName.setText(worker.getFullName());
        tvAssignedWorkerType.setText(worker.getDepartment());

        // Display skills
        StringBuilder skills = new StringBuilder();
        if (worker.getSkills() != null && !worker.getSkills().isEmpty()) {
            for (String skill : worker.getSkills()) {
                if (skills.length() > 0) skills.append(" • ");
                skills.append(skill);
            }
            tvAssignedWorkerSkills.setText(skills.toString());
        } else {
            tvAssignedWorkerSkills.setText("No additional skills");
        }

        tvAssignedWorkerPhone.setText(worker.getPhone());
        ivAssignedWorkerIcon.setImageResource(R.drawable.ic_worker);
    }

    private void displayFounder(UserData founder) {
        tvFounderName.setText(founder.getFullName());
        tvFounderCompany.setText(founder.getCompanyName());
        tvFounderEmail.setText(founder.getEmail());
        tvFounderPhone.setText(founder.getPhone());
        ivFounderIcon.setImageResource(R.drawable.ic_founder);
    }

    private void displayOfficer(UserData officer) {
        tvOfficerName.setText(officer.getFullName());
        tvOfficerDistrict.setText(officer.getDistrict() + " | " + officer.getDivision());
        tvOfficerEmail.setText(officer.getEmail());
        tvOfficerPhone.setText(officer.getPhone());
        ivOfficerIcon.setImageResource(R.drawable.ic_officer);
    }

    private void setupActionButtons() {
        // Hide all buttons by default
        btnAssign.setVisibility(View.GONE);
        btnApprove.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnMarkComplete.setVisibility(View.GONE);

        String status = issue.getStatus();

        // SevaMitra Actions
        if ("SEVASARTHI".equals(userType)) {
            if (issue.getWorkCompleteTimestamp() > 0 && issue.getSevaMitraApprovedTimestamp() == 0)
                btnMarkComplete.setVisibility(View.VISIBLE);
        }
        // Worker Actions
        else if ("WORKER".equals(userType)) {
            if (Status.PROCESS.equals(status) && issue.getWorkCompleteTimestamp() == 0 && issue.getAssignedTo() != null && issue.getAssignedTo().equals(userId)) {
                cardResolutionNote.setVisibility(View.VISIBLE);
                btnMarkComplete.setVisibility(View.VISIBLE);
            }
        }
        // Founder Actions
        else if ("FOUNDER".equals(userType)) {
            btnReject.setVisibility(View.VISIBLE);
            cardResolutionNote.setVisibility(View.VISIBLE);
            if ((Status.RESOLVED.equals(status) || Status.REJECTED.equals(status))) {
                btnReject.setVisibility(View.GONE);
                cardResolutionNote.setVisibility(View.GONE);
            }
        }
        // Officer Actions
        else if ("OFFICER".equals(userType)) {
            if (issue.getOfficerApprovedTimestamp() == 0 && issue.getSevaMitraApprovedTimestamp() > 0)
                btnApprove.setVisibility(View.VISIBLE);
        }

        // Set click listeners
        btnAssign.setOnClickListener(v -> assignIssue());
        btnApprove.setOnClickListener(v -> approveIssue());
        btnReject.setOnClickListener(v -> rejectIssue());
        btnMarkComplete.setOnClickListener(v -> markComplete());
    }

    private void assignIssue() {
        // Show worker selection dialog
        Toast.makeText(this, "Assign worker functionality", Toast.LENGTH_SHORT).show();
    }

    private void approveIssue() {
        if ("OFFICER".equals(userType)) {
            reference = database.getReference().child("Issues").child(issueId);
            long timestamp = System.currentTimeMillis();
            reference.child("officerApprovedTimestamp").setValue(timestamp);
            reference.child("status").setValue(Status.RESOLVED);
            finish();
        }
    }

    private void rejectIssue() {
        reference = database.getReference().child("Issues").child(issueId);
        if (userType.equalsIgnoreCase("FOUNDER")) {
            String note = etResolveNote.getText().toString();
            if (note.isEmpty()) {
                etResolveNote.setError("Reject Note is require!!");
                return;
            }
            long timestamp = System.currentTimeMillis();
            reference.child("resolutionNotes").setValue(note);
            reference.child("status").setValue(Status.REJECTED);
            reference.child("rejectTimestamp").setValue(timestamp);
            finish();
        }
    }

    private void markComplete() {
        reference = database.getReference().child("Issues").child(issueId);
        if (userType.equalsIgnoreCase("WORKER")) {
            String note = etResolveNote.getText().toString();
            if (note.isEmpty()) {
                etResolveNote.setError("Resolution Note is require!!");
                return;
            }
            long timestamp = System.currentTimeMillis();
            reference.child("resolutionNotes").setValue(note);
            reference.child("workCompleteTimestamp").setValue(timestamp);
            finish();
        } else if (userType.equalsIgnoreCase("SEVASARTHI")) {
            long timestamp = System.currentTimeMillis();
            reference.child("sevaMitraApprovedTimestamp").setValue(timestamp);
            finish();
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "Not yet";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}