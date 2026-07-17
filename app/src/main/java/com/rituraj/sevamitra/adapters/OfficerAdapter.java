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
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;

import java.util.List;

public class OfficerAdapter extends RecyclerView.Adapter<OfficerAdapter.OfficerViewHolder> {

    private List<UserData> officerList;
    private OnOfficerClickListener listener;

    public interface OnOfficerClickListener {
        void onOfficerClick(UserData officer);

        void onContactClick(UserData officer);

        void onViewDetailsClick(UserData officer);
    }

    public OfficerAdapter(List<UserData> officerList, OnOfficerClickListener listener) {
        this.officerList = officerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfficerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_officer, parent, false);
        return new OfficerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfficerViewHolder holder, int position) {
        UserData officer = officerList.get(position);
        holder.bind(officer);
    }

    @Override
    public int getItemCount() {
        return officerList.size();
    }

    class OfficerViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivProfile, ivStatus;
        private TextView tvName, tvEmail, tvPhone, tvDistrict, tvDivision, tvLocation;
        private LinearLayout btnContact, btnViewDetails;

        public OfficerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvDivision = itemView.findViewById(R.id.tvDivision);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(UserData officer) {
            // Basic Info
            tvName.setText(officer.getFullName());
            tvEmail.setText(officer.getEmail() != null ? officer.getEmail() : "Email not provided");
            tvPhone.setText(officer.getPhone() != null ? officer.getPhone() : "Phone not provided");
            tvDistrict.setText("District: " + (officer.getDistrict() != null ? officer.getDistrict() : "N/A"));
            tvDivision.setText("Division: " + (officer.getDivision() != null ? officer.getDivision() : "N/A"));
            tvLocation.setText((officer.getCity() != null ? officer.getCity() : "") +
                    (officer.getState() != null ? ", " + officer.getState() : ""));

            // Set status (all officers are active by default)
            ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_green));

            // Load profile image
            if (officer.getProfileUrl() != null && !officer.getProfileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(officer.getProfileUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }

            // Click listeners
            cardView.setOnClickListener(v -> listener.onOfficerClick(officer));
            btnContact.setOnClickListener(v -> listener.onContactClick(officer));
            btnViewDetails.setOnClickListener(v -> listener.onViewDetailsClick(officer));
        }
    }
}