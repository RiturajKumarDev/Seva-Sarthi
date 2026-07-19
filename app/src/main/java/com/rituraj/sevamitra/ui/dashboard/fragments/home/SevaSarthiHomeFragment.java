package com.rituraj.sevamitra.ui.dashboard.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.listitem.SwipeableListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SevaSarthiHomeFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private TextView tvGreeting, tvOfficerName, tvDesignation, tvDepartment, tvCurrentTime, vtProfileLetter;
    private CardView ivProfile;
    private CardView cardProfile;

    // Statistics Cards
    private TextView tvTotalIssues, tvResolvedIssues, tvPendingIssues, tvInProgressIssues;
    private TextView tvTotalWorkers, tvAvailableWorkers, tvAvgResolutionTime, tvSatisfactionRate;
    private CardView cardTotalIssues, cardResolvedIssues, cardPendingIssues, cardInProgress;
    private CardView cardTotalWorkers, cardAvailableWorkers, cardAvgResolution, cardSatisfaction;

    // Quick Actions
    private CardView cardCreateIssue, cardTrackDailyIssues, cardTrackIssues, cardWorkerList;
    private CardView cardSettings;

    // Floating Action Button
    private FloatingActionButton fabNewIssue;
    private long totalIssues = 0, resolvedIssues = 0, pendingIssues = 0, progressIssues = 0;

    // Data
    private UserData currentUser = new UserData();
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_seva_mitra_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setUserData();
        setupHeader();
        updateCurrentTime();
        loadStatistics();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvOfficerName = view.findViewById(R.id.tvOfficerName);
        tvDesignation = view.findViewById(R.id.tvDesignation);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        ivProfile = view.findViewById(R.id.ivProfile);
        cardProfile = view.findViewById(R.id.cardProfile);

        // Statistics
        tvTotalIssues = view.findViewById(R.id.tvTotalIssues);
        tvResolvedIssues = view.findViewById(R.id.tvResolvedIssues);
        tvPendingIssues = view.findViewById(R.id.tvPendingIssues);
        tvInProgressIssues = view.findViewById(R.id.tvInProgressIssues);
        tvTotalWorkers = view.findViewById(R.id.tvTotalWorkers);
        tvAvailableWorkers = view.findViewById(R.id.tvAvailableWorkers);
        tvAvgResolutionTime = view.findViewById(R.id.tvAvgResolutionTime);
        tvSatisfactionRate = view.findViewById(R.id.tvSatisfactionRate);

        cardTotalIssues = view.findViewById(R.id.cardTotalIssues);
        cardResolvedIssues = view.findViewById(R.id.cardResolvedIssues);
        cardPendingIssues = view.findViewById(R.id.cardPendingIssues);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardTotalWorkers = view.findViewById(R.id.cardTotalWorkers);
        cardAvailableWorkers = view.findViewById(R.id.cardAvailableWorkers);
        cardAvgResolution = view.findViewById(R.id.cardAvgResolution);
        cardSatisfaction = view.findViewById(R.id.cardSatisfaction);

        // Quick Actions
        cardCreateIssue = view.findViewById(R.id.cardCreateIssue);
        cardTrackIssues = view.findViewById(R.id.cardTrackIssues);
        cardTrackDailyIssues = view.findViewById(R.id.cardTrackDailyIssues);
        cardWorkerList = view.findViewById(R.id.cardWorkerList);
        cardSettings = view.findViewById(R.id.cardSettings);

        // FAB
        fabNewIssue = view.findViewById(R.id.fabNewIssue);
    }

    private void setupHeader() {
        // Set greeting based on time
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning");
        } else if (hour < 16) {
            tvGreeting.setText("Good Afternoon");
        } else {
            tvGreeting.setText("Good Evening");
        }
        tvDepartment.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "Public Works Department");
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void loadStatistics() {
        getTotalIssues();
        getTotalWorkers();
        tvAvgResolutionTime.setText("2.5 days");
        tvSatisfactionRate.setText("94%");
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::loadStatistics);
        // Statistics Cards
        cardTotalIssues.setOnClickListener(v -> startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardResolvedIssues.setOnClickListener(v -> startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardPendingIssues.setOnClickListener(v -> startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardInProgress.setOnClickListener(v -> startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardTotalWorkers.setOnClickListener(v -> startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardAvailableWorkers.setOnClickListener(v -> startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        // Quick Actions
        cardCreateIssue.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddIssueActivity.class));
        });

        cardTrackIssues.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), IssueListActivity.class));
        });

        cardTrackDailyIssues.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DailyItemsActivity.class));
        });

        cardWorkerList.setOnClickListener(v -> startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SupportActivity.class)));

        // FAB
        fabNewIssue.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddIssueActivity.class)));
    }

    private void setUserData() {
        tvDesignation.setText(firebaseUser.getPhotoUrl() != null ? currentUser.getProfileUrl() : "Other");
        tvOfficerName.setText(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Unknow");
        setAvatarColor(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Unknow");
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

    private void getTotalIssues() {
        totalIssues = pendingIssues = progressIssues = resolvedIssues = 0;
        DatabaseReference myRef = database.getReference().child("Issues");
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    IssueModel issueModel = snapshot.getValue(IssueModel.class);
                    if (issueModel != null && issueModel.getCreatedBy().equalsIgnoreCase(firebaseUser.getUid())) {
                        totalIssues++;
                        if (issueModel.getStatus().equalsIgnoreCase("Resolved")) resolvedIssues++;
                        else if (issueModel.getStatus().equalsIgnoreCase("Pending"))
                            pendingIssues++;
                        else if (issueModel.getStatus().equalsIgnoreCase("In Progress"))
                            progressIssues++;
                    }
                    updateIssuesUi();
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
            }
        });
    }

    private void updateIssuesUi() {
        tvTotalIssues.setText(String.valueOf(totalIssues));
        tvResolvedIssues.setText(String.valueOf(resolvedIssues));
        tvPendingIssues.setText(String.valueOf(pendingIssues));
        tvInProgressIssues.setText(String.valueOf(progressIssues));
    }

    private void getTotalWorkers() {
        reference = database.getReference().child("UserData").child("WORKER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long total = snapshot.getChildrenCount();
                    tvTotalWorkers.setText(String.valueOf(total));
                    long min = Math.max(0, total - 10);
                    long active = total > 0 ? new Random().nextInt((int) (total - min + 1)) + min : 0;
                    tvAvailableWorkers.setText(String.valueOf(active));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}