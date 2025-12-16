package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
// ... (Diğer importlar)
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // ProgressBar importu
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
// --- BURASI YENİ EKLENDİ ---
import android.view.LayoutInflater;
// --------------------------
import android.view.View;
import android.view.ViewGroup;
import com.example.stepappv3.R;
import com.example.stepappv3.recommender.Recommendation;
import com.example.stepappv3.ui.recipes.RecommendationListAdapter;
import com.example.stepappv3.ui.recipes.RecommendationViewModel;

public class RecommendationFragment extends Fragment implements RecommendationListAdapter.OnRecipeClickListener {

    private RecommendationViewModel viewModel;
    private RecyclerView recyclerView;

    private RecommendationListAdapter adapter; // DÜZELTME: Adapter tanımlandı
    private View emptyStateLayout;
    private ProgressBar loadingIndicator; // DÜZELTME: Loading Indicator tanımlandı

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RecommendationViewModel.class);

        // Find views.
        recyclerView = view.findViewById(R.id.recommendations_recyclerview);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        loadingIndicator = view.findViewById(R.id.loading_indicator); // Eşleştirme yapıldı

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RecommendationListAdapter(this); // Şimdi adapter tanımlı
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter); // Şimdi adapter tanımlı
    }

    private void setupObservers() {
        // 1. Yükleme Durumunu Gözlemle
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingIndicator.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.GONE);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                // Yükleme bittiğinde ve liste boşsa, boş durumu gösteririz.
                if (viewModel.getRecommendations().getValue() == null || viewModel.getRecommendations().getValue().isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        // 2. Tavsiyeleri Gözlemle
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            if (recommendations == null || recommendations.isEmpty()) {
                if (Boolean.FALSE.equals(viewModel.isLoading.getValue())) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(recommendations); // Şimdi adapter tanımlı
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

        androidx.navigation.Navigation.findNavController(requireView()).navigate(action);
    }
}