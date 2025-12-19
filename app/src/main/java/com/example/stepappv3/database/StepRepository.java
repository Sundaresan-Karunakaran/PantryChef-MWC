package com.example.stepappv3.database;
import android.app.Application;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.stepappv3.database.ingredient.MasterIngredient;
import com.example.stepappv3.database.ingredient.MasterIngredientDao;
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.example.stepappv3.database.profile.UserProfile;
import com.example.stepappv3.database.profile.UserProfileDao;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDao;
import com.example.stepappv3.database.recipes.RecipeDetailDisplay;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.database.recipes.RecipeIngredientJoin;
import com.example.stepappv3.database.recipes.RecipeIngredientJoinDao;
import com.example.stepappv3.database.recipes.RecipeWithIngredients;
import com.example.stepappv3.database.steps.DailyStep;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;

import java.util.List;


public class StepRepository {
    private final StepDao stepDao;
    private final PantryDao pantryDao;
    private final RecipeDao recipeDao;
    private final UserProfileDao userDao;
    private final MasterIngredientDao masterIngredientDao;
    private final RecipeIngredientJoinDao recipeIngredientJoinDao;




    public StepRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        stepDao = db.stepDao();
        pantryDao = db.pantryDao();
        recipeDao = db.recipeDao();
        userDao = db.userProfileDao();
        masterIngredientDao = db.masterIngredientDao();
        recipeIngredientJoinDao = db.recipeIngredientJoinDao();
    }

    public LiveData<Integer> getDailyStepsUser(long sinceTimestamp,String userId){
        return stepDao.getStepsSinceLiveDataUser(sinceTimestamp,userId);
    }

    public void insert(Step step){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            stepDao.insert(step);
        });
    }

    public void deleteAllUser(String userId){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            stepDao.deleteAllUser(userId);
        });
    }

    public void insertPantryItem(PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.insert(item);
        });
    }

    public void insertUserProfile(UserProfile userProfile) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(userProfile);
        });
    }

    public LiveData<List<PantryItemDisplay>> getItemsByCategoryUser(String category,String userId) {
        return pantryDao.getItemsByCategoryUser(category,userId);
    }

    public LiveData<List<PantryItemDisplay>> getAllPantryItemsUser(String userId) {
        return pantryDao.getItemsWithNames(userId);
    }

    public void updatePantryItem(PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.update(item);
        });
    }

    public void insertAllItems(List<PantryItem> items){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.insertAll(items);
        });
    }

    public void deleteByIdUser(int itemId,String userId){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.deleteByIdUser(itemId,userId);
        });
    }

    public LiveData<UserProfile> getUserProfile(String userId){
        return userDao.getProfile(userId);
    }


    public List<MasterIngredient> getAllMasterIngredients(){
        return masterIngredientDao.getAllMasterIngredients();
    }

    public List<MasterIngredient> searchIngredientsByName(String query) {
        return masterIngredientDao.searchIngredientsByName(query);
    }

    public LiveData<RecipeDetailDisplay> getRecipeForDetailDisplay(int recipeId){
        MediatorLiveData<RecipeDetailDisplay> result = new MediatorLiveData<>();
        LiveData<Recipe> recipe = recipeDao.getRecipeById(recipeId);
        result.addSource(recipe, recipeValue -> {
            if (recipeValue == null) return;
            StepDatabase.databaseWriteExecutor.execute(() ->{
                List<Integer> ingredientIds = recipeIngredientJoinDao.getIngredientIdsForRecipe(recipeId);
                List<String> ingredientNames = masterIngredientDao.getNamesbyId(ingredientIds);
                result.postValue(new RecipeDetailDisplay(recipeValue,ingredientNames));
            });
            result.removeSource(recipe);
        });
        return result;
    }

    public List<RecipeWithIngredients> getRecipeWithIngredients(){
        return recipeDao.getRecipeWithIngredients();
    }

    public LiveData<List<DailyStep>> getStepsGroupedByDay(String userId){
        return stepDao.getStepsGroupedByDay(userId);
    }

}
