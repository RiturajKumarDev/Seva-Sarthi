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

public class WorkerHomeFragment extends Fragment {
    private View view;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    // Header Views
    private TextView tvGreeting, tvWorkerName, tvWorkerId, tvCurrentTime;
    private ImageView ivProfile, ivOnlineStatus;
    private CardView cardProfile;

    // Statistics Cards
    private TextView tvTodayTasks, tvCompletedToday, tvPendingTasks, tvTotalEarnings;
    private TextView tvThisWeekEarnings, tvRating, tvTotalWorks, tvAvailabilityStatus;
    private CardView cardTodayTasks, cardCompletedTasks, cardPendingTasks, cardEarnings;
    private CardView cardWeeklyEarnings, cardRating, cardTotalWorksCard, cardAvailability;

    // Quick Actions
    private CardView cardAvailableWork, cardMyTasks, cardCompletedWork, cardEarningsHistory;
    private CardView cardAttendance, cardLeaveRequest, cardPerformance, cardSupport;

    // Current Tasks Section
    private RecyclerView rvCurrentTasks;
    private LinearLayout viewAllTasks;

    // Earning Charts
    private LinearLayout weeklyEarningsChart;

    // Floating Action Button
    private FloatingActionButton fabMarkAttendance;

    // Data
    private UserData currentUser;
    private List<TaskModel> currentTasks;
    private TaskAdapter taskAdapter;

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
        updateCurrentTime();
        loadStatistics();
        loadCurrentTasks();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvWorkerName = view.findViewById(R.id.tvWorkerName);
        tvWorkerId = view.findViewById(R.id.tvWorkerId);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivOnlineStatus = view.findViewById(R.id.ivOnlineStatus);
        cardProfile = view.findViewById(R.id.cardProfile);

        // Statistics
        tvTodayTasks = view.findViewById(R.id.tvTodayTasks);
        tvCompletedToday = view.findViewById(R.id.tvCompletedToday);
        tvPendingTasks = view.findViewById(R.id.tvPendingTasks);
        tvTotalEarnings = view.findViewById(R.id.tvTotalEarnings);
        tvThisWeekEarnings = view.findViewById(R.id.tvThisWeekEarnings);
        tvRating = view.findViewById(R.id.tvRating);
        tvTotalWorks = view.findViewById(R.id.tvTotalWorks);
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus);

        cardTodayTasks = view.findViewById(R.id.cardTodayTasks);
        cardCompletedTasks = view.findViewById(R.id.cardCompletedTasks);
        cardPendingTasks = view.findViewById(R.id.cardPendingTasks);
        cardEarnings = view.findViewById(R.id.cardEarnings);
        cardWeeklyEarnings = view.findViewById(R.id.cardWeeklyEarnings);
        cardRating = view.findViewById(R.id.cardRating);
        cardTotalWorksCard = view.findViewById(R.id.cardTotalWorksCard);
        cardAvailability = view.findViewById(R.id.cardAvailability);

        // Quick Actions
        cardAvailableWork = view.findViewById(R.id.cardAvailableWork);
        cardMyTasks = view.findViewById(R.id.cardMyTasks);
        cardCompletedWork = view.findViewById(R.id.cardCompletedWork);
        cardEarningsHistory = view.findViewById(R.id.cardEarningsHistory);
        cardAttendance = view.findViewById(R.id.cardAttendance);
        cardLeaveRequest = view.findViewById(R.id.cardLeaveRequest);
        cardPerformance = view.findViewById(R.id.cardPerformance);
        cardSupport = view.findViewById(R.id.cardSupport);

        // Current Tasks
        rvCurrentTasks = view.findViewById(R.id.rvCurrentTasks);
        viewAllTasks = view.findViewById(R.id.viewAllTasks);

        // Charts
        weeklyEarningsChart = view.findViewById(R.id.weeklyEarningsChart);

        // FAB
        fabMarkAttendance = view.findViewById(R.id.fabMarkAttendance);
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

    private void loadStatistics() {
        // Sample data - Replace with actual API calls
        tvTodayTasks.setText("8");
        tvCompletedToday.setText("3");
        tvPendingTasks.setText("5");
        tvTotalEarnings.setText("₹45,000");
        tvThisWeekEarnings.setText("₹8,500");
        tvRating.setText("4.8 ★");
        tvTotalWorks.setText("156");

        // Set progress for today's tasks
        updateTaskProgress(3, 8);
    }

    private void updateTaskProgress(int completed, int total) {
        // Can add a progress bar here
        int percentage = (completed * 100) / total;
        // Update progress indicator
    }

    private void loadCurrentTasks() {
        currentTasks = getSampleTasks();

        taskAdapter = new TaskAdapter(currentTasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel task) {
                showTaskDetails(task);
            }

            @Override
            public void onStartTaskClick(TaskModel task) {
                Toast.makeText(getContext(), "Starting task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleteTaskClick(TaskModel task) {
                Toast.makeText(getContext(), "Task completed: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        rvCurrentTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCurrentTasks.setAdapter(taskAdapter);
    }

    private List<TaskModel> getSampleTasks() {
        List<TaskModel> tasks = new ArrayList<>();

        TaskModel task1 = new TaskModel();
        task1.setId("T001");
        task1.setTitle("AC Repair - Sector 15");
        task1.setDescription("Samsung AC not cooling properly");
        task1.setLocation("Sector 15, Noida");
        task1.setCustomerName("Mr. Sharma");
        task1.setCustomerPhone("9876543210");
        task1.setDateTime("Today, 10:00 AM");
        task1.setStatus("Pending");
        task1.setAmount("₹700");
        tasks.add(task1);

        TaskModel task2 = new TaskModel();
        task2.setId("T002");
        task2.setTitle("Plumbing Work - Indirapuram");
        task2.setDescription("Pipe leakage in kitchen");
        task2.setLocation("Indirapuram, Ghaziabad");
        task2.setCustomerName("Mrs. Verma");
        task2.setCustomerPhone("9876543211");
        task2.setDateTime("Today, 2:00 PM");
        task2.setStatus("Pending");
        task2.setAmount("₹500");
        tasks.add(task2);

        TaskModel task3 = new TaskModel();
        task3.setId("T003");
        task3.setTitle("CCTV Installation - Vaishali");
        task3.setDescription("Install 4 cameras in office");
        task3.setLocation("Vaishali, Ghaziabad");
        task3.setCustomerName("Mr. Gupta");
        task3.setCustomerPhone("9876543212");
        task3.setDateTime("Tomorrow, 11:00 AM");
        task3.setStatus("Scheduled");
        task3.setAmount("₹2500");
        tasks.add(task3);

        return tasks;
    }

    private void setupClickListeners() {
        // Statistics Cards
        cardTodayTasks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View today's tasks", Toast.LENGTH_SHORT).show());

        cardCompletedTasks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View completed tasks", Toast.LENGTH_SHORT).show());

        cardPendingTasks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View pending tasks", Toast.LENGTH_SHORT).show());

        cardEarnings.setOnClickListener(v ->
                Toast.makeText(getContext(), "View earnings details", Toast.LENGTH_SHORT).show());

        cardWeeklyEarnings.setOnClickListener(v ->
                Toast.makeText(getContext(), "View weekly earnings", Toast.LENGTH_SHORT).show());

        cardTotalWorksCard.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all works history", Toast.LENGTH_SHORT).show());

        cardAvailability.setOnClickListener(v -> {
            // Toggle availability status
            Toast.makeText(getContext(), "Change availability status", Toast.LENGTH_SHORT).show();
        });

        // Quick Actions
        cardAvailableWork.setOnClickListener(v ->
                Toast.makeText(getContext(), "Browse available work", Toast.LENGTH_SHORT).show());

        cardMyTasks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View my tasks", Toast.LENGTH_SHORT).show());

        cardCompletedWork.setOnClickListener(v ->
                Toast.makeText(getContext(), "View completed works", Toast.LENGTH_SHORT).show());

        cardEarningsHistory.setOnClickListener(v ->
                Toast.makeText(getContext(), "View earnings history", Toast.LENGTH_SHORT).show());

        cardAttendance.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mark attendance", Toast.LENGTH_SHORT).show());

        cardLeaveRequest.setOnClickListener(v ->
                Toast.makeText(getContext(), "Apply for leave", Toast.LENGTH_SHORT).show());

        cardPerformance.setOnClickListener(v ->
                Toast.makeText(getContext(), "View performance report", Toast.LENGTH_SHORT).show());

        cardSupport.setOnClickListener(v ->
                Toast.makeText(getContext(), "Contact support", Toast.LENGTH_SHORT).show());

        // View All Tasks
        viewAllTasks.setOnClickListener(v ->
                Toast.makeText(getContext(), "View all tasks", Toast.LENGTH_SHORT).show());

        // FAB
        fabMarkAttendance.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mark attendance", Toast.LENGTH_SHORT).show());

        // Setup weekly earnings chart
        setupWeeklyEarningsChart();
    }

    private void setupWeeklyEarningsChart() {
        // Create bar chart for weekly earnings
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int[] earnings = {1200, 1500, 1000, 1800, 2000, 2500};

        weeklyEarningsChart.removeAllViews();

        for (int i = 0; i < days.length; i++) {
            View barView = getLayoutInflater().inflate(R.layout.item_earning_bar, null);
            TextView tvDay = barView.findViewById(R.id.tvDay);
            TextView tvAmount = barView.findViewById(R.id.tvAmount);
            View bar = barView.findViewById(R.id.bar);

            tvDay.setText(days[i]);
            tvAmount.setText("₹" + earnings[i]);

            // Set bar height based on earnings (max height 150dp)
            int height = (earnings[i] * 150) / 2500;
            bar.getLayoutParams().height = height;

            weeklyEarningsChart.addView(barView);
        }
    }

    private void showTaskDetails(TaskModel task) {
        Toast.makeText(getContext(), "Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        // Navigate to task details fragment
    }

    private void setUserData() {
        tvWorkerName.setText(firebaseUser.getDisplayName());
        tvWorkerId.setText(firebaseUser.getEmail());
        Glide.with(requireActivity())
                .load(firebaseUser.getPhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile);
    }

    // Task Model Class
    public static class TaskModel {
        private String id;
        private String title;
        private String description;
        private String location;
        private String customerName;
        private String customerPhone;
        private String dateTime;
        private String status;
        private String amount;

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

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }

    // Task Adapter
    public static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<TaskModel> taskList;
        private OnTaskClickListener listener;

        public interface OnTaskClickListener {
            void onTaskClick(TaskModel task);

            void onStartTaskClick(TaskModel task);

            void onCompleteTaskClick(TaskModel task);
        }

        public TaskAdapter(List<TaskModel> taskList, OnTaskClickListener listener) {
            this.taskList = taskList;
            this.listener = listener;
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_current_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, int position) {
            TaskModel task = taskList.get(position);
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle, tvDateTime, tvLocation, tvAmount, tvStatus;
            private ImageView ivStatus;
            private CardView cardStartTask, cardCompleteTask;

            public TaskViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                ivStatus = itemView.findViewById(R.id.ivStatus);
                cardStartTask = itemView.findViewById(R.id.cardStartTask);
                cardCompleteTask = itemView.findViewById(R.id.cardCompleteTask);
            }

            public void bind(TaskModel task) {
                tvTitle.setText(task.getTitle());
                tvDateTime.setText(task.getDateTime());
                tvLocation.setText(task.getLocation());
                tvAmount.setText(task.getAmount());
                tvStatus.setText(task.getStatus());

                // Set status color
                if ("Pending".equals(task.getStatus())) {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_orange));
                } else if ("Scheduled".equals(task.getStatus())) {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_gold));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_gold));
                } else {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
                }

                cardStartTask.setOnClickListener(v -> listener.onStartTaskClick(task));
                cardCompleteTask.setOnClickListener(v -> listener.onCompleteTaskClick(task));
                itemView.setOnClickListener(v -> listener.onTaskClick(task));
            }
        }
    }
}