package com.rituraj.sevamitra.ui.dashboard.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.rituraj.sevamitra.models.User;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class OfficerHomeFragment extends Fragment {

    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Header Views
    private TextView tvGreeting, tvSDMName, tvDesignation, tvCurrentTime;
    private CardView ivProfile;
    private CardView cardProfile;

    // Statistics Cards
    private TextView tvTotalComplaints, tvResolvedComplaints, tvPendingComplaints, tvInProgressComplaints;
    private TextView tvTotalWorkers, tvAvgResponseTime;
    private CardView cardTotalComplaints, cardResolvedComplaints, cardPendingComplaints, cardInProgress;
    private CardView cardTotalWorkersCard, cardAvgResponse;

    // Jurisdiction Cards
    private TextView tvDistrictName, tvDivisionName, vtProfileLetter;

    // Quick Actions
    private CardView cardViewComplaints, cardTrackComplaints, cardOfficerReports;
    private CardView cardWorkerVerification, cardEmergencyAlerts, cardAnnouncements;
    private FloatingActionButton fabNewIssue;

    // Charts
    private PieChart pieChartComplaints;

    // Data
    private UserData currentUser = new UserData();
    private long totalIssues = 0, resolvedIssues = 0, pendingIssues = 0, progressIssues = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_officer_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setupHeader();
        updateCurrentTime();
        getUserData();
        setUserData();
        getTotalWorkers();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvSDMName = view.findViewById(R.id.tvSDMName);
        tvDesignation = view.findViewById(R.id.tvDesignation);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        ivProfile = view.findViewById(R.id.ivProfile);
        cardProfile = view.findViewById(R.id.cardProfile);

        // Statistics
        tvTotalComplaints = view.findViewById(R.id.tvTotalComplaints);
        tvResolvedComplaints = view.findViewById(R.id.tvResolvedComplaints);
        tvPendingComplaints = view.findViewById(R.id.tvPendingComplaints);
        tvInProgressComplaints = view.findViewById(R.id.tvInProgressComplaints);
        tvTotalWorkers = view.findViewById(R.id.tvTotalWorkers);
        tvAvgResponseTime = view.findViewById(R.id.tvAvgResponseTime);

        cardTotalComplaints = view.findViewById(R.id.cardTotalComplaints);
        cardResolvedComplaints = view.findViewById(R.id.cardResolvedComplaints);
        cardPendingComplaints = view.findViewById(R.id.cardPendingComplaints);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardTotalWorkersCard = view.findViewById(R.id.cardTotalWorkersCard);
        cardAvgResponse = view.findViewById(R.id.cardAvgResponse);

        // Jurisdiction
        tvDistrictName = view.findViewById(R.id.tvDistrictName);
        tvDivisionName = view.findViewById(R.id.tvDivisionName);

        // Quick Actions
        cardViewComplaints = view.findViewById(R.id.cardViewComplaints);
        cardTrackComplaints = view.findViewById(R.id.cardTrackComplaints);
        cardOfficerReports = view.findViewById(R.id.cardOfficerReports);
        cardWorkerVerification = view.findViewById(R.id.cardWorkerVerification);
        cardEmergencyAlerts = view.findViewById(R.id.cardEmergencyAlerts);
        cardAnnouncements = view.findViewById(R.id.cardAnnouncements);
        fabNewIssue = view.findViewById(R.id.fabNewIssue);

        // Charts
        pieChartComplaints = view.findViewById(R.id.pieChartComplaints);
    }

    private void setupHeader() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning");
        } else if (hour < 16) {
            tvGreeting.setText("Good Afternoon");
        } else {
            tvGreeting.setText("Good Evening");
        }
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardResolvedComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardPendingComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardInProgress.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardTotalWorkersCard.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardAvgResponse.setOnClickListener(v ->
                Toast.makeText(getContext(), "View response time analytics", Toast.LENGTH_SHORT).show());

        // Quick Actions
        cardViewComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardTrackComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardOfficerReports.setOnClickListener(v ->
                Toast.makeText(getContext(), "View officer performance reports", Toast.LENGTH_SHORT).show());

        cardWorkerVerification.setOnClickListener(v ->
                Toast.makeText(getContext(), "Verify worker credentials", Toast.LENGTH_SHORT).show());

        cardEmergencyAlerts.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SupportActivity.class)));

        cardAnnouncements.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        fabNewIssue.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddIssueActivity.class)));
    }

    private void setUserData() {
        tvSDMName.setText(firebaseUser.getDisplayName());
        setAvatarColor(firebaseUser.getDisplayName());
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

    private void getUserData() {
        reference = database.getReference().child("UserData").child("OFFICER").child(firebaseUser.getUid());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData officer = snapshot.getValue(UserData.class);
                    if (officer != null) {
                        setOfficerData(officer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setOfficerData(UserData officer) {
        tvDistrictName.setText(officer.getDistrict() != null ? officer.getDistrict() : "NA");
        tvDivisionName.setText(officer.getDivision() != null ? officer.getDivision() : "NA");
        tvDesignation.setText(officer.getDepartment() != null ? officer.getDepartment() : "NA");
        getTotalIssues(officer);
    }

    private void getTotalIssues(UserData officer) {
        totalIssues = pendingIssues = progressIssues = resolvedIssues = 0;
        DatabaseReference myRef = database.getReference().child("Issues");
        myRef.keepSynced(true);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    IssueModel issueModel = snapshot.getValue(IssueModel.class);
                    if (issueModel != null && issueModel.getIssue().equalsIgnoreCase("SEVASARTHI")
                            && issueModel.getProblemType().equalsIgnoreCase(officer.getDepartment())) {
                        totalIssues++;
                        if (issueModel.getStatus().equalsIgnoreCase("Resolved"))
                            resolvedIssues++;
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

    private void getTotalWorkers() {
        reference = database.getReference().child("UserData").child("WORKER");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long total = snapshot.getChildrenCount();
                    tvTotalWorkers.setText(String.valueOf(total));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateIssuesUi() {
        // Sample data - Replace with actual API calls
        tvTotalComplaints.setText(String.valueOf(totalIssues));
        tvResolvedComplaints.setText(String.valueOf(resolvedIssues));
        tvPendingComplaints.setText(String.valueOf(pendingIssues));
        tvInProgressComplaints.setText(String.valueOf(progressIssues));
        tvAvgResponseTime.setText("2.5 hrs");
        setupPieChart();
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(resolvedIssues, "Resolved"));
        entries.add(new PieEntry(pendingIssues, "Pending"));
        entries.add(new PieEntry(progressIssues, "In Progress"));

        PieDataSet dataSet = new PieDataSet(entries, "Complaints Status");
        dataSet.setColors(new int[]{
                getResources().getColor(R.color.logo_green),
                getResources().getColor(R.color.logo_orange),
                getResources().getColor(R.color.logo_gold)
        });
        dataSet.setValueTextColor(getResources().getColor(R.color.logo_white));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChartComplaints));

        PieData pieData = new PieData(dataSet);
        pieChartComplaints.setData(pieData);

        // Customize chart
        pieChartComplaints.setUsePercentValues(true);
        pieChartComplaints.getDescription().setEnabled(false);
        pieChartComplaints.setDrawHoleEnabled(true);
        pieChartComplaints.setHoleColor(getResources().getColor(android.R.color.transparent));
        pieChartComplaints.getLegend().setTextColor(getResources().getColor(R.color.logo_white));
        pieChartComplaints.setTransparentCircleRadius(55f);
        pieChartComplaints.setHoleRadius(45f);
        pieChartComplaints.setDrawEntryLabels(false);
        pieChartComplaints.setCenterTextColor(getResources().getColor(R.color.logo_white));
        pieChartComplaints.animateY(1000);
        pieChartComplaints.invalidate();
    }

}