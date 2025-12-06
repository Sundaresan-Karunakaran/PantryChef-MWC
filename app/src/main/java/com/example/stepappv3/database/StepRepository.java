package com.example.stepappv3.database;
import android.app.Application;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;

import java.util.List;


public class StepRepository {
    private final StepDao stepDao;
    private final PantryDao pantryDao;

    public StepRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        stepDao = db.stepDao();
        pantryDao = db.pantryDao();
    }

    public LiveData<Integer> getDailySteps(long sinceTimestamp){
        return stepDao.getStepsSinceLiveData(sinceTimestamp);
    }


    public void getStepsSince(long sinceTimestamp, final OnDataFetchedCallback callback) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            // This runs on a background thread.
            final int total = stepDao.getStepsSince(sinceTimestamp);

            // To update the UI, we must post back to the main thread.
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                // This runs on the main UI thread and delivers the result.
                callback.onDataFetched(total);
            });
        });
    }
    public void getTotalStepsAsync(final OnDataFetchedCallback callback){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            final int total = stepDao.getTotalStepsAsync();
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                callback.onDataFetched(total);
            });
        });
    }
    public void insert(Step step){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            stepDao.insert(step);
        });
    }

    public void deleteAll(){
        StepDatabase.databaseWriteExecutor.execute(() -> {
            stepDao.deleteAll();
        });
    }

    public void insertPantryItem(PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.insert(item);
        });
    }

    public LiveData<List<PantryItem>> getItemsByCategory(String category) {
        return pantryDao.getItemsByCategory(category);
    }

    public LiveData<List<PantryItem>> getAllPantryItems() {
        return pantryDao.getAllItems();
    }
}
