package com.rituraj.sevamitra.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;

import java.util.ArrayList;
import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> implements Filterable {

    private List<UserData> vendorList;
    private List<UserData> vendorListFull;
    private OnVendorClickListener listener;

    public interface OnVendorClickListener {
        void onVendorClick(UserData vendor);
        void onContactClick(UserData vendor);
    }

    public VendorAdapter(List<UserData> vendorList, OnVendorClickListener listener) {
        this.vendorList = vendorList;
        this.vendorListFull = new ArrayList<>(vendorList);
        this.listener = listener;
    }

    public void updateData(List<UserData> newList) {
        this.vendorList = new ArrayList<>(newList);
        this.vendorListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VendorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorViewHolder holder, int position) {
        UserData vendor = vendorList.get(position);
        holder.bind(vendor);
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    @Override
    public Filter getFilter() {
        return vendorFilter;
    }

    private final Filter vendorFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserData> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(vendorListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (UserData item : vendorListFull) {
                    boolean matchesName = item.getFullName() != null && item.getFullName().toLowerCase().contains(filterPattern);
                    boolean matchesPhone = item.getPhone() != null && item.getPhone().contains(filterPattern);
                    boolean matchesCity = item.getCity() != null && item.getCity().toLowerCase().contains(filterPattern);
                    boolean matchesCompany = item.getCompanyName() != null && item.getCompanyName().toLowerCase().contains(filterPattern);
                    boolean matchesDept = item.getDepartment() != null && item.getDepartment().toLowerCase().contains(filterPattern);

                    if (matchesName || matchesPhone || matchesCity || matchesCompany || matchesDept) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            vendorList.clear();
            if (results.values != null) {
                vendorList.addAll((List<UserData>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    class VendorViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivProfile, ivStatus;
        private TextView tvName, tvCompanyName, tvDepartment;
        private TextView tvEmail, tvPhone, tvLocation;
        private MaterialButton btnContact, btnSelectVendor;

        public VendorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnSelectVendor = itemView.findViewById(R.id.btnSelectVendor);
        }

        public void bind(UserData vendor) {
            tvName.setText(vendor.getFullName() != null && !vendor.getFullName().isEmpty() ? vendor.getFullName() : "Vendor");

            if (vendor.getCompanyName() != null && !vendor.getCompanyName().isEmpty()) {
                tvCompanyName.setText(vendor.getCompanyName());
                tvCompanyName.setVisibility(View.VISIBLE);
            } else {
                tvCompanyName.setVisibility(View.GONE);
            }

            tvDepartment.setText(vendor.getDepartment() != null && !vendor.getDepartment().isEmpty() ? vendor.getDepartment() : "General Vendor");

            tvEmail.setText(vendor.getEmail() != null && !vendor.getEmail().isEmpty() ? vendor.getEmail() : "Email not available");
            tvPhone.setText(vendor.getPhone() != null && !vendor.getPhone().isEmpty() ? vendor.getPhone() : "Phone not available");

            String locationStr = "";
            if (vendor.getCity() != null && !vendor.getCity().isEmpty()) {
                locationStr += vendor.getCity();
            }
            if (vendor.getState() != null && !vendor.getState().isEmpty()) {
                if (!locationStr.isEmpty()) locationStr += ", ";
                locationStr += vendor.getState();
            }
            if (locationStr.isEmpty() && vendor.getAddress() != null && !vendor.getAddress().isEmpty()) {
                locationStr = vendor.getAddress();
            }
            tvLocation.setText(!locationStr.isEmpty() ? locationStr : "Location not provided");

            if (vendor.getProfileUrl() != null && !vendor.getProfileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(vendor.getProfileUrl())
                        .placeholder(R.drawable.ic_worker)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_worker);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVendorClick(vendor);
                }
            });

            btnSelectVendor.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVendorClick(vendor);
                }
            });

            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(vendor);
                } else if (vendor.getPhone() != null && !vendor.getPhone().isEmpty()) {
                    Context context = itemView.getContext();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vendor.getPhone()));
                    context.startActivity(callIntent);
                } else {
                    Toast.makeText(itemView.getContext(), "Contact number not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
