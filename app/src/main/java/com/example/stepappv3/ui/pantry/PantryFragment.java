package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class PantryFragment extends Fragment {

    private PantryViewModel pantryViewModel;
    private RecyclerView categoryRecyclerView;
    private PantryCategoryAdapter adapter;
    private LinearLayout emptyPantryView;
    private FloatingActionButton addPantryItemFab;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get the ViewModel
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        // 2. Find all the views
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        //emptyPantryView = view.findViewById(R.id.empty_pantry_view);
        addPantryItemFab = view.findViewById(R.id.add_pantry_item_fab);

        // 3. Set up the RecyclerView
        setupRecyclerView();

        // 4. Set up the observer to listen for data changes
        setupObservers();

        // 5. Set up any click listeners
        setupClickListeners();
    }

    private void setupRecyclerView() {
        // The LayoutManager is already set in XML, but it's good practice to be explicit.
        // The span count of 2 creates our 2-column grid.
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize the adapter with an empty list. It will be updated by the observer.
        adapter = new PantryCategoryAdapter(new ArrayList<>());
        categoryRecyclerView.setAdapter(adapter);
    }

    // In PantryFragment.java

    private void setupObservers() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            // This code runs whenever the ViewModel provides a new list.

            // The logic for the empty state is still perfect.
            if (categories == null || categories.isEmpty()) {
                categoryRecyclerView.setVisibility(View.GONE);
                // You'll need to uncomment this line and make sure emptyPantryView is initialized
                // emptyPantryView.setVisibility(View.VISIBLE);
            } else {
                categoryRecyclerView.setVisibility(View.VISIBLE);
                // emptyPantryView.setVisibility(View.GONE);

                // THE FIX: Use our new, efficient update method.
                adapter.updateData(categories);
            }
        });
    }

    private void setupClickListeners() {
        addPantryItemFab.setOnClickListener(v -> {
            // TODO: Navigate to a new screen to add a pantry item.
            Toast.makeText(getContext(), "Add new item clicked!", Toast.LENGTH_SHORT).show();
        });
    }
}