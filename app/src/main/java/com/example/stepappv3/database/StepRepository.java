package com.example.stepappv3.database;

import android.app.Application;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Adım veritabanı sınıfları
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;

// Kiler veritabanı sınıfları
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;

// Tarif veritabanı sınıfları
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDao;

// DÜZELTİLEN KISIM: UserProfile doğru paketten çekiliyor (profile -> user)
import com.example.stepappv3.database.user.UserProfile;

// Firebase
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StepRepository {

    private final StepDao stepDao;
    private final PantryDao pantryDao;
    private final RecipeDao recipeDao;
    private final FirebaseFirestore firestore;

    public StepRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        stepDao = db.stepDao();
        pantryDao = db.pantryDao();
        recipeDao = db.recipeDao(); // Tarif DAO'su (Eğer StepDatabase içinde tanımlıysa)

        firestore = FirebaseFirestore.getInstance();
    }

    // ==========================================
    // 1. KULLANICI PROFİLİ (FIREBASE FIRESTORE)
    // ==========================================

    public void saveUserProfile(UserProfile profile) {
        if (profile.userId != null) {
            firestore.collection("users").document(profile.userId).set(profile);
        }
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        MutableLiveData<UserProfile> profileData = new MutableLiveData<>();

        firestore.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        profileData.setValue(null);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        profileData.setValue(profile);
                    } else {
                        profileData.setValue(null);
                    }
                });

        return profileData;
    }

    public void updateUserField(String userId, String fieldName, Object value) {
        if (userId != null) {
            firestore.collection("users").document(userId)
                    .update(fieldName, value);
        }
    }

    // ==========================================
    // 2. ADIM (STEP) METODLARI (ROOM)
    // ==========================================

    public LiveData<Integer> getDailyStepsUser(long sinceTimestamp, String userId){
        return stepDao.getStepsSinceLiveDataUser(sinceTimestamp, userId);
    }

    public void getStepsSinceUser(long sinceTimestamp, String userId, final OnDataFetchedCallback<Integer> callback) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            final int total = stepDao.getStepsSinceUser(sinceTimestamp, userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> callback.onDataFetched(total));
        });
    }

    public void getTotalStepsAsyncUser(String userId, final OnDataFetchedCallback<Integer> callback){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            final int total = stepDao.getTotalStepsAsyncUser(userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> callback.onDataFetched(total));
        });
    }

    public void getStepsRangeUser(long startTime, long endTime, String userId, final OnDataFetchedCallback<List<Step>> callback) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            List<Step> steps = stepDao.getStepsInRangeUser(startTime, endTime, userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> callback.onDataFetched(steps));
        });
    }

    public void insert(Step step){
        StepDatabase.databaseWriteExecutor.execute(() -> stepDao.insert(step));
    }

    public void deleteAllUser(String userId){
        StepDatabase.databaseWriteExecutor.execute(() -> stepDao.deleteAllUser(userId));
    }

    // ==========================================
    // 3. KİLER (PANTRY) METODLARI (ROOM)
    // ==========================================

    public void insertPantryItem(PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(() -> pantryDao.insert(item));
    }

    public void updatePantryItem(PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(() -> pantryDao.update(item));
    }

    public LiveData<List<PantryItem>> getItemsByCategoryUser(String category, String userId) {
        return pantryDao.getItemsByCategoryUser(category, userId);
    }

    public LiveData<List<PantryItem>> getAllPantryItemsUser(String userId) {
        return pantryDao.getAllItemsUser(userId);
    }

    // ==========================================
    // 4. TARİF (RECIPE) METODLARI (ROOM)
    // ==========================================

    public LiveData<List<Recipe>> getAllRecipes(){
        return recipeDao.getAllRecipes();
    }

    public LiveData<Recipe> getRecipeById(int recipeId) {
        return recipeDao.getRecipeById(recipeId);
    }
}