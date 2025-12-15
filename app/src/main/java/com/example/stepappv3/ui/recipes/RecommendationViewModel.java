package com.example.stepappv3.ui.recipes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.StepDatabase;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.recommender.RecipeRecommender;
import com.example.stepappv3.recommender.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import java.util.concurrent.Executor;

public class RecommendationViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private final RecipeRecommender recommender;
    private final MediatorLiveData<List<Recommendation>> recommendations = new MediatorLiveData<>();

    private LiveData<List<PantryItem>> pantryItems;
    private LiveData<List<Recipe>> allRecipes;
    private final Executor backgroundExecutor;

    public RecommendationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StepRepository(application);
        this.recommender = new RecipeRecommender();
        this.backgroundExecutor = StepDatabase.databaseWriteExecutor;


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Get the two LiveData sources from the repository.
        this.pantryItems = repository.getAllPantryItemsUser(userId);
        this.allRecipes = repository.getAllRecipes();

        // 2. Add both sources to the Mediator.
        recommendations.addSource(pantryItems, items -> calculateRecommendations(items, allRecipes.getValue()));
        recommendations.addSource(allRecipes, recipes -> calculateRecommendations(pantryItems.getValue(), recipes));
    }

    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }

    private void calculateRecommendations(List<PantryItem> currentPantry, List<Recipe> currentRecipes) {
        // Only run the engine when both data sources have returned non-null data.
        if (currentPantry != null && currentRecipes != null) {
            backgroundExecutor.execute(() -> {
                List<Recommendation> result = recommender.recommend(currentPantry, currentRecipes);
                // Use postValue() to safely send the result back to the main UI thread.
                recommendations.postValue(result);
            });
        }
    }
}