package com.rituraj.sevamitra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.IssueModel;
import com.rituraj.sevamitra.models.Priority;
import com.rituraj.sevamitra.models.Status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    private List<IssueModel> issueList;
    private OnIssueClickListener listener;
    private String userType;
    private Context context;

    public interface OnIssueClickListener {
        void onIssueClick(IssueModel issue);

        void onAssignClick(IssueModel issue);

        void onTrackClick(IssueModel issue);

        void onApproveClick(IssueModel issue);
    }

    public IssueAdapter(List<IssueModel> issueList, OnIssueClickListener listener, String userType) {
        this.issueList = issueList;
        this.listener = listener;
        this.userType = userType;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        IssueModel issue = issueList.get(position);
        holder.bind(issue);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    class IssueViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvId, tvTitle, tvDescription, tvLocation, tvProblemType, tvIssueType;
        private TextView tvPriority, tvStatus, tvCreatedDate, tvAssignedTo;
        private TextView tvWorkAssignTime, tvWorkCompleteTime, tvSevaMitraApproveTime, tvOfficerApproveTime;
        private TextView tvResolutionNotes;
        private ImageView ivPriority, ivStatus;
        private LinearLayout btnAssign, btnTrack, btnApprove;
        private LinearLayout timelineLayout, resolutionLayout;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvId = itemView.findViewById(R.id.tvId);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvProblemType = itemView.findViewById(R.id.tvProblemType);
            tvIssueType = itemView.findViewById(R.id.tvIssueType);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            tvWorkAssignTime = itemView.findViewById(R.id.tvWorkAssignTime);
            tvWorkCompleteTime = itemView.findViewById(R.id.tvWorkCompleteTime);
            tvSevaMitraApproveTime = itemView.findViewById(R.id.tvSevaMitraApproveTime);
            tvOfficerApproveTime = itemView.findViewById(R.id.tvOfficerApproveTime);
            tvResolutionNotes = itemView.findViewById(R.id.tvResolutionNotes);
            ivPriority = itemView.findViewById(R.id.ivPriority);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            btnAssign = itemView.findViewById(R.id.btnAssign);
            btnTrack = itemView.findViewById(R.id.btnTrack);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            timelineLayout = itemView.findViewById(R.id.timelineLayout);
            resolutionLayout = itemView.findViewById(R.id.resolutionLayout);
        }

        public void bind(IssueModel issue) {
            // Basic Info
            tvId.setText("ID: " + issue.getId());
            tvTitle.setText(issue.getProblemTitle());
            tvDescription.setText(issue.getProblemDescription());
            tvLocation.setText("📍 " + issue.getLocation());
            tvProblemType.setText("📋 " + issue.getProblemType());
            tvIssueType.setText("🔧 " + issue.getIssue());


            // Priority with color
            tvPriority.setText(issue.getPriority());
            switch (issue.getPriority()) {
                case Priority.CRITICAL:
                    tvPriority.setTextColor(context.getColor(R.color.logo_orange));
                    ivPriority.setColorFilter(context.getColor(R.color.logo_orange));
                    break;
                case Priority.HIGH:
                    tvPriority.setTextColor(context.getColor(R.color.logo_gold));
                    ivPriority.setColorFilter(context.getColor(R.color.logo_gold));
                    break;
                case Priority.MEDIUM:
                    tvPriority.setTextColor(context.getColor(R.color.logo_green));
                    ivPriority.setColorFilter(context.getColor(R.color.logo_green));
                    break;
                default:
                    tvPriority.setTextColor(context.getColor(R.color.logo_gold_light));
                    ivPriority.setColorFilter(context.getColor(R.color.logo_gold_light));
                    break;
            }

            // Status with color
            tvStatus.setText(issue.getStatus());
            switch (issue.getStatus()) {
                case Status.PENDING:
                    tvStatus.setTextColor(context.getColor(R.color.logo_orange));
                    ivStatus.setColorFilter(context.getColor(R.color.logo_orange));
                    break;
                case Status.PROCESS:
                    tvStatus.setTextColor(context.getColor(R.color.logo_gold));
                    ivStatus.setColorFilter(context.getColor(R.color.logo_gold));
                    break;
                case Status.RESOLVED:
                    tvStatus.setTextColor(context.getColor(R.color.logo_green));
                    ivStatus.setColorFilter(context.getColor(R.color.logo_green));
                    break;
                case Status.REJECTED:
                    tvStatus.setTextColor(context.getColor(R.color.logo_red));
                    ivStatus.setColorFilter(context.getColor(R.color.logo_red));
                    break;
            }

            // Created Date
            tvCreatedDate.setText(formatTimestamp(issue.getCreatedTimestamp()));

            // Assigned To
            if (issue.getAssignedTo() != null && !issue.getAssignedTo().isEmpty()) {
                tvAssignedTo.setText("Assigned to: " + issue.getAssignedTo());
            } else {
                tvAssignedTo.setText("Not Assigned Yet");
            }

            if (issue.getRejectTimestamp() > 0)
                btnAssign.setVisibility(View.GONE);
            else if ("FOUNDER".equalsIgnoreCase(userType)) {
                if (issue.getWorkAssignTimestamp() == 0)
                    btnAssign.setVisibility(View.VISIBLE);
                else
                    btnAssign.setVisibility(View.GONE);
            }

            // Timeline - Show only if timestamps exist
            if (issue.getWorkAssignTimestamp() > 0) {
                btnAssign.setVisibility(View.GONE);
                tvWorkAssignTime.setText(formatTimestamp(issue.getWorkAssignTimestamp()));
            } else {
                tvWorkAssignTime.setText("Not assigned yet");
            }

            if (issue.getWorkCompleteTimestamp() > 0) {
                tvWorkCompleteTime.setText(formatTimestamp(issue.getWorkCompleteTimestamp()));
            } else {
                tvWorkCompleteTime.setText("Not completed yet");
            }

            if (issue.getSevaMitraApprovedTimestamp() > 0) {
                tvSevaMitraApproveTime.setText(formatTimestamp(issue.getSevaMitraApprovedTimestamp()));
            } else {
                tvSevaMitraApproveTime.setText("Not approved yet");
            }

            if (issue.getOfficerApprovedTimestamp() > 0) {
                tvOfficerApproveTime.setText(formatTimestamp(issue.getOfficerApprovedTimestamp()));
            } else {
                tvOfficerApproveTime.setText("Not approved yet");
            }

            // Resolution Notes
            if (issue.getResolutionNotes() != null && !issue.getResolutionNotes().isEmpty()) {
                tvResolutionNotes.setText(issue.getResolutionNotes());
                resolutionLayout.setVisibility(View.VISIBLE);
            } else {
                resolutionLayout.setVisibility(View.GONE);
            }

            btnTrack.setVisibility(View.VISIBLE);
            if (issue.getWorkAssignTimestamp() > 0)
                btnApprove.setVisibility(View.VISIBLE);

            // Click listeners
            cardView.setOnClickListener(v -> listener.onIssueClick(issue));
            btnAssign.setOnClickListener(v -> listener.onAssignClick(issue));
            btnTrack.setOnClickListener(v -> listener.onTrackClick(issue));
            btnApprove.setOnClickListener(v -> listener.onApproveClick(issue));
        }

        private String formatTimestamp(long timestamp) {
            if (timestamp <= 0) return "N/A";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}