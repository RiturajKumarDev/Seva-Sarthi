package com.rituraj.sevamitra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    private List<UserData> workers;
    private Context context;

    public WorkerAdapter(List<UserData> workers, Context context) {
        this.workers = workers;
        this.context = context;
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
        UserData worker = workers.get(position);

        holder.tvName.setText(worker.getFullName());
        holder.tvCategory.setText(worker.getPrimaryCategory());
        holder.tvExperience.setText("Exp: " + worker.getExperience());
        holder.tvHourlyRate.setText(worker.getHourlyRate() + "/hour");
        holder.tvRating.setText(String.valueOf(4));
        holder.tvLocation.setText(worker.getAddress());

        holder.tvAvailable.setText("Available");

        // Set rating stars
        setRatingStars(holder, 4);

        holder.cardView.setOnClickListener(v -> {
            Toast.makeText(context, "Selected: " + worker.getFullName(), Toast.LENGTH_LONG).show();
        });

        holder.btnHire.setOnClickListener(v -> {
            Toast.makeText(context, "Hire " + worker.getFullName(), Toast.LENGTH_LONG).show();
        });
    }

    private void setRatingStars(WorkerViewHolder holder, double rating) {
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;

        // Reset all stars
        holder.star1.setVisibility(View.GONE);
        holder.star2.setVisibility(View.GONE);
        holder.star3.setVisibility(View.GONE);
        holder.star4.setVisibility(View.GONE);
        holder.star5.setVisibility(View.GONE);

        // Set full stars
        for (int i = 1; i <= fullStars; i++) {
            getStarView(holder, i).setVisibility(View.VISIBLE);
            getStarView(holder, i).setText("★");
        }

        // Set half star
        if (hasHalfStar && fullStars < 5) {
            getStarView(holder, fullStars + 1).setVisibility(View.VISIBLE);
            getStarView(holder, fullStars + 1).setText("½");
        }

        // Set empty stars
        for (int i = fullStars + (hasHalfStar ? 2 : 1); i <= 5; i++) {
            getStarView(holder, i).setVisibility(View.VISIBLE);
            getStarView(holder, i).setText("☆");
        }
    }

    private TextView getStarView(WorkerViewHolder holder, int starNumber) {
        switch (starNumber) {
            case 1:
                return holder.star1;
            case 2:
                return holder.star2;
            case 3:
                return holder.star3;
            case 4:
                return holder.star4;
            case 5:
                return holder.star5;
            default:
                return holder.star1;
        }
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExperience, tvHourlyRate, tvRating, tvLocation, tvAvailable;
        TextView star1, star2, star3, star4, star5;
        CardView cardView;
        com.google.android.material.button.MaterialButton btnHire;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
            cardView = itemView.findViewById(R.id.cardView);
            btnHire = itemView.findViewById(R.id.btnHire);
        }
    }
}