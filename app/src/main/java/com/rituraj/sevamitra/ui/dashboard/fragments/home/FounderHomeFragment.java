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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
import com.rituraj.sevamitra.adapters.WorkerAdapter;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.ui.auth.RegistrationActivity;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.officer.OfficerListActivity;
import com.rituraj.sevamitra.ui.sevaSarthi.SevaSarthiListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

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
    private CardView cardTotalWorkers, cardActiveWorkers, cardTotalWorks, cardCompletedWorks, cardAddUser, cardSupport;
    private CardView cardMonthlyRevenue, cardRating;

    // Charts
    private BarChart barChartWorks;
    private ArrayList<IssueModel> totalIssueModels = new ArrayList<>(), completedIssueModels = new ArrayList<>();

    // Management Cards
    private CardView cardManageWorkers, cardAssignWork, cardTrackWorks;
    private CardView cardOfficers, cardSevaSarthi, cardSettings;

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
        cardOfficers = view.findViewById(R.id.cardOfficers);
        cardSevaSarthi = view.findViewById(R.id.cardSevaSarthi);
        cardSettings = view.findViewById(R.id.cardSettings);
        cardAddUser = view.findViewById(R.id.cardAddUser);
        cardSupport = view.findViewById(R.id.cardSupport);

        // FAB
        fabAddWorker = view.findViewById(R.id.fabAddWorker);
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

    private void loadStatistics() {
        getTotalWorkers();
        getTotalWork();
        tvMonthlyRevenue.setText("₹2,35,000");
        tvAvgRating.setText("4.8");
    }

    private void setupCharts() {
        setupWorksChart();
    }

    private void setupWorksChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        int[] months = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (IssueModel issueModel : completedIssueModels) {
            int month = getMonth(issueModel.getWorkCompleteTimestamp());
            months[month]++;
        }

        for (int i = 0; i < months.length; i++) {
            entries.add(new BarEntry(i, months[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Works Completed");
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

        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        barChartWorks.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthNames));
        barChartWorks.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartWorks.getXAxis().setLabelRotationAngle(-45f);
        barChartWorks.getXAxis().setGranularity(1f);
        barChartWorks.getXAxis().setGranularityEnabled(true);

        barChartWorks.animateY(1000);
        barChartWorks.invalidate();
    }

    private int getMonth(long timestamp) {
        if (timestamp <= 0) return 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.MONTH);
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTotalWorkers.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardActiveWorkers.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardTotalWorks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardCompletedWorks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardMonthlyRevenue.setOnClickListener(v ->
                Toast.makeText(getContext(), "View monthly revenue", Toast.LENGTH_SHORT).show());

        // Management Cards
        cardManageWorkers.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WorkerListActivity.class)));

        cardAssignWork.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardTrackWorks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardOfficers.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OfficerListActivity.class)));

        cardSevaSarthi.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SevaSarthiListActivity.class)));

        cardSettings.setOnClickListener((v) ->
                startActivity(new Intent(requireContext(), DailyItemsActivity.class)));

        cardSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SupportActivity.class)));

        cardAddUser.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RegistrationActivity.class)));

        fabAddWorker.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddIssueActivity.class)));
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
                    tvActiveWorkers.setText(String.valueOf(active));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getTotalWork() {
        totalIssueModels.clear();
        completedIssueModels.clear();
        reference = database.getReference().child("Issues");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    IssueModel issueModel = snapshot.getValue(IssueModel.class);
                    if (issueModel != null) {
                        issueModel.setId(snapshot.getKey());
                        totalIssueModels.add(issueModel);
                        if (issueModel.getStatus().equalsIgnoreCase("Resolved"))
                            completedIssueModels.add(issueModel);

                        tvTotalWorks.setText(String.valueOf(totalIssueModels.size()));
                        tvCompletedWorks.setText(String.valueOf(completedIssueModels.size()));
                        setupCharts();
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
            }
        });
    }
}