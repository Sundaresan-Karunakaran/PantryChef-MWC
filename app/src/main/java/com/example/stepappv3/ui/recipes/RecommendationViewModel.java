package com.example.stepappv3.ui.recipes;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.StepDatabase;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.recommender.RecipeRecommender;
import com.example.stepappv3.recommender.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class RecommendationViewModel extends AndroidViewModel {

    private static final String TAG = "RecommendationViewModel"; // It's good practice to use a TAG for logging.

    private final StepRepository repository;
    private final RecipeRecommender recommender;

    // The final list of recommendations that the UI will observe.
    private final MediatorLiveData<List<Recommendation>> recommendations = new MediatorLiveData<>();

    // The sources of data for our recommendations.
    private final LiveData<List<PantryItem>> pantryItems;
    private final LiveData<List<RecipeIngredientInfo>> allRecipes;

    public RecommendationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StepRepository(application);
        this.recommender = new RecipeRecommender();

        // It's safer to check for a null user. If the user isn't logged in,
        // the app could crash here.
        String userId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 1. Initialize the LiveData sources from the repository.
        this.pantryItems = repository.getAllPantryItemsUser(userId);
        this.allRecipes = repository.getAllRecipes();

        // 2. Set up the mediator to listen to the sources. Call this method ONLY ONCE.
        setupRecommendationMediator();
    }

    /**
     * Exposes the final, combined LiveData to the UI (Fragment/Activity).
     * The UI should observe this to get the list of recommendations.
     */
    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }

    /**
     * Configures the MediatorLiveData by adding the data sources it needs to observe.
     * This is the central point for orchestrating our data streams.
     */
    private void setupRecommendationMediator() {
        // Source 1: Pantry Items
        recommendations.addSource(pantryItems, items -> {
            Log.d(TAG, "Pantry items have been updated. Triggering calculation.");
            calculateRecommendations();
        });

        // Source 2: All Recipes
        recommendations.addSource(allRecipes, recipes -> {
            Log.d(TAG, "Recipes list has been updated. Triggering calculation.");
            calculateRecommendations();
        });
    }

    /**
     * This method is triggered whenever one of the source LiveData objects changes.
     * It retrieves the latest data from both sources and, if both are ready,
     * runs the recommendation logic on a background thread.
     */
    private void calculateRecommendations() {
        // Get the current values. These might be null if the data hasn't loaded yet.
        List<PantryItem> currentPantryItems = pantryItems.getValue();
        List<RecipeIngredientInfo> currentRecipes = allRecipes.getValue();

        // Guard clause: Only proceed if both lists have been populated.
        // This is the core of the solution to the race condition.
        if (currentPantryItems == null || currentRecipes == null) {
            Log.d(TAG, "Waiting for all data sources to be loaded...");
            return; // Exit the method; we'll be called again when the other data source loads.
        }

        Log.d(TAG, "Both data sources are ready. Calculating recommendations on a background thread.");

        // Perform the potentially long-running recommendation logic off the main thread.
        StepDatabase.databaseWriteExecutor.execute(() -> {
            List<Recommendation> result = recommender.recommend(currentPantryItems, currentRecipes);

            // Use postValue to safely send the result back to the main thread.
            // This will notify any observers (our UI) of the new data.
            recommendations.postValue(result);
        });
    }
}
