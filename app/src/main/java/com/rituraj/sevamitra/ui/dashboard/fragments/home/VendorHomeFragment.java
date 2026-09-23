package com.rituraj.sevamitra.ui.dashboard.fragments.home;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.issues.DepartmentSelectionActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;
import com.rituraj.sevamitra.ui.worker.DailyOrderListActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VendorHomeFragment extends Fragment {

    private View view;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // Header Views
    private TextView tvGreeting, tvVendorName, tvVendorId, tvVendorDepartment, tvCurrentTime, vtProfileLetter;
    private ImageView ivOnlineStatus;
    private CardView ivProfile;

    // Statistics Cards
    private TextView tvTotalOrders, tvActiveOrders, tvCompletedOrders, tvRating, tvAvailabilityStatus;
    private CardView cardAvailability, cardTotalOrders, cardActiveOrders, cardCompletedOrders;

    // Quick Actions
    private CardView cardMyDailyTasks, cardRegisterIssue, cardAvailableWork;
    private CardView cardAttendance, cardLeaveRequest, cardSupport;

    // Floating Action Button
    private FloatingActionButton fabAddIssue;

    public VendorHomeFragment() {
        // Required empty public constructor
    }

    public static VendorHomeFragment newInstance() {
        return new VendorHomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vendor_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setUserData();
        setupHeader();
        getVendorData();
        loadOrdersStatistics();
        updateCurrentTime();
        setupClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> translationViews(view), 50);
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvVendorName = view.findViewById(R.id.tvVendorName);
        tvVendorId = view.findViewById(R.id.tvVendorId);
        tvVendorDepartment = view.findViewById(R.id.tvVendorDepartment);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivOnlineStatus = view.findViewById(R.id.ivOnlineStatus);

        // Statistics
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvActiveOrders = view.findViewById(R.id.tvActiveOrders);
        tvCompletedOrders = view.findViewById(R.id.tvCompletedOrders);
        tvRating = view.findViewById(R.id.tvRating);
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus);

        cardAvailability = view.findViewById(R.id.cardAvailability);
        cardTotalOrders = view.findViewById(R.id.cardTotalOrders);
        cardActiveOrders = view.findViewById(R.id.cardActiveOrders);
        cardCompletedOrders = view.findViewById(R.id.cardCompletedOrders);

        // Quick Actions
        cardMyDailyTasks = view.findViewById(R.id.cardMyDailyTasks);
        cardRegisterIssue = view.findViewById(R.id.cardRegisterIssue);
        cardAvailableWork = view.findViewById(R.id.cardAvailableWork);
        cardAttendance = view.findViewById(R.id.cardAttendance);
        cardLeaveRequest = view.findViewById(R.id.cardLeaveRequest);
        cardSupport = view.findViewById(R.id.cardSupport);

        // FAB
        fabAddIssue = view.findViewById(R.id.fabAddIssue);
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");
        return new LanguageModel(name, code);
    }

    private void translationViews(View rootView) {
        if (isAdded() && getContext() != null) {
            LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(rootView));
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
        if (getContext() != null) {
            ivOnlineStatus.setColorFilter(requireContext().getColor(R.color.logo_green));
        }
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void setUserData() {
        if (firebaseUser != null) {
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            tvVendorName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Vendor");
            tvVendorId.setText(email != null && !email.isEmpty() ? email : "Vendor Account");
            setAvatarColor(displayName != null && !displayName.isEmpty() ? displayName : "Vendor");
        }
    }

    private void setAvatarColor(String name) {
        if (getContext() == null || name == null || name.isEmpty()) return;
        Context context = getContext();
        char firstChar = name.toUpperCase().charAt(0);
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

    private void getVendorData() {
        if (firebaseUser == null) return;
        DatabaseReference vendorRef = database.getReference().child("UserData").child("VENDOR").child(firebaseUser.getUid());
        vendorRef.keepSynced(true);
        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData vendor = snapshot.getValue(UserData.class);
                    if (vendor != null) {
                        if (vendor.getFullName() != null && !vendor.getFullName().isEmpty()) {
                            tvVendorName.setText(vendor.getFullName());
                            setAvatarColor(vendor.getFullName());
                        }
                        if (vendor.getDepartment() != null && !vendor.getDepartment().isEmpty()) {
                            tvVendorDepartment.setText(vendor.getDepartment());
                        }
                        if (Status.PENDING.equalsIgnoreCase(vendor.getIsSelected())) {
                            tvAvailabilityStatus.setText("Waiting for Approval");
                        } else {
                            tvAvailabilityStatus.setText("Active & Ready");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadOrdersStatistics() {
        if (firebaseUser == null) return;
        String userId = firebaseUser.getUid();
        DatabaseReference worksRef = database.getReference().child("DailyWorks");
        worksRef.keepSynced(true);
        worksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                int total = 0;
                int active = 0;
                int completed = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        DailyItemModel item = childSnapshot.getValue(DailyItemModel.class);
                        if (item != null) {
                            boolean isMyOrder = (item.getCreatedBy() != null && item.getCreatedBy().equalsIgnoreCase(userId)) ||
                                    (item.getSupplierId() != null && item.getSupplierId().equalsIgnoreCase(userId));
                            if (isMyOrder) {
                                total++;
                                if (Status.ACCEPTED.equalsIgnoreCase(item.getStatus()) || Status.PENDING.equalsIgnoreCase(item.getStatus())) {
                                    active++;
                                } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                                    completed++;
                                }
                            }
                        }
                    }
                }

                tvTotalOrders.setText(String.valueOf(total));
                tvActiveOrders.setText(String.valueOf(active));
                tvCompletedOrders.setText(String.valueOf(completed));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::loadOrdersStatistics);

        View.OnClickListener openDailyOrders = v ->
                startActivity(new Intent(requireContext(), DailyOrderListActivity.class));

        cardMyDailyTasks.setOnClickListener(openDailyOrders);
        cardTotalOrders.setOnClickListener(openDailyOrders);
        cardActiveOrders.setOnClickListener(openDailyOrders);
        cardCompletedOrders.setOnClickListener(openDailyOrders);

        cardRegisterIssue.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DepartmentSelectionActivity.class)));

        cardAttendance.setOnClickListener(v ->
                Toast.makeText(getContext(), "Vendor Check-In Recorded", Toast.LENGTH_SHORT).show());

        cardLeaveRequest.setOnClickListener(v ->
                Toast.makeText(getContext(), "Apply for Leave", Toast.LENGTH_SHORT).show());

        cardSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SupportActivity.class)));

        fabAddIssue.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DepartmentSelectionActivity.class)));
    }
}