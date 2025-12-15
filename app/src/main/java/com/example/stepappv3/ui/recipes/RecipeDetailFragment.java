package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.stepappv3.R;
import com.example.stepappv3.database.recipes.Recipe;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class RecipeDetailFragment extends Fragment {

    // 1. Declare member fields for ViewModel and all UI Views
    private RecipeDetailViewModel viewModel;
    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView caloriesTextView, fatTextView, sugarTextView, fiberTextView;
    private TextView ingredientsTextView, stepsTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 2. Inflate the layout
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Get the ViewModel
        viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        // 4. Find all the views
        toolbar = view.findViewById(R.id.toolbar);
        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        caloriesTextView = view.findViewById(R.id.calories_textview);
        fatTextView = view.findViewById(R.id.fat_textview);
        sugarTextView = view.findViewById(R.id.sugar_textview);
        fiberTextView = view.findViewById(R.id.fiber_textview);
        ingredientsTextView = view.findViewById(R.id.ingredients_textview);
        stepsTextView = view.findViewById(R.id.steps_textview);

        // 5. Use helper methods to set up the UI and observers
        setupToolbar();
        setupObservers();
    }

    private void setupToolbar() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        // This single line connects the toolbar to the NavController,
        // automatically handling the back button ('Up' button) and title.
        NavigationUI.setupWithNavController(toolbar, navController);
    }

    private void setupObservers() {
        // 6. Observe the LiveData from the ViewModel
        viewModel.recipe.observe(getViewLifecycleOwner(), recipe -> {
            // Perform a defensive null check
            if (recipe != null) {
                // If the data is valid, call a helper to populate the UI
                bindDataToViews(recipe);
            } else {
                // Handle the case where the recipe ID was invalid or not found.
                // You could show an error message here.
                collapsingToolbar.setTitle("Recipe Not Found");
            }
        });
    }

    /**
     * Helper method to populate all the views with data from the Recipe object.
     * This keeps the observer logic clean.
     */
    private void bindDataToViews(Recipe recipe) {
        // Set the collapsing toolbar title. It will only be visible when collapsed.
        collapsingToolbar.setTitle(recipe.name);

        // Populate the nutritional facts
        caloriesTextView.setText(String.format("Calories: %.0f", recipe.calories));
        fatTextView.setText(String.format("Total Fat: %.1fg", recipe.fat));
        sugarTextView.setText(String.format("Total Sugars: %.1fg", recipe.sugar));
        fiberTextView.setText(String.format("Dietary Fiber: %.1fg", recipe.fiber));

        // Populate the ingredients list
        // A simple replace can make the comma-separated list more readable.
        ingredientsTextView.setText(recipe.ingredients.replace(",", "\n• "));

        // Populate the cooking steps
        stepsTextView.setText(recipe.steps);

        // In a real app, you would use a library like Glide or Picasso here
        // to load an image from a URL into the recipe_image ImageView.
    }
}