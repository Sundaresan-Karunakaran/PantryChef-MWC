package com.example.stepappv3.ui.recipes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.recipes.Recipe;

public class RecipeDetailViewModel extends AndroidViewModel {

    // This public, final LiveData object will be observed by our Fragment.
    public final LiveData<Recipe> recipe;

    private final StepRepository repository;

    /**
     * The constructor is the entry point for this ViewModel.
     * It receives the Application context and the SavedStateHandle, which contains
     * the navigation arguments (our recipeId).
     */
    public RecipeDetailViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        this.repository = new StepRepository(application);

        // 1. Get the recipeId from the navigation arguments.
        // The key "recipeId" MUST exactly match the name defined in mobile_navigation.xml.
        Integer recipeId = savedStateHandle.get("recipeId");

        // 2. Perform a defensive check. If the ID is missing, we cannot proceed.
        // We will fetch a null recipe, and the UI will have to handle this empty state.
        if (recipeId != null) {
            this.recipe = repository.getRecipeById(recipeId);
        } else {
            // If no ID was passed, this is an invalid state. We can use a helper to return
            // an empty LiveData object to prevent crashes in the Fragment.
            // For now, we'll assign the result of a query that will return null.
            this.recipe = repository.getRecipeById(-1); // Assuming -1 is an invalid ID.
        }
    }
}