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

public class SevaSarthiAdapter extends RecyclerView.Adapter<SevaSarthiAdapter.SevaMitraViewHolder> {

    private List<UserData> sevaMitraList;
    private OnSevaMitraClickListener listener;

    public interface OnSevaMitraClickListener {
        void onSevaMitraClick(UserData sevaMitra);

        void onContactClick(UserData sevaMitra);

        void onViewDetailsClick(UserData sevaMitra);
    }

    public SevaSarthiAdapter(List<UserData> sevaMitraList, OnSevaMitraClickListener listener) {
        this.sevaMitraList = sevaMitraList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SevaMitraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sevamitra, parent, false);
        return new SevaMitraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SevaMitraViewHolder holder, int position) {
        UserData sevaMitra = sevaMitraList.get(position);
        holder.bind(sevaMitra);
    }

    @Override
    public int getItemCount() {
        return sevaMitraList.size();
    }

    class SevaMitraViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivProfile, ivStatus;
        private TextView tvName, tvEmail, tvPhone, tvDepartment, tvDesignation, tvLocation;
        private LinearLayout btnContact, btnViewDetails;

        public SevaMitraViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvDesignation = itemView.findViewById(R.id.tvDesignation);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(UserData sevaMitra) {
            // Basic Info
            tvName.setText(sevaMitra.getFullName());
            tvEmail.setText(sevaMitra.getEmail() != null ? sevaMitra.getEmail() : "Email not provided");
            tvPhone.setText(sevaMitra.getPhone() != null ? sevaMitra.getPhone() : "Phone not provided");
            tvDepartment.setText("Department: " + (sevaMitra.getDepartment() != null ? sevaMitra.getDepartment() : "N/A"));
            tvDesignation.setText("Designation: " + (sevaMitra.getDesignation() != null ? sevaMitra.getDesignation() : "N/A"));
            tvLocation.setText((sevaMitra.getCity() != null ? sevaMitra.getCity() : "") +
                    (sevaMitra.getState() != null ? ", " + sevaMitra.getState() : ""));

            // Set status (all SevaMitra are active by default)
            ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_gold));

            // Load profile image
            if (sevaMitra.getProfileUrl() != null && !sevaMitra.getProfileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(sevaMitra.getProfileUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }

            // Click listeners
            cardView.setOnClickListener(v -> listener.onSevaMitraClick(sevaMitra));
            btnContact.setOnClickListener(v -> listener.onContactClick(sevaMitra));
            btnViewDetails.setOnClickListener(v -> listener.onViewDetailsClick(sevaMitra));
        }
    }
}
