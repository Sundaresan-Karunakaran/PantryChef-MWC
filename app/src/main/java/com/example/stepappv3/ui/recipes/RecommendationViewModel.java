package com.example.stepappv3.ui.recipes;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.StepDatabase; // Executor için eklendi
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.recommender.RecipeRecommender;
import com.example.stepappv3.recommender.Recommendation;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class RecommendationViewModel extends AndroidViewModel {

    private static final String TAG = "RecommendationViewModel";

    private final StepRepository repository;
    private final RecipeRecommender recommender;

    // --- Hesaplama için ayrı sabit iş parçacığı havuzu ---
    private static final ExecutorService recommendationExecutor = Executors.newFixedThreadPool(4);

    private final MediatorLiveData<List<Recommendation>> recommendations = new MediatorLiveData<>();

    // YENİ EKLENEN LIVE DATA
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    // --------------------------------

    private final LiveData<List<PantryItem>> pantryItems;

    // Statik tarif verilerini tutacak alan
    private List<RecipeIngredientInfo> staticAllRecipes = null;
    private List<PantryItem> lastPantryItems = null;

    public RecommendationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StepRepository(application);
        this.recommender = new RecipeRecommender();

        String userId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        this.pantryItems = repository.getAllPantryItemsUser(userId);

        loadAllRecipesOnce();

        setupRecommendationMediator();
    }

// ... (Constructor ve diğer alanlar aynı kalacak)

    private void loadAllRecipesOnce() {
        _isLoading.setValue(true); // Yükleme başladı

        // DB okuma işlemini Room'un Executor'ında yap
        StepDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // DÜZELTME: Repository'deki yeni public metodu kullanıyoruz.
                staticAllRecipes = repository.getAllRecipesSynchronous();
                Log.d(TAG, "Static recipes loaded into ViewModel cache: " + staticAllRecipes.size());

                // Veri yüklendiği için tavsiye hesaplamasını tetikle
                calculateRecommendations();

            } catch (Exception e) {
                Log.e(TAG, "FATAL: Failed to load static recipes from DB.", e);
                _isLoading.postValue(false);
                recommendations.postValue(new ArrayList<>());
            }
        });
    }
    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }

    private void setupRecommendationMediator() {
        // Sadece Pantry LiveData'sını izliyoruz
        recommendations.addSource(pantryItems, items -> {
            Log.d(TAG, "Pantry items have been updated. Triggering calculation.");
            lastPantryItems = items;
            calculateRecommendations();
        });
    }

    private void calculateRecommendations() {
        // Statik tarif listesi yüklenmediyse bekle
        if (staticAllRecipes == null || lastPantryItems == null) {
            Log.d(TAG, "Waiting for all data sources to be loaded...");
            return;
        }

        // Hata kontrolü
        if (staticAllRecipes.isEmpty()) {
            Log.d(TAG, "Recipes list is empty. Cannot calculate recommendations.");
            recommendations.postValue(new ArrayList<>());
            _isLoading.postValue(false);
            return;
        }

        _isLoading.postValue(true); // Hesaplama başladığını bildir

        // Hesaplamayı ayrı Executor'a gönderiyoruz.
        recommendationExecutor.execute(() -> {
            Log.d(TAG, "Calculating recommendations on a dedicated background thread...");

            List<Recommendation> result = recommender.recommend(lastPantryItems, staticAllRecipes);

            // Sonucu ana iş parçacığına güvenli bir şekilde gönder.
            recommendations.postValue(result);
            _isLoading.postValue(false); // Hesaplama bitti
        });
    }
}