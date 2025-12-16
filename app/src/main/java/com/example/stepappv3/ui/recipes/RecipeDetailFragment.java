package com.example.stepappv3.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.database.recipes.Recipe;

import com.example.stepappv3.ui.recipes.RecipeDetailViewModel;

public class RecipeDetailFragment extends Fragment {

    private RecipeDetailViewModel mViewModel;

    // UI Bileşenleri
    // Yeni eklenen başlık TextView'ı
    private TextView recipeTitleTextView;

    private TextView ingredientsTextView;
    private TextView instructionsTextView;
    private TextView caloriesTextView;
    private TextView fatTextView;
    private TextView sugarTextView;
    private TextView fiberTextView;

    // YENİ UI Bileşenleri
    private MaterialButton btnCookedYes;
    private MaterialButton btnCookedNo;
    private TextView textCookedStatusMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- ID EŞLEŞTİRMELERİ ---
        recipeTitleTextView = view.findViewById(R.id.recipe_title_textview); // YENİ EŞLEŞTİRME

        ingredientsTextView = view.findViewById(R.id.ingredients_textview);
        instructionsTextView = view.findViewById(R.id.steps_textview);
        caloriesTextView = view.findViewById(R.id.calories_textview);
        fatTextView = view.findViewById(R.id.fat_textview);
        sugarTextView = view.findViewById(R.id.sugar_textview);
        fiberTextView = view.findViewById(R.id.fiber_textview);

        // YENİ BUTONLAR VE DURUM
        btnCookedYes = view.findViewById(R.id.btn_cooked_yes);
        btnCookedNo = view.findViewById(R.id.btn_cooked_no);
        textCookedStatusMessage = view.findViewById(R.id.text_cooked_status_message);

        mViewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        mViewModel.recipe.observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                bindRecipeData(recipe);
            }
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        btnCookedYes.setOnClickListener(v -> {

            showStatus(true);
        });

        btnCookedNo.setOnClickListener(v -> {
            showStatus(false);
        });
    }

    private void showStatus(boolean cooked) {
        if (cooked) {
            textCookedStatusMessage.setText("Marked as cooked!");
            textCookedStatusMessage.setVisibility(View.VISIBLE);
            btnCookedYes.setEnabled(false);
            btnCookedNo.setEnabled(true);
        } else {
            textCookedStatusMessage.setText("Status reset.");
            textCookedStatusMessage.setVisibility(View.VISIBLE);
            btnCookedYes.setEnabled(true);
            btnCookedNo.setEnabled(false);
        }
        Toast.makeText(getContext(), cooked ? "Recipe marked as cooked!" : "Status reset.", Toast.LENGTH_SHORT).show();
    }


    private void bindRecipeData(Recipe recipe) {
        // --- BAŞLIK EKLEME ---
        recipeTitleTextView.setText(recipe.name);

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