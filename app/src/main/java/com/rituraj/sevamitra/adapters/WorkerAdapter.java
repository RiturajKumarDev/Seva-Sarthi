package com.rituraj.sevamitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.Status;
import com.rituraj.sevamitra.models.UserData;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    private List<UserData> workerList;
    private OnWorkerClickListener listener;
    private String issueId;
    private FirebaseUser firebaseUser;

    public interface OnWorkerClickListener {
        void onWorkerClick(UserData worker);

        void onContactClick(UserData worker);

        void onManageClick(UserData worker);

        void onAssignClick(UserData worker);
    }

    public WorkerAdapter(List<UserData> workerList, String issueId, FirebaseUser firebaseUser, OnWorkerClickListener listener) {
        this.workerList = workerList;
        this.issueId = issueId;
        this.firebaseUser = firebaseUser;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        UserData worker = workerList.get(position);
        holder.bind(worker);
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivProfile, ivStatus;
        private TextView tvName, tvCategory;
        private TextView tvEmail, tvPhone, tvAddress, tvCityState, tvSkills;
        private LinearLayout btnContact, btnManage, btnAssign;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCityState = itemView.findViewById(R.id.tvCityState);
            tvSkills = itemView.findViewById(R.id.tvSkills);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnManage = itemView.findViewById(R.id.btnManage);
            btnAssign = itemView.findViewById(R.id.btnAssign);
        }

        public void bind(UserData worker) {
            // Basic Info
            tvName.setText(worker.getFullName());
            tvCategory.setText(worker.getDepartment() != null ? worker.getDepartment() : "General");

            // Contact Info
            tvEmail.setText(worker.getEmail() != null ? worker.getEmail() : "Email not provided");
            tvPhone.setText(worker.getPhone() != null ? worker.getPhone() : "Phone not provided");

            // Address Info
            tvAddress.setText(worker.getAddress() != null ? worker.getAddress() : "Address not provided");
            tvCityState.setText((worker.getCity() != null ? worker.getCity() : "") + ", " +
                    (worker.getState() != null ? worker.getState() : ""));

            // Skills
            if (worker.getSkills() != null && !worker.getSkills().isEmpty()) {
                StringBuilder skillsStr = new StringBuilder();
                for (String skill : worker.getSkills()) {
                    if (skillsStr.length() > 0) skillsStr.append(" • ");
                    skillsStr.append(skill);
                }
                tvSkills.setText(skillsStr.toString());
            } else {
                tvSkills.setText("No additional skills listed");
            }

            // Set status color
            String status = worker.getIsSelected();
            if (Status.ACTIVE.equals(status)) {
                btnManage.setVisibility(View.GONE);
                ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
            } else if (Status.INACTIVE.equals(status)) {
                btnManage.setVisibility(View.VISIBLE);
                ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_orange));
            } else {
                btnManage.setVisibility(View.VISIBLE);
                ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_red));
            }

            // Load profile image
            if (worker.getProfileUrl() != null && !worker.getProfileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(worker.getProfileUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }

            if (issueId == null || issueId.isEmpty())
                btnAssign.setVisibility(View.GONE);

            if (firebaseUser != null && firebaseUser.getPhotoUrl() != null && firebaseUser.getPhotoUrl().toString().equals("SEVASARTHI")) {
                btnManage.setVisibility(View.GONE);
                btnAssign.setVisibility(View.GONE);
            }

            // Click listeners
            cardView.setOnClickListener(v -> listener.onWorkerClick(worker));
            btnContact.setOnClickListener(v -> listener.onContactClick(worker));
            btnManage.setOnClickListener(v -> listener.onManageClick(worker));
            btnAssign.setOnClickListener(v -> listener.onAssignClick(worker));
        }
    }
}