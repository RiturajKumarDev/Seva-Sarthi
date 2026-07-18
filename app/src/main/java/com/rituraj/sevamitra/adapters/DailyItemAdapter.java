package com.rituraj.sevamitra.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.DailyItemModel;
import com.rituraj.sevamitra.models.Status;

import java.util.List;

public class DailyItemAdapter extends RecyclerView.Adapter<DailyItemAdapter.ViewHolder> {

    private List<DailyItemModel> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DailyItemModel item);

        void onDeleteClick(DailyItemModel item);
    }

    public DailyItemAdapter(List<DailyItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<DailyItemModel> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DailyItemModel item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvItemName, tvCategory, tvQuantity, tvTotal;
        private TextView tvDate, tvStatus, tvSupplier;
        private ImageView ivStatus, ivDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSupplier = itemView.findViewById(R.id.tvSupplier);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(DailyItemModel item) {
            tvItemName.setText(item.getItemName());
            tvCategory.setText(item.getCategory());
            tvQuantity.setText(item.getQuantity() + " " + item.getUnit());
            tvDate.setText(item.getDate() + " " + item.getTime());
            tvTotal.setText(item.getCreatedBy());
            tvSupplier.setText("Supplier: " + (item.getSupplierDetail() != null ? item.getSupplierDetail() : "N/A"));
            tvStatus.setText(item.getStatus());

            // Set status color
            switch (item.getStatus()) {
                case Status.ACCEPTED:
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_green));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_green));
                    break;
                case Status.PENDING:
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_orange));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_orange));
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.logo_red));
                    ivStatus.setColorFilter(itemView.getContext().getColor(R.color.logo_red));
                    break;
            }

            cardView.setOnClickListener(v -> listener.onItemClick(item));
            ivDelete.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }
}