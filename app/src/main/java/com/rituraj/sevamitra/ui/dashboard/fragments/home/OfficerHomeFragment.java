package com.rituraj.sevamitra.ui.dashboard.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OfficerHomeFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private TextView tvGreeting, tvOfficerName, tvDesignation, tvDepartment, tvCurrentTime;
    private ImageView ivProfile;
    private CardView cardProfile;

    // Statistics Cards
    private TextView tvTotalIssues, tvResolvedIssues, tvPendingIssues, tvInProgressIssues;
    private TextView tvTotalWorkers, tvAvailableWorkers, tvAvgResolutionTime, tvSatisfactionRate;
    private CardView cardTotalIssues, cardResolvedIssues, cardPendingIssues, cardInProgress;
    private CardView cardTotalWorkers, cardAvailableWorkers, cardAvgResolution, cardSatisfaction;

    // Quick Actions
    private CardView cardCreateIssue, cardAssignWorker, cardTrackIssues, cardWorkerList;
    private CardView cardGenerateReport, cardBills, cardWorkerPerformance, cardSettings;

    // Recent Issues Section
    private RecyclerView rvRecentIssues;
    private LinearLayout viewAllIssues;

    // Charts
    private BarChart barChartIssues;
    private LinearLayout weeklyStatsChart;

    // Floating Action Button
    private FloatingActionButton fabNewIssue;

    // Data
    private UserData currentUser = new UserData();
    private List<IssueModel> recentIssues;
    private IssueAdapter issueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_officer_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setUserData();
        setupHeader();
        updateCurrentTime();
        loadStatistics();
        setupBarChart();
        loadRecentIssues();
        setupWeeklyStats();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
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
        cardAssignWorker = view.findViewById(R.id.cardAssignWorker);
        cardTrackIssues = view.findViewById(R.id.cardTrackIssues);
        cardWorkerList = view.findViewById(R.id.cardWorkerList);
        cardGenerateReport = view.findViewById(R.id.cardGenerateReport);
        cardBills = view.findViewById(R.id.cardBills);
        cardWorkerPerformance = view.findViewById(R.id.cardWorkerPerformance);
        cardSettings = view.findViewById(R.id.cardSettings);

        // Recent Issues
        rvRecentIssues = view.findViewById(R.id.rvRecentIssues);
        viewAllIssues = view.findViewById(R.id.viewAllIssues);

        // Charts
        barChartIssues = view.findViewById(R.id.barChartIssues);
        weeklyStatsChart = view.findViewById(R.id.weeklyStatsChart);

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
        tvDesignation.setText(currentUser.getDesignation() != null ? currentUser.getDesignation() : "Development Officer");
        tvDepartment.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "Public Works Department");
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void loadStatistics() {
        // Sample data - Replace with actual API calls
        tvTotalIssues.setText("156");
        tvResolvedIssues.setText("128");
        tvPendingIssues.setText("18");
        tvInProgressIssues.setText("10");
        tvTotalWorkers.setText("45");
        tvAvailableWorkers.setText("32");
        tvAvgResolutionTime.setText("2.5 days");
        tvSatisfactionRate.setText("94%");

        // Update progress
        updateIssueProgress(128, 156);
    }

    private void updateIssueProgress(int resolved, int total) {
        int percentage = (resolved * 100) / total;
        // Update progress indicator if needed
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 25));
        entries.add(new BarEntry(1, 32));
        entries.add(new BarEntry(2, 28));
        entries.add(new BarEntry(3, 35));
        entries.add(new BarEntry(4, 42));
        entries.add(new BarEntry(5, 38));
        entries.add(new BarEntry(6, 45));

        BarDataSet dataSet = new BarDataSet(entries, "Issues Created");
        dataSet.setColor(getResources().getColor(R.color.logo_gold));
        dataSet.setValueTextColor(getResources().getColor(R.color.logo_white));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartIssues.setData(barData);

        // Customize chart
        barChartIssues.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        barChartIssues.setDescription(null);
        barChartIssues.setDrawGridBackground(false);
        barChartIssues.setDrawBorders(false);
        barChartIssues.setPinchZoom(false);
        barChartIssues.setScaleEnabled(false);
        barChartIssues.getAxisLeft().setTextColor(getResources().getColor(R.color.logo_white));
        barChartIssues.getAxisRight().setTextColor(getResources().getColor(R.color.logo_white));
        barChartIssues.getXAxis().setTextColor(getResources().getColor(R.color.logo_white));
        barChartIssues.getLegend().setTextColor(getResources().getColor(R.color.logo_white));

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        barChartIssues.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        barChartIssues.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

        barChartIssues.animateY(1000);
        barChartIssues.invalidate();
    }

    private void loadRecentIssues() {
        recentIssues = getSampleIssues();

        issueAdapter = new IssueAdapter(recentIssues, new IssueAdapter.OnIssueClickListener() {
            @Override
            public void onIssueClick(IssueModel issue) {
                showIssueDetails(issue);
            }

            @Override
            public void onAssignClick(IssueModel issue) {
                Toast.makeText(getContext(), "Assign worker to: " + issue.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTrackClick(IssueModel issue) {
                Toast.makeText(getContext(), "Track issue: " + issue.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        rvRecentIssues.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentIssues.setAdapter(issueAdapter);
    }

    private List<IssueModel> getSampleIssues() {
        List<IssueModel> issues = new ArrayList<>();

        IssueModel issue1 = new IssueModel();
        issue1.setId("ISS001");
        issue1.setTitle("AC Not Working - Government Office");
        issue1.setDescription("AC not cooling properly in room no. 105");
        issue1.setLocation("Collectorate, Room 105");
        issue1.setReporterName("Mr. Sharma");
        issue1.setReporterPhone("9876543210");
        issue1.setDate("2024-01-15");
        issue1.setStatus("In Progress");
        issue1.setPriority("High");
        issue1.setAssignedWorker("Ramesh Kumar (AC Technician)");
        issue1.setCategory("AC Repair");
        issues.add(issue1);

        IssueModel issue2 = new IssueModel();
        issue2.setId("ISS002");
        issue2.setTitle("Plumbing Issue - District Hospital");
        issue2.setDescription("Water leakage in washroom");
        issue2.setLocation("District Hospital, Ground Floor");
        issue2.setReporterName("Dr. Gupta");
        issue2.setReporterPhone("9876543211");
        issue2.setDate("2024-01-14");
        issue2.setStatus("Pending");
        issue2.setPriority("Critical");
        issue2.setAssignedWorker("Not Assigned");
        issue2.setCategory("Plumbing");
        issues.add(issue2);

        IssueModel issue3 = new IssueModel();
        issue3.setId("ISS003");
        issue3.setTitle("CCTV Camera Not Working");
        issue3.setDescription("Main gate CCTV camera malfunctioning");
        issue3.setLocation("Main Gate, Collectorate");
        issue3.setReporterName("Mr. Singh");
        issue3.setReporterPhone("9876543212");
        issue3.setDate("2024-01-13");
        issue3.setStatus("Resolved");
        issue3.setPriority("Medium");
        issue3.setAssignedWorker("Vikash Singh (CCTV Technician)");
        issue3.setCategory("CCTV");
        issues.add(issue3);

        IssueModel issue4 = new IssueModel();
        issue4.setId("ISS004");
        issue4.setTitle("Electrical Fault - Office Building");
        issue4.setDescription("Power fluctuation in entire building");
        issue4.setLocation("Main Building, 2nd Floor");
        issue4.setReporterName("Mrs. Verma");
        issue4.setReporterPhone("9876543213");
        issue4.setDate("2024-01-12");
        issue4.setStatus("In Progress");
        issue4.setPriority("High");
        issue4.setAssignedWorker("Suresh Sharma (Electrician)");
        issue4.setCategory("Electrical");
        issues.add(issue4);

        return issues;
    }

    private void setupWeeklyStats() {
        // Create bar chart for weekly issues
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int[] created = {25, 32, 28, 35, 42, 38};
        int[] resolved = {22, 28, 25, 30, 38, 35};

        weeklyStatsChart.removeAllViews();

        for (int i = 0; i < days.length; i++) {
            View statView = getLayoutInflater().inflate(R.layout.item_weekly_stat, null);
            TextView tvDay = statView.findViewById(R.id.tvDay);
            TextView tvCreated = statView.findViewById(R.id.tvCreated);
            TextView tvResolved = statView.findViewById(R.id.tvResolved);
            View barCreated = statView.findViewById(R.id.barCreated);
            View barResolved = statView.findViewById(R.id.barResolved);

            tvDay.setText(days[i]);
            tvCreated.setText("" + created[i]);
            tvResolved.setText("" + resolved[i]);

            // Set bar heights (max height 100dp)
            int maxValue = 50;
            int createdHeight = (created[i] * 100) / maxValue;
            int resolvedHeight = (resolved[i] * 100) / maxValue;

            barCreated.getLayoutParams().height = createdHeight;
            barResolved.getLayoutParams().height = resolvedHeight;

            weeklyStatsChart.addView(statView);
        }
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalIssues.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all issues", Toast.LENGTH_SHORT).show());

        cardResolvedIssues.setOnClickListener(v ->
                Toast.makeText(getContext(), "View resolved issues", Toast.LENGTH_SHORT).show());

        cardPendingIssues.setOnClickListener(v ->
                Toast.makeText(getContext(), "View pending issues", Toast.LENGTH_SHORT).show());

        cardInProgress.setOnClickListener(v ->
                Toast.makeText(getContext(), "View in-progress issues", Toast.LENGTH_SHORT).show());

        cardTotalWorkers.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all workers", Toast.LENGTH_SHORT).show());

        cardAvailableWorkers.setOnClickListener(v ->
                Toast.makeText(getContext(), "View available workers", Toast.LENGTH_SHORT).show());

        cardAvgResolution.setOnClickListener(v ->
                Toast.makeText(getContext(), "View resolution time analytics", Toast.LENGTH_SHORT).show());

        cardSatisfaction.setOnClickListener(v ->
                Toast.makeText(getContext(), "View satisfaction survey", Toast.LENGTH_SHORT).show());

        // Quick Actions
        cardCreateIssue.setOnClickListener(v ->
                Toast.makeText(getContext(), "Create new issue", Toast.LENGTH_SHORT).show());

        cardAssignWorker.setOnClickListener(v ->
                Toast.makeText(getContext(), "Assign worker to issue", Toast.LENGTH_SHORT).show());

        cardTrackIssues.setOnClickListener(v ->
                Toast.makeText(getContext(), "Track all issues", Toast.LENGTH_SHORT).show());

        cardWorkerList.setOnClickListener(v ->
                Toast.makeText(getContext(), "View workers list", Toast.LENGTH_SHORT).show());

        cardGenerateReport.setOnClickListener(v ->
                Toast.makeText(getContext(), "Generate monthly report", Toast.LENGTH_SHORT).show());

        cardBills.setOnClickListener(v ->
                Toast.makeText(getContext(), "Generate bills", Toast.LENGTH_SHORT).show());

        cardWorkerPerformance.setOnClickListener(v ->
                Toast.makeText(getContext(), "View worker performance", Toast.LENGTH_SHORT).show());

        cardSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show());

        // View All Issues
        viewAllIssues.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all issues", Toast.LENGTH_SHORT).show());

        // FAB
        fabNewIssue.setOnClickListener(v ->
                Toast.makeText(getContext(), "Create new issue", Toast.LENGTH_SHORT).show());
    }

    private void showIssueDetails(IssueModel issue) {
        Toast.makeText(getContext(), "Issue: " + issue.getTitle(), Toast.LENGTH_SHORT).show();
        // Navigate to issue details fragment
    }

    private void setUserData() {
        tvOfficerName.setText(firebaseUser.getDisplayName());
        Glide.with(requireActivity())
                .load(firebaseUser.getPhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile);
    }

    // Issue Model Class
    public static class IssueModel {
        private String id;
        private String title;
        private String description;
        private String location;
        private String reporterName;
        private String reporterPhone;
        private String date;
        private String status;
        private String priority;
        private String assignedWorker;
        private String category;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getReporterName() {
            return reporterName;
        }

        public void setReporterName(String reporterName) {
            this.reporterName = reporterName;
        }

        public String getReporterPhone() {
            return reporterPhone;
        }

        public void setReporterPhone(String reporterPhone) {
            this.reporterPhone = reporterPhone;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getAssignedWorker() {
            return assignedWorker;
        }

        public void setAssignedWorker(String assignedWorker) {
            this.assignedWorker = assignedWorker;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    // Issue Adapter
    public static class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

        private List<IssueModel> issueList;
        private OnIssueClickListener listener;

        public interface OnIssueClickListener {
            void onIssueClick(IssueModel issue);

            void onAssignClick(IssueModel issue);

            void onTrackClick(IssueModel issue);
        }

        public IssueAdapter(List<IssueModel> issueList, OnIssueClickListener listener) {
            this.issueList = issueList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_issue, parent, false);
            return new IssueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
            IssueModel issue = issueList.get(position);
            holder.bind(issue);
        }

        @Override
        public int getItemCount() {
            return issueList.size();
        }

        class IssueViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle, tvId, tvDate, tvLocation, tvStatus, tvPriority, tvAssignedWorker;
            private ImageView ivPriority;
            private CardView cardAssign, cardTrack;

            public IssueViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvId = itemView.findViewById(R.id.tvId);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPriority = itemView.findViewById(R.id.tvPriority);
                tvAssignedWorker = itemView.findViewById(R.id.tvAssignedWorker);
                ivPriority = itemView.findViewById(R.id.ivPriority);
                cardAssign = itemView.findViewById(R.id.cardAssign);
                cardTrack = itemView.findViewById(R.id.cardTrack);
            }

            public void bind(IssueModel issue) {
                tvTitle.setText(issue.getTitle());
                tvId.setText("ID: " + issue.getId());
                tvDate.setText(issue.getDate());
                tvLocation.setText(issue.getLocation());
                tvStatus.setText(issue.getStatus());
                tvPriority.setText(issue.getPriority());
                tvAssignedWorker.setText("Assigned: " + issue.getAssignedWorker());

                // Set priority color
                switch (issue.getPriority()) {
                    case "Critical":
                        tvPriority.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                        ivPriority.setColorFilter(itemView.getContext().getColor(R.color.logo_orange));
                        break;
                    case "High":
                        tvPriority.setTextColor(itemView.getContext().getColor(R.color.logo_gold));
                        ivPriority.setColorFilter(itemView.getContext().getColor(R.color.logo_gold));
                        break;
                    default:
                        tvPriority.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                        ivPriority.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
                        break;
                }

                // Set status color
                switch (issue.getStatus()) {
                    case "Resolved":
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                        break;
                    case "In Progress":
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_gold));
                        break;
                    default:
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                        break;
                }

                cardAssign.setOnClickListener(v -> listener.onAssignClick(issue));
                cardTrack.setOnClickListener(v -> listener.onTrackClick(issue));
                itemView.setOnClickListener(v -> listener.onIssueClick(issue));
            }
        }
    }
}