package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;
import com.example.stepappv3.recommender.Recommendation;
import com.example.stepappv3.ui.recipes.RecommendationListAdapter;
import com.example.stepappv3.ui.recipes.RecommendationViewModel; // Adjust if your package is different

public class RecommendationFragment extends Fragment implements RecommendationListAdapter.OnRecipeClickListener {

    private RecommendationViewModel viewModel;
    private RecyclerView recyclerView;
    private RecommendationListAdapter adapter;
    private View emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get an instance of the ViewModel.
        viewModel = new ViewModelProvider(this).get(RecommendationViewModel.class);

        // Find views.
        recyclerView = view.findViewById(R.id.recommendations_recyclerview);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        // Setup RecyclerView.
        setupRecyclerView();

        // Setup the observer to listen for data.
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RecommendationListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        // This is the core of the UI logic.
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            if (recommendations == null || recommendations.isEmpty()) {
                // If there are no recommendations, show the empty state message.
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // If we have recommendations, show the list and submit the data to the adapter.
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(recommendations);
            }
        });
    }

    @Override
    public void onRecipeClick(Recommendation recommendation) {

        RecommendationFragmentDirections.ActionRecommendationsFragmentToRecipeDetailFragment action =
                RecommendationFragmentDirections.actionRecommendationsFragmentToRecipeDetailFragment(
                        recommendation.recipe.recipeId,
                        recommendation.recipe.name
                );

        // Find the NavController and execute the navigation.
        androidx.navigation.Navigation.findNavController(requireView()).navigate(action);
    }
}