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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    public enum FilterType { DAILY, WEEKLY, MONTHLY }

    private final StepRepository repo;
    private String userId;

    private final MutableLiveData<List<BarEntry>> _stepsGraphData = new MutableLiveData<>();
    public final LiveData<List<BarEntry>> stepsGraphData = _stepsGraphData;

    private final MutableLiveData<Map<String, Integer>> _pantryPieData = new MutableLiveData<>();
    public final LiveData<Map<String, Integer>> pantryPieData = _pantryPieData;

    private final MutableLiveData<FilterType> _currentFilter = new MutableLiveData<>(FilterType.DAILY);
    public final LiveData<FilterType> currentFilter = _currentFilter;

    public HomeViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        repo = new StepRepository(application);

        // --- DÜZELTME 1: UserID'yi doğrudan Firebase'den alıyoruz ---
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = "";
        }

        // İlk açılışta verileri yükle
        loadStepsData(FilterType.DAILY);
        loadPantryData();
    }

    // --- DÜZELTME 2: Sayfaya her dönüldüğünde çağrılacak metod ---
    public void refresh() {
        if (_currentFilter.getValue() != null) {
            loadStepsData(_currentFilter.getValue());
            loadPantryData();
        }
    }

    public void setFilter(FilterType type) {
        _currentFilter.setValue(type);
        loadStepsData(type);
    }

    private void loadStepsData(FilterType type) {
        if (userId == null || userId.isEmpty()) return;

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = 0;

        // Günü sıfırla
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (type == FilterType.DAILY) {
            startTime = calendar.getTimeInMillis();
        } else if (type == FilterType.WEEKLY) {
            calendar.add(Calendar.DAY_OF_YEAR, -6);
            startTime = calendar.getTimeInMillis();
        } else if (type == FilterType.MONTHLY) {
            calendar.add(Calendar.DAY_OF_YEAR, -29);
            startTime = calendar.getTimeInMillis();
        }

        repo.getStepsRangeUser(startTime, endTime, userId, (List<Step> steps) -> {
            processStepsForGraph(steps, type);
        });
    }

    private void processStepsForGraph(List<Step> steps, FilterType type) {
        Map<Integer, Integer> aggregatedData = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        // Gelen verileri topla (Aggregation)
        for (Step step : steps) {
            cal.setTimeInMillis(step.getTimestamp());
            int key;

            if (type == FilterType.DAILY) {
                key = cal.get(Calendar.HOUR_OF_DAY);
            } else {
                key = cal.get(Calendar.DAY_OF_YEAR);
            }

            // Aynı saat/gün için birden fazla kayıt varsa üstüne ekle (Toplama işlemi)
            aggregatedData.put(key, aggregatedData.getOrDefault(key, 0) + step.getSteps());
        }

        List<BarEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : aggregatedData.entrySet()) {
            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        _stepsGraphData.postValue(entries);
    }

    private void loadPantryData() {
        if (userId == null || userId.isEmpty()) return;

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