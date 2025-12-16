package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
// Toolbar başlığını değiştirmek için import ekledik
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.database.recipes.Recipe;

public class RecipeDetailFragment extends Fragment {

    private RecipeDetailViewModel mViewModel;
    // ID'yi ViewModel otomatik aldığı için buradaki int recipeId'ye aslında gerek yoktu ama dursun.

    // UI Bileşenleri
    private CollapsingToolbarLayout collapsingToolbar; // Başlık için
    private TextView ingredientsTextView;
    private TextView instructionsTextView;
    private TextView caloriesTextView;
    private TextView fatTextView;
    private TextView sugarTextView;
    private TextView fiberTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- DÜZELTME 1: ID'LER XML İLE EŞLEŞTİRİLDİ ---

        // İsim için ayrı bir TextView yok, CollapsingToolbarLayout başlığını kullanacağız
        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);

        ingredientsTextView = view.findViewById(R.id.ingredients_textview); // recipe_detail_ingredients YERİNE
        instructionsTextView = view.findViewById(R.id.steps_textview);      // recipe_detail_instructions YERİNE
        caloriesTextView = view.findViewById(R.id.calories_textview);       // recipe_detail_calories YERİNE
        fatTextView = view.findViewById(R.id.fat_textview);                 // recipe_detail_fat YERİNE
        sugarTextView = view.findViewById(R.id.sugar_textview);             // recipe_detail_sugar YERİNE
        fiberTextView = view.findViewById(R.id.fiber_textview);             // recipe_detail_fiber YERİNE

        mViewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        // --- DÜZELTME 2: VIEWMODEL KULLANIMI ---
        // mViewModel.getRecipe(id) YERİNE mViewModel.recipe değişkenini izliyoruz.
        // ViewModel, ID'yi "SavedStateHandle" üzerinden otomatik aldı.
        mViewModel.recipe.observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                bindRecipeData(recipe);
            }
        });
    }

    private void bindRecipeData(Recipe recipe) {
        // İsmi Toolbar başlığına yazıyoruz
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(recipe.name);
        }

        if (recipe.ingredients != null) {
            ingredientsTextView.setText(recipe.ingredients);
        } else {
            ingredientsTextView.setText("No ingredients available.");
        }

        if (recipe.steps != null) {
            String cleanSteps = recipe.steps.replace("['", "").replace("']", "").replace("', '", "\n\n");
            instructionsTextView.setText(cleanSteps);
        } else {
            instructionsTextView.setText("No instructions available.");
        }

        caloriesTextView.setText(String.format("Calories: %.0f kcal", recipe.calories));
        fatTextView.setText(String.format("Total Fat: %.1fg", recipe.fat));
        sugarTextView.setText(String.format("Total Sugars: %.1fg", recipe.sugar));
        fiberTextView.setText(String.format("Dietary Fiber: %.1fg", recipe.fiber));
    }
}