package com.rituraj.sevamitra.ui.dashboard.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
    private TextView tvTotalOfficers, tvActiveOfficers, tvTotalWorkers, tvAvgResponseTime;
    private CardView cardTotalComplaints, cardResolvedComplaints, cardPendingComplaints, cardInProgress;
    private CardView cardTotalOfficers, cardActiveOfficers, cardTotalWorkersCard, cardAvgResponse;

    // Jurisdiction Cards
    private TextView tvDistrictName, tvDivisionName, tvTotalVillages, tvTotalCities, vtProfileLetter;

    // Quick Actions
    private CardView cardViewComplaints, cardAssignOfficer, cardTrackComplaints, cardOfficerReports;
    private CardView cardWorkerVerification, cardRevenueReports, cardEmergencyAlerts, cardAnnouncements;

    // Charts
    private PieChart pieChartComplaints;

    // Data
    private UserData currentUser = new UserData();

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
        setUserData();
        loadStatistics();
        loadJurisdictionInfo();
        setupPieChart();
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
        tvTotalOfficers = view.findViewById(R.id.tvTotalOfficers);
        tvActiveOfficers = view.findViewById(R.id.tvActiveOfficers);
        tvTotalWorkers = view.findViewById(R.id.tvTotalWorkers);
        tvAvgResponseTime = view.findViewById(R.id.tvAvgResponseTime);

        cardTotalComplaints = view.findViewById(R.id.cardTotalComplaints);
        cardResolvedComplaints = view.findViewById(R.id.cardResolvedComplaints);
        cardPendingComplaints = view.findViewById(R.id.cardPendingComplaints);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardTotalOfficers = view.findViewById(R.id.cardTotalOfficers);
        cardActiveOfficers = view.findViewById(R.id.cardActiveOfficers);
        cardTotalWorkersCard = view.findViewById(R.id.cardTotalWorkersCard);
        cardAvgResponse = view.findViewById(R.id.cardAvgResponse);

        // Jurisdiction
        tvDistrictName = view.findViewById(R.id.tvDistrictName);
        tvDivisionName = view.findViewById(R.id.tvDivisionName);
        tvTotalVillages = view.findViewById(R.id.tvTotalVillages);
        tvTotalCities = view.findViewById(R.id.tvTotalCities);

        // Quick Actions
        cardViewComplaints = view.findViewById(R.id.cardViewComplaints);
        cardAssignOfficer = view.findViewById(R.id.cardAssignOfficer);
        cardTrackComplaints = view.findViewById(R.id.cardTrackComplaints);
        cardOfficerReports = view.findViewById(R.id.cardOfficerReports);
        cardWorkerVerification = view.findViewById(R.id.cardWorkerVerification);
        cardRevenueReports = view.findViewById(R.id.cardRevenueReports);
        cardEmergencyAlerts = view.findViewById(R.id.cardEmergencyAlerts);
        cardAnnouncements = view.findViewById(R.id.cardAnnouncements);

        // Charts
        pieChartComplaints = view.findViewById(R.id.pieChartComplaints);
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
        tvDesignation.setText("Sub-Divisional Magistrate");
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void loadStatistics() {
        // Sample data - Replace with actual API calls
        tvTotalComplaints.setText("1,284");
        tvResolvedComplaints.setText("1,156");
        tvPendingComplaints.setText("98");
        tvInProgressComplaints.setText("30");
        tvTotalOfficers.setText("24");
        tvActiveOfficers.setText("20");
        tvTotalWorkers.setText("156");
        tvAvgResponseTime.setText("2.5 hrs");

        // Update progress
        updateComplaintProgress(1156, 1284);
    }

    private void updateComplaintProgress(int resolved, int total) {
        int percentage = (resolved * 100) / total;
        // Update progress indicator if needed
    }

    private void loadJurisdictionInfo() {
        tvDistrictName.setText(currentUser.getDistrict() != null ? currentUser.getDistrict() : "Lucknow");
        tvDivisionName.setText(currentUser.getDivision() != null ? currentUser.getDivision() : "Lucknow Division");
        tvTotalVillages.setText("456");
        tvTotalCities.setText("12");
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(1156, "Resolved"));
        entries.add(new PieEntry(98, "Pending"));
        entries.add(new PieEntry(30, "In Progress"));

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
        pieChartComplaints.setTransparentCircleRadius(55f);
        pieChartComplaints.setHoleRadius(45f);
        pieChartComplaints.setDrawEntryLabels(false);
        pieChartComplaints.setCenterTextColor(getResources().getColor(R.color.logo_white));
        pieChartComplaints.animateY(1000);
        pieChartComplaints.invalidate();
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardResolvedComplaints.setOnClickListener(v ->
                Toast.makeText(getContext(), "View resolved complaints", Toast.LENGTH_SHORT).show());

        cardPendingComplaints.setOnClickListener(v ->
                Toast.makeText(getContext(), "View pending complaints", Toast.LENGTH_SHORT).show());

        cardInProgress.setOnClickListener(v ->
                Toast.makeText(getContext(), "View in-progress complaints", Toast.LENGTH_SHORT).show());

        cardTotalOfficers.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all officers", Toast.LENGTH_SHORT).show());

        cardActiveOfficers.setOnClickListener(v ->
                Toast.makeText(getContext(), "View active officers", Toast.LENGTH_SHORT).show());

        cardTotalWorkersCard.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all registered workers", Toast.LENGTH_SHORT).show());

        cardAvgResponse.setOnClickListener(v ->
                Toast.makeText(getContext(), "View response time analytics", Toast.LENGTH_SHORT).show());

        // Quick Actions
        cardViewComplaints.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardAssignOfficer.setOnClickListener(v ->
                Toast.makeText(getContext(), "Assign complaint to officer", Toast.LENGTH_SHORT).show());

        cardTrackComplaints.setOnClickListener(v ->
                Toast.makeText(getContext(), "Track complaints status", Toast.LENGTH_SHORT).show());

        cardOfficerReports.setOnClickListener(v ->
                Toast.makeText(getContext(), "View officer performance reports", Toast.LENGTH_SHORT).show());

        cardWorkerVerification.setOnClickListener(v ->
                Toast.makeText(getContext(), "Verify worker credentials", Toast.LENGTH_SHORT).show());

        cardRevenueReports.setOnClickListener(v ->
                Toast.makeText(getContext(), "View revenue reports", Toast.LENGTH_SHORT).show());

        cardEmergencyAlerts.setOnClickListener(v ->
                Toast.makeText(getContext(), "Send emergency alerts", Toast.LENGTH_SHORT).show());

        cardAnnouncements.setOnClickListener(v ->
                Toast.makeText(getContext(), "Make public announcements", Toast.LENGTH_SHORT).show());
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
}