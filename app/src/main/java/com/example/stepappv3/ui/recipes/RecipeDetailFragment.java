package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.stepappv3.R;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.Recipe;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeDetailFragment extends Fragment {

    private RecipeDetailViewModel viewModel;
    private MaterialToolbar toolbar;

    private ChipGroup servingSizeChipGroup;
    private LinearLayout ingredientsContainer, stepsContainer,missingIngredientsContainer;
    private TextView caloriesTextView, fatTextView, sugarTextView, fiberTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);
        initializeViews(view);
        setupToolbar();
        setupChipListener();
        setupObservers();
    }

    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        caloriesTextView = view.findViewById(R.id.calories_textview);
        fatTextView = view.findViewById(R.id.fat_textview);
        sugarTextView = view.findViewById(R.id.sugar_textview);
        fiberTextView = view.findViewById(R.id.fiber_textview);
        servingSizeChipGroup = view.findViewById(R.id.serving_size_chip_group);
        ingredientsContainer = view.findViewById(R.id.ingredients_container);
        stepsContainer = view.findViewById(R.id.steps_container);
        missingIngredientsContainer = view.findViewById(R.id.missing_ingredients_container);
    }

    private void setupToolbar() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    private void setupChipListener() {
        servingSizeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            com.google.android.material.chip.Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                try {
                    int newSize = Integer.parseInt(selectedChip.getText().toString());
                    viewModel.updateServingSize(newSize);
                } catch (NumberFormatException e) {
                }
            }
        });
    }

    private void setupObservers() {

        viewModel.recipe.observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                buildServingSizeChips(recipe.recipe.serving);
                buildBulletedList(ingredientsContainer, recipe.recipe.ingredients);
                buildBulletedList(stepsContainer, recipe.recipe.steps);
                toolbar.setTitle(recipe.recipe.name);
            }
        });

        viewModel.displayedCalories.observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) caloriesTextView.setText(String.format("Calories: %.0f", calories));
        });
        viewModel.displayedFat.observe(getViewLifecycleOwner(), fat -> {
            if (fat != null) fatTextView.setText(String.format("Total Fat: %.1fg", fat));
        });
        viewModel.displayedSugars.observe(getViewLifecycleOwner(), sugar -> {
            if (sugar != null) sugarTextView.setText(String.format("Total Sugars: %.1fg", sugar));
        });
        viewModel.displayedFiber.observe(getViewLifecycleOwner(), fiber -> {
            if (fiber != null) fiberTextView.setText(String.format("Dietary Fiber: %.1fg", fiber));
        });
        viewModel.missingIngredients.observe(getViewLifecycleOwner(), missing -> {
            buildBulletedList(missingIngredientsContainer,missing.toString());
        });

    }



    private void buildServingSizeChips(int maxServings) {
        servingSizeChipGroup.removeAllViews();
        if (maxServings <= 0) return;

        for (int i = 1; i <= maxServings; i++) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
            chip.setText(String.valueOf(i));
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setId(View.generateViewId());
            servingSizeChipGroup.addView(chip);
        }
        if (servingSizeChipGroup.getChildCount() > 0) {
            int originalServingsIndex = maxServings - 1;
            if(servingSizeChipGroup.getChildCount() > originalServingsIndex){
                com.google.android.material.chip.Chip defaultChip = (com.google.android.material.chip.Chip) servingSizeChipGroup.getChildAt(originalServingsIndex);
                if(defaultChip != null) defaultChip.setChecked(true);
            }
        }
    }


    // In RecipeDetailFragment.java

    /**
     * A robust method to parse a raw, bracketed, comma-separated string
     * and build a bulleted list from it.
     */
    private void buildBulletedList(LinearLayout container, String data) {
        container.removeAllViews();
        if (data == null || data.trim().isEmpty()) {
            return;
        }
        String cleanedData = data.replace("[", "").replace("]", "").replace("\"", "");
        if (cleanedData.trim().isEmpty()) {
            return;
        }
        String[] items = cleanedData.split(",");
        for (String item : items) {
            String trimmedItem = item.trim();
            if (!trimmedItem.isEmpty()) {
                TextView textView = new TextView(requireContext());
                textView.setPadding(0, 4, 0, 4);
                textView.setText("• " + trimmedItem);
                textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                container.addView(textView);
            }
        }
    }

}