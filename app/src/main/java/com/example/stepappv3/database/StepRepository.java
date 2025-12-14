package com.example.stepappv3.database;

import android.app.Application;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;
import java.util.List; // BU IMPORT EKLENDİ

public class StepRepository {
    private final StepDao stepDao;
    private final PantryDao pantryDao;

    public StepRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        stepDao = db.stepDao();
        pantryDao = db.pantryDao();
    }

    public LiveData<Integer> getDailyStepsUser(long sinceTimestamp, String userId){
        return stepDao.getStepsSinceLiveDataUser(sinceTimestamp, userId);
    }

    public void getStepsSinceUser(long sinceTimestamp, String userId, final OnDataFetchedCallback<Integer> callback) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            final int total = stepDao.getStepsSinceUser(sinceTimestamp, userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                callback.onDataFetched(total);
            });
        });
    }

    public void getTotalStepsAsyncUser(String userId, final OnDataFetchedCallback<Integer> callback){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            final int total = stepDao.getTotalStepsAsyncUser(userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                callback.onDataFetched(total);
            });
        });
    }


    public void getStepsRangeUser(long startTime, long endTime, String userId, final OnDataFetchedCallback<List<Step>> callback) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            List<Step> steps = stepDao.getStepsInRangeUser(startTime, endTime, userId);
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                callback.onDataFetched(steps);
            });
        });
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

    public LiveData<List<PantryItem>> getItemsByCategoryUser(String category, String userId) {
        return pantryDao.getItemsByCategoryUser(category, userId);
    }

    public LiveData<List<PantryItem>> getAllPantryItemsUser(String userId) {
        return pantryDao.getAllItemsUser(userId);
    }
}