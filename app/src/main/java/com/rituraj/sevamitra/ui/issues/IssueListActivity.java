package com.rituraj.sevamitra.ui.issues;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.IssueAdapter;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.util.ArrayList;
import java.util.List;

public class IssueListActivity extends AppCompatActivity implements IssueAdapter.OnIssueClickListener {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Views
    private Toolbar toolbar;
    private RecyclerView rvIssues;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private EditText etSearch;
    private ImageView ivSort, ivFilter;
    private Chip chipAll, chipPending, chipInProgress, chipResolved, chipRejected;
    private Chip chipCritical, chipHigh, chipMedium, chipLow;
    private LinearLayout sortOptionsLayout;
    private CardView cardFilter;
    private Button btnClearFilters;
    private TextView tvTotalIssues, tvPendingCount, tvInProgressCount, tvResolvedCount;

    // Adapter and Data
    private IssueAdapter issueAdapter;
    private List<IssueModel> issueList = new ArrayList<>();
    private List<IssueModel> issueListFull = new ArrayList<>();

    // Filters
    private String currentStatusFilter = "all";
    private String currentPriorityFilter = "all";
    private String currentSort = "date_desc";

    // User info
    private String userId;
    private String userType;
    private UserData officer;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_list);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        userId = firebaseUser.getUid();
        if (firebaseUser.getPhotoUrl() != null) {
            userType = firebaseUser.getPhotoUrl().toString();
        } else {
            userType = "WORKER"; // Default or handle appropriately
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupSortAndFilter();
        if (userType.equalsIgnoreCase("OFFICER")) getOfficerData();
        else loadIssuesFromFirebase();
    }

    private void getOfficerData() {
        reference = database.getReference().child("UserData").child(userType).child(userId);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    officer = snapshot.getValue(UserData.class);
                    if (officer != null) {
                        officer.setId(snapshot.getKey());
                        loadIssuesFromFirebase();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        toolbar = findViewById(R.id.toolbar);
        rvIssues = findViewById(R.id.rvIssues);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        etSearch = findViewById(R.id.etSearch);
        ivSort = findViewById(R.id.ivSort);
        ivFilter = findViewById(R.id.ivFilter);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipInProgress = findViewById(R.id.chipInProgress);
        chipResolved = findViewById(R.id.chipResolved);
        chipRejected = findViewById(R.id.chipRejected);
        chipCritical = findViewById(R.id.chipCritical);
        chipHigh = findViewById(R.id.chipHigh);
        chipMedium = findViewById(R.id.chipMedium);
        chipLow = findViewById(R.id.chipLow);
        sortOptionsLayout = findViewById(R.id.sortOptionsLayout);
        cardFilter = findViewById(R.id.cardFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        tvTotalIssues = findViewById(R.id.tvTotalIssues);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Issues List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        issueAdapter = new IssueAdapter(issueList, this, userType);
        rvIssues.setLayoutManager(new LinearLayoutManager(this));
        rvIssues.setAdapter(issueAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIssues(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortAndFilter() {
        swipeRefresh.setOnRefreshListener(IssueListActivity.this::loadIssuesFromFirebase);
        // Sort button click
        ivSort.setOnClickListener(v -> {
            if (sortOptionsLayout.getVisibility() == View.VISIBLE) {
                sortOptionsLayout.setVisibility(View.GONE);
            } else {
                sortOptionsLayout.setVisibility(View.VISIBLE);
                cardFilter.setVisibility(View.GONE);
            }
        });

        // Filter button click
        ivFilter.setOnClickListener(v -> {
            if (cardFilter.getVisibility() == View.VISIBLE) {
                cardFilter.setVisibility(View.GONE);
            } else {
                cardFilter.setVisibility(View.VISIBLE);
                sortOptionsLayout.setVisibility(View.GONE);
            }
        });

        // Status Filter Chips
        chipAll.setOnClickListener(v -> {
            currentStatusFilter = "all";
            updateStatusChipSelection(chipAll);
            applyFilters();
        });

        chipPending.setOnClickListener(v -> {
            currentStatusFilter = "Pending";
            updateStatusChipSelection(chipPending);
            applyFilters();
        });

        chipInProgress.setOnClickListener(v -> {
            currentStatusFilter = "In Progress";
            updateStatusChipSelection(chipInProgress);
            applyFilters();
        });

        chipResolved.setOnClickListener(v -> {
            currentStatusFilter = "Resolved";
            updateStatusChipSelection(chipResolved);
            applyFilters();
        });

        chipRejected.setOnClickListener(v -> {
            currentStatusFilter = "Rejected";
            updateStatusChipSelection(chipRejected);
            applyFilters();
        });

        // Priority Filter Chips
        chipCritical.setOnClickListener(v -> {
            currentPriorityFilter = "Critical";
            updatePriorityChipSelection(chipCritical);
            applyFilters();
        });

        chipHigh.setOnClickListener(v -> {
            currentPriorityFilter = "High";
            updatePriorityChipSelection(chipHigh);
            applyFilters();
        });

        chipMedium.setOnClickListener(v -> {
            currentPriorityFilter = "Medium";
            updatePriorityChipSelection(chipMedium);
            applyFilters();
        });

        chipLow.setOnClickListener(v -> {
            currentPriorityFilter = "Low";
            updatePriorityChipSelection(chipLow);
            applyFilters();
        });

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> {
            currentStatusFilter = "all";
            currentPriorityFilter = "all";
            updateStatusChipSelection(chipAll);
            updatePriorityChipSelection(null);
            applyFilters();
            cardFilter.setVisibility(View.GONE);
        });

        // Sort options
        setupSortOptions();
    }

    private void setupSortOptions() {
        LinearLayout sortByDateDesc = findViewById(R.id.sortByDateDesc);
        LinearLayout sortByDateAsc = findViewById(R.id.sortByDateAsc);
        LinearLayout sortByPriority = findViewById(R.id.sortByPriority);
        LinearLayout sortByStatus = findViewById(R.id.sortByStatus);

        sortByDateDesc.setOnClickListener(v -> {
            currentSort = "date_desc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByDateAsc.setOnClickListener(v -> {
            currentSort = "date_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByPriority.setOnClickListener(v -> {
            currentSort = "priority_desc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });

        sortByStatus.setOnClickListener(v -> {
            currentSort = "status_asc";
            applySort();
            sortOptionsLayout.setVisibility(View.GONE);
        });
    }

    private void updateStatusChipSelection(Chip selectedChip) {
        chipAll.setChecked(false);
        chipPending.setChecked(false);
        chipInProgress.setChecked(false);
        chipResolved.setChecked(false);
        chipRejected.setChecked(false);
        selectedChip.setChecked(true);
    }

    private void updatePriorityChipSelection(Chip selectedChip) {
        chipCritical.setChecked(false);
        chipHigh.setChecked(false);
        chipMedium.setChecked(false);
        chipLow.setChecked(false);

        if (selectedChip != null) {
            selectedChip.setChecked(true);
        } else {
            currentPriorityFilter = "all";
        }
    }

    private void applyFilters() {
        List<IssueModel> filteredList = new ArrayList<>();

        for (IssueModel issue : issueListFull) {
            boolean statusMatch = false;
            boolean priorityMatch = false;

            // Status filter
            if ("all".equals(currentStatusFilter)) {
                statusMatch = true;
            } else {
                statusMatch = currentStatusFilter.equals(issue.getStatus());
            }

            // Priority filter
            if ("all".equals(currentPriorityFilter)) {
                priorityMatch = true;
            } else {
                priorityMatch = currentPriorityFilter.equals(issue.getPriority());
            }

            if (statusMatch && priorityMatch) {
                filteredList.add(issue);
            }
        }

        issueList.clear();
        issueList.addAll(filteredList);
        applySort();
        updateStatistics();
        updateNoDataVisibility();
    }

    private void filterIssues(String query) {
        issueList.clear();
        if (query.isEmpty()) {
            issueList.addAll(issueListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (IssueModel issue : issueListFull) {
                if (issue.getProblemTitle().toLowerCase().contains(lowerCaseQuery) || issue.getProblemDescription().toLowerCase().contains(lowerCaseQuery) || issue.getLocation().toLowerCase().contains(lowerCaseQuery) || issue.getProblemType().toLowerCase().contains(lowerCaseQuery) || issue.getIssue().toLowerCase().contains(lowerCaseQuery) || issue.getId().toLowerCase().contains(lowerCaseQuery)) {
                    issueList.add(issue);
                }
            }
        }
        applySort();
        updateNoDataVisibility();
    }

    private void applySort() {
        switch (currentSort) {
            case "date_desc":
                issueList.sort((i1, i2) -> Long.compare(i2.getCreatedTimestamp(), i1.getCreatedTimestamp()));
                break;
            case "date_asc":
                issueList.sort((i1, i2) -> Long.compare(i1.getCreatedTimestamp(), i2.getCreatedTimestamp()));
                break;
            case "priority_desc":
                issueList.sort((i1, i2) -> getPriorityValue(i2.getPriority()) - getPriorityValue(i1.getPriority()));
                break;
            case "status_asc":
                issueList.sort((i1, i2) -> i1.getStatus().compareTo(i2.getStatus()));
                break;
        }
        issueAdapter.notifyDataSetChanged();
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "Critical":
                return 4;
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 0;
        }
    }

    private void loadIssuesFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.VISIBLE);
        issueList.clear();
        issueListFull.clear();
        reference = database.getReference().child("Issues");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    swipeRefresh.setRefreshing(false);
                    IssueModel issueModel = snapshot.getValue(IssueModel.class);
                    if (issueModel != null) {
                        issueModel.setId(snapshot.getKey());
                        if (userType.equalsIgnoreCase("SEVASARTHI") && userId.equalsIgnoreCase(issueModel.getCreatedBy())) {
                            issueList.add(issueModel);
                            issueListFull.add(issueModel);
                        } else if (userType.equalsIgnoreCase("WORKER") && issueModel.getAssignedTo() != null && userId.equalsIgnoreCase(issueModel.getAssignedTo())) {
                            issueList.add(issueModel);
                            issueListFull.add(issueModel);
                        } else if (userType.equalsIgnoreCase("OFFICER") && officer.getDepartment().equalsIgnoreCase(issueModel.getProblemType())) {
                            issueList.add(issueModel);
                            issueListFull.add(issueModel);
                        } else if (userType.equalsIgnoreCase("FOUNDER")) {
                            issueList.add(issueModel);
                            issueListFull.add(issueModel);
                        }
                        progressBar.setVisibility(View.GONE);
                        if (!issueList.isEmpty()) {
                            tvNoData.setVisibility(View.GONE);
                            rvIssues.setVisibility(View.VISIBLE);
                            updateStatistics();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(IssueListActivity.this, "Error loading issues: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatistics() {
        int total = issueListFull.size();
        int pending = 0, inProgress = 0, resolved = 0;

        for (IssueModel issue : issueListFull) {
            switch (issue.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "In Progress":
                    inProgress++;
                    break;
                case "Resolved":
                    resolved++;
                    break;
            }
        }

        tvTotalIssues.setText(String.valueOf(total));
        tvPendingCount.setText(String.valueOf(pending));
        tvInProgressCount.setText(String.valueOf(inProgress));
        tvResolvedCount.setText(String.valueOf(resolved));
    }

    private void updateNoDataVisibility() {
        if (issueList.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvIssues.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvIssues.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onIssueClick(IssueModel issue) {
        Intent intent = new Intent(IssueListActivity.this, IssueDetailsActivity.class);
        intent.putExtra("IssueId", issue.getId());
        startActivity(intent);
    }

    @Override
    public void onAssignClick(IssueModel issue) {
        if (userType.equalsIgnoreCase("FOUNDER")) {
            Intent intent = new Intent(IssueListActivity.this, WorkerListActivity.class);
            intent.putExtra("IssueId", issue.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onTrackClick(IssueModel issue) {
        Intent intent = new Intent(IssueListActivity.this, IssueDetailsActivity.class);
        intent.putExtra("IssueId", issue.getId());
        startActivity(intent);
    }

    @Override
    public void onApproveClick(IssueModel issue) {
        Toast.makeText(this, "Already " + issue.getStatus(), Toast.LENGTH_SHORT).show();
    }
}