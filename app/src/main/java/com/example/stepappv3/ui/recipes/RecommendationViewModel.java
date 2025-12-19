package com.example.stepappv3.ui.recipes;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.StepDatabase;
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.database.recipes.RecipeIngredientJoinDao;
import com.example.stepappv3.database.recipes.RecipeWithIngredients;
import com.example.stepappv3.recommender.RecipeRecommender;
import com.example.stepappv3.recommender.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RecommendationViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private final RecipeIngredientJoinDao joinDao;
    private final RecipeRecommender recommender;
    private final MediatorLiveData<List<Recommendation>> recommendations = new MediatorLiveData<>();

    private LiveData<List<PantryItemDisplay>> pantryItems;

    private final Executor backgroundExecutor;

    private final AtomicBoolean isCalculating = new AtomicBoolean(false);
    private List<PantryItemDisplay> lastPantryState = new ArrayList<>();
    private final MutableLiveData<Boolean> _isLoading = new MediatorLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _loadingText = new MutableLiveData<>();
    public final LiveData<String> loadingText = _loadingText;
    private final Handler textUpdateHandler = new Handler(Looper.getMainLooper());
    private int loadingTextIndex = 0;
    private final List<String> loadingMessages = Arrays.asList(
            "Checking pantry ...",
            "Rats found ...",
            "Sorry meant rats, can't find items in the pantry...",
            "Almost there..."
    );





    public RecommendationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StepRepository(application);
        this.joinDao = StepDatabase.getDatabase(application).recipeIngredientJoinDao();
        this.backgroundExecutor = StepDatabase.databaseWriteExecutor;
        this.recommender = new RecipeRecommender();


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        this.pantryItems = repository.getAllPantryItemsUser(userId);
        recommendations.addSource(pantryItems, pantryItems -> {
            calculateRecommendations(pantryItems);
        });

    }
    private final Runnable textUpdater = new Runnable() {
        @Override
        public void run() {
            _loadingText.setValue(loadingMessages.get(loadingTextIndex));
            loadingTextIndex = (loadingTextIndex + 1) % loadingMessages.size();
            textUpdateHandler.postDelayed(this, 2500);
        }
    };

    private void startLoadingTextUpdates() {
        textUpdateHandler.post(textUpdater);
    }

    private void stopLoadingTextUpdates() {
        textUpdateHandler.removeCallbacks(textUpdater);
    }

    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }

    private void calculateRecommendations(List<PantryItemDisplay> currentPantry) {

        if (currentPantry == null) return;
        if (Objects.equals(lastPantryState,currentPantry)) return;
        this.lastPantryState = currentPantry;
        if(!isCalculating.compareAndSet(false,true)) return;
        _isLoading.postValue(true);

        startLoadingTextUpdates();
        backgroundExecutor.execute(() -> {
            try {
                Set<Integer> pantryIngredientIds = currentPantry.stream()
                        .map(pantryItem -> pantryItem.masterIngredientId)
                        .collect(Collectors.toSet());
                List<RecipeWithIngredients> allRecipes = repository.getRecipeWithIngredients();
                if (pantryIngredientIds.isEmpty() || allRecipes.isEmpty()) {
                    recommendations.postValue(new ArrayList<>());
                    return;
                }
                List<Recommendation> result = recommender.recommend(pantryIngredientIds, allRecipes);

                recommendations.postValue(result);
            } finally {
                isCalculating.set(false);
                _isLoading.postValue(false);
                stopLoadingTextUpdates();
            }
        });


    }
    @Override
    protected void onCleared() {
        super.onCleared();
        stopLoadingTextUpdates();
    }
}