package com.example.stepappv3.ui.visualization;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.example.stepappv3.database.steps.DailyStep;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizationViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private final LiveData<Map<String, Integer>> pantryCategoryCounts;
    private final LiveData<List<DailyStep>> dailySteps;

    public VisualizationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StepRepository(application);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LiveData<List<PantryItemDisplay>> allPantryItems = repository.getAllPantryItemsUser(userId);
        this.pantryCategoryCounts = Transformations.map(allPantryItems, items -> {
            Map<String, Integer> counts = new HashMap<>();
            for (PantryItemDisplay item : items) {
                counts.put(item.category, counts.getOrDefault(item.category, 0) + 1);
            }
            return counts;
        });
        this.dailySteps = repository.getStepsGroupedByDay(userId);
    }

    public LiveData<Map<String, Integer>> getPantryCategoryCounts() {
        return pantryCategoryCounts;
    }
    public LiveData<List<DailyStep>> getDailySteps() {
        return dailySteps;
    }
}
    