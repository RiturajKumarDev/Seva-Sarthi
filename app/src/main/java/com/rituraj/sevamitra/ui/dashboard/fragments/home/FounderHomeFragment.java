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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.rituraj.sevamitra.adapters.WorkerAdapter;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.util.ArrayList;
import java.util.List;

public class FounderHomeFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private TextView tvGreeting, tvFounderName, tvCompanyName, vtProfileLetter;
    private CardView ivProfile;

    // Statistics Cards
    private TextView tvTotalWorkers, tvActiveWorkers, tvTotalWorks, tvCompletedWorks;
    private TextView tvMonthlyRevenue, tvAvgRating;
    private CardView cardTotalWorkers, cardActiveWorkers, cardTotalWorks, cardCompletedWorks;
    private CardView cardMonthlyRevenue, cardRating;

    // Charts
    private BarChart barChartWorks;

    // Management Cards
    private CardView cardManageWorkers, cardAssignWork, cardTrackWorks, cardWorkerPerformance;
    private CardView cardGenerateBill, cardReports, cardWorkerRequests, cardSettings;

    // Floating Action Button
    private FloatingActionButton fabAddWorker;

    // Data
    private List<UserData> recentWorkers;
    private WorkerAdapter workerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_founder_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setUserData();
        setupHeader();
        loadStatistics();
        setupCharts();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvFounderName = view.findViewById(R.id.tvFounderName);
        tvCompanyName = view.findViewById(R.id.tvCompanyName);
        ivProfile = view.findViewById(R.id.ivProfile);

        // Statistics
        tvTotalWorkers = view.findViewById(R.id.tvTotalWorkers);
        tvActiveWorkers = view.findViewById(R.id.tvActiveWorkers);
        tvTotalWorks = view.findViewById(R.id.tvTotalWorks);
        tvCompletedWorks = view.findViewById(R.id.tvCompletedWorks);
        tvMonthlyRevenue = view.findViewById(R.id.tvMonthlyRevenue);
        tvAvgRating = view.findViewById(R.id.tvAvgRating);

        cardTotalWorkers = view.findViewById(R.id.cardTotalWorkers);
        cardActiveWorkers = view.findViewById(R.id.cardActiveWorkers);
        cardTotalWorks = view.findViewById(R.id.cardTotalWorks);
        cardCompletedWorks = view.findViewById(R.id.cardCompletedWorks);
        cardMonthlyRevenue = view.findViewById(R.id.cardMonthlyRevenue);
        cardRating = view.findViewById(R.id.cardRating);

        // Charts
        barChartWorks = view.findViewById(R.id.barChartWorks);

        // Management Cards
        cardManageWorkers = view.findViewById(R.id.cardManageWorkers);
        cardAssignWork = view.findViewById(R.id.cardAssignWork);
        cardTrackWorks = view.findViewById(R.id.cardTrackWorks);
        cardWorkerPerformance = view.findViewById(R.id.cardWorkerPerformance);
        cardGenerateBill = view.findViewById(R.id.cardGenerateBill);
        cardReports = view.findViewById(R.id.cardReports);
        cardWorkerRequests = view.findViewById(R.id.cardWorkerRequests);
        cardSettings = view.findViewById(R.id.cardSettings);

        // FAB
        fabAddWorker = view.findViewById(R.id.fabAddWorker);
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
    }

    private void loadStatistics() {
        // Sample data - Replace with actual API calls
        tvTotalWorkers.setText("48");
        tvActiveWorkers.setText("42");
        tvTotalWorks.setText("1,284");
        tvCompletedWorks.setText("1,156");
        tvMonthlyRevenue.setText("₹2,35,000");
        tvAvgRating.setText("4.8");

        // Set trend indicators
        setTrendIndicator(tvTotalWorkers, "+8");
        setTrendIndicator(tvActiveWorkers, "+12");
        setTrendIndicator(tvTotalWorks, "+156");
        setTrendIndicator(tvMonthlyRevenue, "+15%");
    }

    private void setTrendIndicator(TextView textView, String trend) {
        // You can add a small indicator next to the value
        // This is a placeholder for trend visualization
    }

    private void setupCharts() {
        setupRevenueChart();
        setupWorksChart();
    }

    private void setupRevenueChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 185000));
        entries.add(new BarEntry(1, 210000));
        entries.add(new BarEntry(2, 195000));
        entries.add(new BarEntry(3, 235000));
        entries.add(new BarEntry(4, 280000));
        entries.add(new BarEntry(5, 315000));

        BarDataSet dataSet = new BarDataSet(entries, "Revenue (₹)");
        dataSet.setColor(getResources().getColor(R.color.logo_gold));
        dataSet.setValueTextColor(getResources().getColor(R.color.logo_white));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartWorks.setData(barData);

        // Customize chart
        barChartWorks.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        barChartWorks.setDescription(null);
        barChartWorks.setDrawGridBackground(false);
        barChartWorks.setDrawBorders(false);
        barChartWorks.setPinchZoom(false);
        barChartWorks.setScaleEnabled(false);
        barChartWorks.getAxisLeft().setTextColor(getResources().getColor(R.color.logo_white));
        barChartWorks.getAxisRight().setTextColor(getResources().getColor(R.color.logo_white));
        barChartWorks.getXAxis().setTextColor(getResources().getColor(R.color.logo_white));
        barChartWorks.getLegend().setTextColor(getResources().getColor(R.color.logo_white));

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        barChartWorks.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        barChartWorks.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        barChartWorks.animateY(1000);
        barChartWorks.invalidate();
    }

    private void setupWorksChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 45));
        entries.add(new BarEntry(1, 52));
        entries.add(new BarEntry(2, 48));
        entries.add(new BarEntry(3, 62));
        entries.add(new BarEntry(4, 78));
        entries.add(new BarEntry(5, 85));

        BarDataSet dataSet = new BarDataSet(entries, "Works Completed");
        dataSet.setColor(getResources().getColor(R.color.logo_green));
        dataSet.setValueTextColor(getResources().getColor(R.color.logo_white));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartWorks.setData(barData);

        barChartWorks.animateY(1000);
        barChartWorks.invalidate();
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalWorkers.setOnClickListener(v ->
                Toast.makeText(getContext(), "Total workers", Toast.LENGTH_SHORT).show()
        );

        cardActiveWorkers.setOnClickListener(v ->
                Toast.makeText(getContext(), "View active workers", Toast.LENGTH_SHORT).show());

        cardTotalWorks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all works", Toast.LENGTH_SHORT).show());

        cardCompletedWorks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View completed works", Toast.LENGTH_SHORT).show());

        cardMonthlyRevenue.setOnClickListener(v ->
                Toast.makeText(getContext(), "View monthly revenue", Toast.LENGTH_SHORT).show());

        // Management Cards
        cardManageWorkers.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardAssignWork.setOnClickListener(v ->
                Toast.makeText(getContext(), "Assign work to workers", Toast.LENGTH_SHORT).show());

        cardTrackWorks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardWorkerPerformance.setOnClickListener(v ->
                Toast.makeText(getContext(), "View worker performance", Toast.LENGTH_SHORT).show());

        cardGenerateBill.setOnClickListener(v ->
                Toast.makeText(getContext(), "Generate bills", Toast.LENGTH_SHORT).show());

        cardReports.setOnClickListener(v ->
                Toast.makeText(getContext(), "View reports", Toast.LENGTH_SHORT).show());

        cardWorkerRequests.setOnClickListener(v ->
                Toast.makeText(getContext(), "View worker requests", Toast.LENGTH_SHORT).show());

        cardSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show());

        // FAB
        fabAddWorker.setOnClickListener(v ->
                Toast.makeText(getContext(), "Add new worker", Toast.LENGTH_SHORT).show());
    }

    private void setUserData() {
        String displayName = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        
        tvFounderName.setText(displayName != null ? displayName : "User");
        tvCompanyName.setText(email != null ? email : "No Email");
        setAvatarColor(displayName != null && !displayName.isEmpty() ? displayName : "User");
    }

    private void setAvatarColor(String name) {
        Context context = getContext();
        if (context == null || name == null || name.isEmpty()) return;
        
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
}