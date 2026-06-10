package com.rituraj.sevamitra.ui.dashboard.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.adapters.WorkerAdapter;
import com.rituraj.sevamitra.models.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WorkerListFragment extends Fragment {

    private RecyclerView rvWorkers;
    private WorkerAdapter workerAdapter;
    private List<UserData> workerList;
    private List<UserData> filteredWorkerList;

    private EditText etSearch;
    private Spinner spinnerSortBy;
    private ChipGroup chipGroupCategory;
    private TextView tvNoResults;
    private CardView cardFilters;

    private String currentSortBy = "rating";
    private String selectedCategory = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worker_list, container, false);

        initViews(view);
        loadWorkersData();
        setupSearch();
        setupSorting();
        setupCategoryFilters();

        return view;
    }

    private void initViews(View view) {
        rvWorkers = view.findViewById(R.id.rvWorkers);
        etSearch = view.findViewById(R.id.etSearch);
        spinnerSortBy = view.findViewById(R.id.spinnerSortBy);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        cardFilters = view.findViewById(R.id.cardFilters);

        rvWorkers.setLayoutManager(new LinearLayoutManager(getContext()));
        workerList = new ArrayList<>();
        filteredWorkerList = new ArrayList<>();
    }

    private void loadWorkersData() {
//        // Sample data - Replace with API call
//        workerList.add(new WorkerModel("1", "Rajesh Kumar", "rajesh@email.com", "9876543210",
//                "Plumber", "5 years", "₹500", 4.5, true, "Lucknow"));
//        workerList.add(new WorkerModel("2", "Suresh Sharma", "suresh@email.com", "9876543211",
//                "Electrician", "8 years", "₹600", 4.8, true, "Kanpur"));
//        workerList.add(new WorkerModel("3", "Amit Verma", "amit@email.com", "9876543212",
//                "AC Technician", "3 years", "₹700", 4.2, false, "Lucknow"));
//        workerList.add(new WorkerModel("4", "Rahul Singh", "rahul@email.com", "9876543213",
//                "CCTV Technician", "4 years", "₹550", 4.6, true, "Varanasi"));
//        workerList.add(new WorkerModel("5", "Vikash Patel", "vikash@email.com", "9876543214",
//                "Carpenter", "6 years", "₹450", 4.3, true, "Lucknow"));
//        workerList.add(new WorkerModel("6", "Pankaj Mishra", "pankaj@email.com", "9876543215",
//                "Painter", "2 years", "₹400", 4.0, true, "Agra"));
//        workerList.add(new WorkerModel("7", "Manoj Gupta", "manoj@email.com", "9876543216",
//                "Mechanic", "7 years", "₹650", 4.7, false, "Lucknow"));
//        workerList.add(new WorkerModel("8", "Alok Yadav", "alok@email.com", "9876543217",
//                "Plumber", "4 years", "₹520", 4.4, true, "Prayagraj"));

        filteredWorkerList.addAll(workerList);
        workerAdapter = new WorkerAdapter(filteredWorkerList, getContext());
        rvWorkers.setAdapter(workerAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSorting() {
        String[] sortOptions = {"Rating (High to Low)", "Price (Low to High)",
                "Price (High to Low)", "Experience (High to Low)",
                "Name (A-Z)", "Name (Z-A)"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, sortOptions);
        spinnerSortBy.setAdapter(adapter);

        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sortByRating();
                        break;
                    case 1:
                        sortByPriceLowToHigh();
                        break;
                    case 2:
                        sortByPriceHighToLow();
                        break;
                    case 3:
                        sortByExperience();
                        break;
                    case 4:
                        sortByNameAZ();
                        break;
                    case 5:
                        sortByNameZA();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCategoryFilters() {
        // Add chips dynamically
        String[] categories = {"All", "Plumber", "Electrician", "AC Technician",
                "CCTV Technician", "Carpenter", "Painter", "Mechanic"};

        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(getResources().getColor(R.color.logo_white));

            if (category.equals("All")) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category;
                    filterWorkers(etSearch.getText().toString());
                }
            });

            chipGroupCategory.addView(chip);
        }
    }

    private void filterWorkers(String searchText) {
        filteredWorkerList.clear();

        for (UserData worker : workerList) {
            // Category filter
            if (!selectedCategory.equals("All") && !worker.getSpecialization().equals(selectedCategory)) {
                continue;
            }

            // Search filter
            if (searchText.isEmpty() ||
                    worker.getFullName().toLowerCase().contains(searchText.toLowerCase()) ||
                    worker.getSpecialization().toLowerCase().contains(searchText.toLowerCase()) ||
                    worker.getAddress().toLowerCase().contains(searchText.toLowerCase())) {
                filteredWorkerList.add(worker);
            }
        }

        // Apply current sorting
        applyCurrentSorting();

        workerAdapter.notifyDataSetChanged();

        // Show/hide no results message
        if (filteredWorkerList.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvWorkers.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvWorkers.setVisibility(View.VISIBLE);
        }
    }

    private void applyCurrentSorting() {
        if (currentSortBy.equals("rating")) {
            sortByRating();
        } else if (currentSortBy.equals("price_low")) {
            sortByPriceLowToHigh();
        } else if (currentSortBy.equals("price_high")) {
            sortByPriceHighToLow();
        } else if (currentSortBy.equals("experience")) {
            sortByExperience();
        }
    }

    private void sortByRating() {
        currentSortBy = "rating";
//        Collections.sort(filteredWorkerList, (w1, w2) ->
//                Double.compare(w2.getRating(), w1.getRating()));
        workerAdapter.notifyDataSetChanged();
    }

    private void sortByPriceLowToHigh() {
        currentSortBy = "price_low";
        Collections.sort(filteredWorkerList, (w1, w2) -> {
            int price1 = Integer.parseInt(w1.getHourlyRate().replace("₹", ""));
            int price2 = Integer.parseInt(w2.getHourlyRate().replace("₹", ""));
            return Integer.compare(price1, price2);
        });
        workerAdapter.notifyDataSetChanged();
    }

    private void sortByPriceHighToLow() {
        currentSortBy = "price_high";
        Collections.sort(filteredWorkerList, (w1, w2) -> {
            int price1 = Integer.parseInt(w1.getHourlyRate().replace("₹", ""));
            int price2 = Integer.parseInt(w2.getHourlyRate().replace("₹", ""));
            return Integer.compare(price2, price1);
        });
        workerAdapter.notifyDataSetChanged();
    }

    private void sortByExperience() {
        currentSortBy = "experience";
        Collections.sort(filteredWorkerList, (w1, w2) -> {
            int exp1 = Integer.parseInt(w1.getExperience().replace(" years", ""));
            int exp2 = Integer.parseInt(w2.getExperience().replace(" years", ""));
            return Integer.compare(exp2, exp1);
        });
        workerAdapter.notifyDataSetChanged();
    }

    private void sortByNameAZ() {
        Collections.sort(filteredWorkerList, (w1, w2) ->
                w1.getFullName().compareTo(w2.getFullName()));
        workerAdapter.notifyDataSetChanged();
    }

    private void sortByNameZA() {
        Collections.sort(filteredWorkerList, (w1, w2) ->
                w2.getFullName().compareTo(w1.getFullName()));
        workerAdapter.notifyDataSetChanged();
    }
}