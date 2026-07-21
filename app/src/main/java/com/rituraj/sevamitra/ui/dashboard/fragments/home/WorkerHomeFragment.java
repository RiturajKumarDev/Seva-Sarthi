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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;
import com.rituraj.sevamitra.translationLanguage.LanguageManager;
import com.rituraj.sevamitra.ui.dailyItems.DailyItemsActivity;
import com.rituraj.sevamitra.ui.issues.AddIssueActivity;
import com.rituraj.sevamitra.ui.issues.DepartmentSelectionActivity;
import com.rituraj.sevamitra.ui.issues.IssueListActivity;
import com.rituraj.sevamitra.ui.support.SupportActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkerHomeFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Header Views
    private TextView tvGreeting, tvWorkerName, tvWorkerId, tvCurrentTime, vtProfileLetter;
    private ImageView ivOnlineStatus;
    private CardView ivProfile;

    // Statistics Cards
    private TextView tvRating, tvTotalWorks, tvAvailabilityStatus;
    private CardView cardAvailability;

    // Quick Actions
    private CardView cardAvailableWork, cardMyTasks, cardCompletedWork, cardMyDailyTasks;
    private CardView cardAttendance, cardLeaveRequest, cardSupport;

    // Floating Action Button
    private FloatingActionButton fabMarkAttendance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_worker_home, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        initViews(view);
        setUserData();
        setupHeader();
        getUserData();
        updateCurrentTime();
        setupClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sometimes need slight delay for all views to render
        new Handler(Looper.getMainLooper()).postDelayed(() -> translationViews(view), 50);
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        vtProfileLetter = view.findViewById(R.id.vtProfileLetter);
        tvWorkerName = view.findViewById(R.id.tvWorkerName);
        tvWorkerId = view.findViewById(R.id.tvWorkerId);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivOnlineStatus = view.findViewById(R.id.ivOnlineStatus);

        // Statistics
        tvRating = view.findViewById(R.id.tvRating);
        tvTotalWorks = view.findViewById(R.id.tvTotalWorks);
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus);

        cardAvailability = view.findViewById(R.id.cardAvailability);

        // Quick Actions
        cardAvailableWork = view.findViewById(R.id.cardAvailableWork);
        cardMyTasks = view.findViewById(R.id.cardMyTasks);
        cardMyDailyTasks = view.findViewById(R.id.cardMyDailyTasks);
        cardCompletedWork = view.findViewById(R.id.cardCompletedWork);
        cardAttendance = view.findViewById(R.id.cardAttendance);
        cardLeaveRequest = view.findViewById(R.id.cardLeaveRequest);
        cardSupport = view.findViewById(R.id.cardSupport);

        // FAB
        fabMarkAttendance = view.findViewById(R.id.fabMarkAttendance);
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");

        return new LanguageModel(name, code);
    }

    private void translationViews(View rootView) {
        LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(rootView));
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
        ivOnlineStatus.setColorFilter(getResources().getColor(R.color.logo_green));
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }

    private void setupClickListeners() {
        // Quick Actions
        cardAvailableWork.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardMyTasks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardCompletedWork.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IssueListActivity.class)));

        cardMyDailyTasks.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DailyItemsActivity.class)));

        cardAttendance.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mark attendance", Toast.LENGTH_SHORT).show());

        cardLeaveRequest.setOnClickListener(v ->
                Toast.makeText(getContext(), "Apply for leave", Toast.LENGTH_SHORT).show());

        cardSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SupportActivity.class)));

        // FAB
        fabMarkAttendance.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DepartmentSelectionActivity.class)));
    }

    private void setUserData() {
        if (firebaseUser != null) {
            tvWorkerName.setText(firebaseUser.getDisplayName());
            tvWorkerId.setText(firebaseUser.getEmail());
            setAvatarColor(firebaseUser.getDisplayName());
        }
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
        reference = database.getReference().child("UserData").child("WORKER").child(firebaseUser.getUid());
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData worker = snapshot.getValue(UserData.class);
                    if (worker != null)
                        if (Status.PENDING.equalsIgnoreCase(worker.getIsSelected())) {
                            tvAvailabilityStatus.setText("Waiting for Founder Approval");
                        } else {
                            tvAvailabilityStatus.setText("Available for Work");
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}