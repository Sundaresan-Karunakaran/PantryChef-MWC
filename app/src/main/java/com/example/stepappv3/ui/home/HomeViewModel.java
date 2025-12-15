package com.example.stepappv3.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.steps.Step;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    public enum FilterType { DAILY, WEEKLY, MONTHLY }

    private final StepRepository repo;
    private final String userId;

    private final MutableLiveData<List<BarEntry>> _stepsGraphData = new MutableLiveData<>();
    public final LiveData<List<BarEntry>> stepsGraphData = _stepsGraphData;

    private final MutableLiveData<Map<String, Integer>> _pantryPieData = new MutableLiveData<>();
    public final LiveData<Map<String, Integer>> pantryPieData = _pantryPieData;

    public HomeViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        repo = new StepRepository(application);
        userId = savedStateHandle.get("userId");

        loadStepsData(FilterType.DAILY);
        loadPantryData();
    }

    public void setFilter(FilterType type) {
        loadStepsData(type);
    }

    private void loadStepsData(FilterType type) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = 0;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (type == FilterType.DAILY) {
            startTime = calendar.getTimeInMillis();
        } else if (type == FilterType.WEEKLY) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            startTime = calendar.getTimeInMillis();
        } else if (type == FilterType.MONTHLY) {
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            startTime = calendar.getTimeInMillis();
        }

        repo.getStepsRangeUser(startTime, endTime, userId, (List<Step> steps) -> {
            processStepsForGraph(steps, type);
        });
    }

    private void processStepsForGraph(List<Step> steps, FilterType type) {
        Map<Integer, Integer> aggregatedData = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (Step step : steps) {
            // DÜZELTME: getTimestamp() kullanıldı
            cal.setTimeInMillis(step.getTimestamp());
            int key;

            if (type == FilterType.DAILY) {
                key = cal.get(Calendar.HOUR_OF_DAY);
            } else {
                key = cal.get(Calendar.DAY_OF_YEAR);
            }

            // DÜZELTME: getSteps() kullanıldı
            aggregatedData.put(key, aggregatedData.getOrDefault(key, 0) + step.getSteps());
        }

        List<BarEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : aggregatedData.entrySet()) {
            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        _stepsGraphData.postValue(entries);
    }

    private void loadPantryData() {
        repo.getAllPantryItemsUser(userId).observeForever(items -> {
            Map<String, Integer> categoryCounts = new HashMap<>();
            if (items != null) {
                for (PantryItem item : items) {
                    categoryCounts.put(item.category, categoryCounts.getOrDefault(item.category, 0) + 1);
                }
            }
            _pantryPieData.setValue(categoryCounts);
        });
    }
}