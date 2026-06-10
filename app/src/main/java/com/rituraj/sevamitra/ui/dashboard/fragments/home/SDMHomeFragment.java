package com.rituraj.sevamitra.ui.dashboard.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SDMHomeFragment extends Fragment {

    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Header Views
    private TextView tvGreeting, tvSDMName, tvDesignation, tvCurrentTime;
    private ImageView ivProfile;
    private CardView cardProfile;

    // Statistics Cards
    private TextView tvTotalComplaints, tvResolvedComplaints, tvPendingComplaints, tvInProgressComplaints;
    private TextView tvTotalOfficers, tvActiveOfficers, tvTotalWorkers, tvAvgResponseTime;
    private CardView cardTotalComplaints, cardResolvedComplaints, cardPendingComplaints, cardInProgress;
    private CardView cardTotalOfficers, cardActiveOfficers, cardTotalWorkersCard, cardAvgResponse;

    // Jurisdiction Cards
    private TextView tvDistrictName, tvDivisionName, tvTotalVillages, tvTotalCities;

    // Quick Actions
    private CardView cardViewComplaints, cardAssignOfficer, cardTrackComplaints, cardOfficerReports;
    private CardView cardWorkerVerification, cardRevenueReports, cardEmergencyAlerts, cardAnnouncements;

    // Recent Complaints Section
    private RecyclerView rvRecentComplaints;
    private LinearLayout viewAllComplaints;

    // Charts
    private PieChart pieChartComplaints;

    // Data
    private UserData currentUser = new UserData();
    private List<ComplaintModel> recentComplaints;
    private ComplaintAdapter complaintAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_s_d_m_home, container, false);

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
        loadRecentComplaints();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
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

        // Recent Complaints
        rvRecentComplaints = view.findViewById(R.id.rvRecentComplaints);
        viewAllComplaints = view.findViewById(R.id.viewAllComplaints);

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

    private void loadRecentComplaints() {
        recentComplaints = getSampleComplaints();

        complaintAdapter = new ComplaintAdapter(recentComplaints, new ComplaintAdapter.OnComplaintClickListener() {
            @Override
            public void onComplaintClick(ComplaintModel complaint) {
                showComplaintDetails(complaint);
            }

            @Override
            public void onAssignClick(ComplaintModel complaint) {
                Toast.makeText(getContext(), "Assign complaint to officer", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTrackClick(ComplaintModel complaint) {
                Toast.makeText(getContext(), "Track complaint: " + complaint.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        rvRecentComplaints.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentComplaints.setAdapter(complaintAdapter);
    }

    private List<ComplaintModel> getSampleComplaints() {
        List<ComplaintModel> complaints = new ArrayList<>();

        ComplaintModel complaint1 = new ComplaintModel();
        complaint1.setId("CMP001");
        complaint1.setTitle("AC Not Working in Government Office");
        complaint1.setDescription("AC not working in collectorate office room no. 105");
        complaint1.setLocation("Collectorate, Lucknow");
        complaint1.setComplainantName("Mr. Sharma");
        complaint1.setComplainantPhone("9876543210");
        complaint1.setDate("2024-01-15");
        complaint1.setStatus("In Progress");
        complaint1.setPriority("High");
        complaint1.setAssignedTo("Officer Verma");
        complaints.add(complaint1);

        ComplaintModel complaint2 = new ComplaintModel();
        complaint2.setId("CMP002");
        complaint2.setTitle("Plumbing Issue in Hospital");
        complaint2.setDescription("Water leakage in district hospital");
        complaint2.setLocation("District Hospital, Lucknow");
        complaint2.setComplainantName("Dr. Gupta");
        complaint2.setComplainantPhone("9876543211");
        complaint2.setDate("2024-01-14");
        complaint2.setStatus("Pending");
        complaint2.setPriority("Critical");
        complaint2.setAssignedTo("Not Assigned");
        complaints.add(complaint2);

        ComplaintModel complaint3 = new ComplaintModel();
        complaint3.setId("CMP003");
        complaint3.setTitle("CCTV Camera Not Working");
        complaint3.setDescription("CCTV cameras at main chowk not functioning");
        complaint3.setLocation("Hazratganj, Lucknow");
        complaint3.setComplainantName("Mr. Singh");
        complaint3.setComplainantPhone("9876543212");
        complaint3.setDate("2024-01-13");
        complaint3.setStatus("Resolved");
        complaint3.setPriority("Medium");
        complaint3.setAssignedTo("Officer Kumar");
        complaints.add(complaint3);

        return complaints;
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalComplaints.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all complaints", Toast.LENGTH_SHORT).show());

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
                Toast.makeText(getContext(), "View all complaints", Toast.LENGTH_SHORT).show());

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

        // View All Complaints
        viewAllComplaints.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all complaints", Toast.LENGTH_SHORT).show());
    }

    private void showComplaintDetails(ComplaintModel complaint) {
        Toast.makeText(getContext(), "Complaint: " + complaint.getTitle(), Toast.LENGTH_SHORT).show();
        // Navigate to complaint details fragment
    }

    private void setUserData() {
        tvSDMName.setText(firebaseUser.getDisplayName());
        Glide.with(requireActivity())
                .load(firebaseUser.getPhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile);
    }

    // Complaint Model Class
    public static class ComplaintModel {
        private String id;
        private String title;
        private String description;
        private String location;
        private String complainantName;
        private String complainantPhone;
        private String date;
        private String status;
        private String priority;
        private String assignedTo;

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

        public String getComplainantName() {
            return complainantName;
        }

        public void setComplainantName(String complainantName) {
            this.complainantName = complainantName;
        }

        public String getComplainantPhone() {
            return complainantPhone;
        }

        public void setComplainantPhone(String complainantPhone) {
            this.complainantPhone = complainantPhone;
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

        public String getAssignedTo() {
            return assignedTo;
        }

        public void setAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
        }
    }

    // Complaint Adapter
    public static class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

        private List<ComplaintModel> complaintList;
        private OnComplaintClickListener listener;

        public interface OnComplaintClickListener {
            void onComplaintClick(ComplaintModel complaint);

            void onAssignClick(ComplaintModel complaint);

            void onTrackClick(ComplaintModel complaint);
        }

        public ComplaintAdapter(List<ComplaintModel> complaintList, OnComplaintClickListener listener) {
            this.complaintList = complaintList;
            this.listener = listener;
        }

        @Override
        public ComplaintViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_complaint, parent, false);
            return new ComplaintViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ComplaintViewHolder holder, int position) {
            ComplaintModel complaint = complaintList.get(position);
            holder.bind(complaint);
        }

        @Override
        public int getItemCount() {
            return complaintList.size();
        }

        class ComplaintViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle, tvId, tvDate, tvLocation, tvStatus, tvPriority;
            private ImageView ivPriority;
            private CardView cardAssign, cardTrack;

            public ComplaintViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvId = itemView.findViewById(R.id.tvId);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPriority = itemView.findViewById(R.id.tvPriority);
                ivPriority = itemView.findViewById(R.id.ivPriority);
                cardAssign = itemView.findViewById(R.id.cardAssign);
                cardTrack = itemView.findViewById(R.id.cardTrack);
            }

            public void bind(ComplaintModel complaint) {
                tvTitle.setText(complaint.getTitle());
                tvId.setText("ID: " + complaint.getId());
                tvDate.setText(complaint.getDate());
                tvLocation.setText(complaint.getLocation());
                tvStatus.setText(complaint.getStatus());
                tvPriority.setText(complaint.getPriority());

                // Set priority color
                switch (complaint.getPriority()) {
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
                switch (complaint.getStatus()) {
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

                cardAssign.setOnClickListener(v -> listener.onAssignClick(complaint));
                cardTrack.setOnClickListener(v -> listener.onTrackClick(complaint));
                itemView.setOnClickListener(v -> listener.onComplaintClick(complaint));
            }
        }
    }
}