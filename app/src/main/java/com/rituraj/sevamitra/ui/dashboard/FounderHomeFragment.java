package com.rituraj.sevamitra.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.auth.RegistrationActivity;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.issues.DepartmentSelectionActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.officer.OfficerListActivity;
import com.rituraj.sevamitra.ui.sevaSarthi.SevaSarthiListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.WorkerListActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FounderHomeFragment extends Fragment {

    private View view;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private TextView tvGreeting, tvFounderName, tvCompanyName, vtProfileLetter;
    private ImageView ivOnlineStatus;
    private CardView ivProfile;

    // Statistics Cards
    private TextView tvTotalWorkers, tvActiveWorkers, tvTotalWorks, tvCompletedWorks;
    private TextView tvMonthlyRevenue, tvAvgRating;
    private CardView cardTotalWorkers, cardActiveWorkers, cardTotalWorks, cardCompletedWorks;
    private CardView cardMonthlyRevenue, cardRating;

    // Charts
    private BarChart barChartWorks;
    private ArrayList<IssueModel> totalIssueModels = new ArrayList<>();
    private ArrayList<IssueModel> completedIssueModels = new ArrayList<>();

    // Management Cards
    private CardView cardManageWorkers, cardAssignWork, cardTrackWorks;
    private CardView cardOfficers, cardSevaSarthi, cardSettings, cardAddUser, cardSupport;

    // Floating Action Button
    private FloatingActionButton fabAddWorker;

    public FounderHomeFragment() {
        // Required empty constructor
    }

    public static FounderHomeFragment newInstance() {
        return new FounderHomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_founder_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        initViews(view);
        setUserData();
        setupHeader();
        loadStatistics();
        setupClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(this::translationViews, 50);
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvFounderName = view.findViewById(R.id.tvFounderName);
        tvCompanyName = view.findViewById(R.id.tvCompanyName);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivOnlineStatus = view.findViewById(R.id.ivOnlineStatus);

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

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");
        return new LanguageModel(name, code);
    }

    private void translationViews() {
        if (isAdded() && getContext() != null && view != null) {
            LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(view));
        }
    }

    private void setupHeader() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning");
        } else if (hour < 16) {
            tvGreeting.setText("Good Afternoon");
        } else {
            tvGreeting.setText("Good Evening");
        }
        if (ivOnlineStatus != null && getContext() != null) {
            ivOnlineStatus.setColorFilter(requireContext().getColor(R.color.logo_green));
        }
    }

    private void loadStatistics() {
        getTotalWorkers();
        getTotalWork();
        getFounderProfileData();
        tvMonthlyRevenue.setText("₹2,35,000");
        tvAvgRating.setText("4.8 ★");
    }

    private void getFounderProfileData() {
        if (firebaseUser == null) return;
        DatabaseReference founderRef = database.getReference().child("UserData").child("FOUNDER").child(firebaseUser.getUid());
        founderRef.keepSynced(true);
        founderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData founder = snapshot.getValue(UserData.class);
                    if (founder != null) {
                        if (founder.getFullName() != null && !founder.getFullName().isEmpty()) {
                            tvFounderName.setText(founder.getFullName());
                            setAvatarColor(founder.getFullName());
                        }
                        if (founder.getCompanyName() != null && !founder.getCompanyName().isEmpty()) {
                            tvCompanyName.setText(founder.getCompanyName());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupCharts() {
        if (barChartWorks == null || getContext() == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        int[] months = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (IssueModel issueModel : completedIssueModels) {
            int month = getMonth(issueModel.getWorkCompleteTimestamp());
            if (month >= 0 && month < 12) {
                months[month]++;
            }
        }

        for (int i = 0; i < months.length; i++) {
            entries.add(new BarEntry(i, months[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completed Works");
        dataSet.setColor(requireContext().getColor(R.color.logo_gold));
        dataSet.setValueTextColor(requireContext().getColor(R.color.logo_white));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartWorks.setData(barData);

        // Customize chart
        barChartWorks.setBackgroundColor(requireContext().getColor(android.R.color.transparent));
        barChartWorks.setDescription(null);
        barChartWorks.setDrawGridBackground(false);
        barChartWorks.setDrawBorders(false);
        barChartWorks.setPinchZoom(false);
        barChartWorks.setScaleEnabled(false);
        barChartWorks.getAxisLeft().setTextColor(requireContext().getColor(R.color.logo_white));
        barChartWorks.getAxisRight().setTextColor(requireContext().getColor(R.color.logo_white));
        barChartWorks.getXAxis().setTextColor(requireContext().getColor(R.color.logo_white));
        barChartWorks.getLegend().setTextColor(requireContext().getColor(R.color.logo_white));

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
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadStatistics);
        }

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
                Toast.makeText(getContext(), "Monthly Revenue Report", Toast.LENGTH_SHORT).show());

        cardRating.setOnClickListener(v ->
                Toast.makeText(getContext(), "Founder Rating & Reviews", Toast.LENGTH_SHORT).show());

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
                startActivity(new Intent(requireContext(), DepartmentSelectionActivity.class)));
    }

    private void setUserData() {
        if (firebaseUser == null) return;
        String displayName = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();

        tvFounderName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Founder");
        tvCompanyName.setText(email != null && !email.isEmpty() ? email : "Founder Account");
        setAvatarColor(displayName != null && !displayName.isEmpty() ? displayName : "Founder");
    }

    private void setAvatarColor(String name) {
        if (getContext() == null || name == null || name.isEmpty()) return;
        Context context = getContext();
        char firstChar = Character.toUpperCase(name.charAt(0));
        vtProfileLetter.setText(String.valueOf(firstChar));

        int color;
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
                    long active = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserData worker = child.getValue(UserData.class);
                        if (worker != null && Status.ACTIVE.equalsIgnoreCase(worker.getIsSelected())) {
                            active++;
                        }
                    }
                    tvTotalWorkers.setText(String.valueOf(total));
                    tvActiveWorkers.setText(String.valueOf(active > 0 ? active : (total > 0 ? total : 0)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getTotalWork() {
        reference = database.getReference().child("Issues");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                totalIssueModels.clear();
                completedIssueModels.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        IssueModel issueModel = childSnapshot.getValue(IssueModel.class);
                        if (issueModel != null) {
                            issueModel.setId(childSnapshot.getKey());
                            totalIssueModels.add(issueModel);
                            if (issueModel.getStatus() != null &&
                                    (issueModel.getStatus().equalsIgnoreCase("Resolved") ||
                                            issueModel.getStatus().equalsIgnoreCase("Completed"))) {
                                completedIssueModels.add(issueModel);
                            }
                        }
                    }
                }

                tvTotalWorks.setText(String.valueOf(totalIssueModels.size()));
                tvCompletedWorks.setText(String.valueOf(completedIssueModels.size()));
                setupCharts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }
}