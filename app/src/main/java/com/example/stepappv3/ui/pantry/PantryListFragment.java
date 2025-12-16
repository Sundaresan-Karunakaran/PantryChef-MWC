package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stepappv3.R;


public class PantryListFragment extends Fragment implements PantryListAdapter.OnEditItemClickListener {

    private PantryListViewModel viewModel;
    private RecyclerView recyclerView;
    private PantryListAdapter adapter;
    private TextView emptyListTextView;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get an instance of the ViewModel for this screen
        viewModel = new ViewModelProvider(this).get(PantryListViewModel.class);

        // Find all the UI elements
        recyclerView = view.findViewById(R.id.pantry_item_recyclerview);
        emptyListTextView = view.findViewById(R.id.empty_list_textview);
        toolbar = view.findViewById(R.id.toolbar);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
    }

    private void setupToolbar() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        // This line automatically sets up the toolbar title and back button
        NavigationUI.setupWithNavController(toolbar, navController);
    }

    private void setupRecyclerView() {
        adapter = new PantryListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe the list of pantry items from the ViewModel
        viewModel.pantryItems.observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                // If there are items, show the RecyclerView and submit the list to the adapter
                recyclerView.setVisibility(View.VISIBLE);
                emptyListTextView.setVisibility(View.GONE);
                adapter.submitList(items);
            } else {
                // If the list is empty, hide the RecyclerView and show the "empty state" text
                recyclerView.setVisibility(View.GONE);
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditClicked(com.example.stepappv3.database.pantry.PantryItem item) {

        PantryAddItemManualFragment dialogFragment = new PantryAddItemManualFragment();
        Bundle args = new Bundle();
        args.putInt("ITEM_ID", item.id);
        args.putString("ITEM_NAME", item.name);
        args.putInt("ITEM_QUANTITY", item.quantity);
        args.putString("ITEM_UNIT", item.unit);
        args.putString("ITEM_CATEGORY", item.category);
        dialogFragment.setArguments(args);

        // Show the dialog.
        dialogFragment.show(getChildFragmentManager(), "EditPantryItemDialog");
    }
}