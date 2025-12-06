package com.example.stepappv3.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.StepRepository;
import java.util.Calendar;

public class HomeViewModel extends AndroidViewModel {
    // For the counting state
    private final MutableLiveData<Boolean> _isCounting = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCounting = _isCounting;

    // For the step count
    private final MutableLiveData<Integer> _steps = new MutableLiveData<>(0);
    public final LiveData<Integer> steps;
    private static final int DAILY_STEP_GOAL = 100;
    private StepRepository repo;
    public LiveData<Integer> progressPercentage ;
    private StepRepository getRepo(){
        if (repo == null){
            repo = new StepRepository(getApplication());
        }
        return repo;
    }

    public HomeViewModel(@NonNull Application application){
        super(application);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfTodayTimestamp = calendar.getTimeInMillis();

        // 2. Get the reactive stream for steps today from the repository.
        LiveData<Integer> rawStepsToday = getRepo().getDailySteps(startOfTodayTimestamp);

        // 3. Transform it for the UI, handling the null case.
        steps = Transformations.map(rawStepsToday, total -> total == null ? 0 : total);

        progressPercentage = Transformations.map(steps, stepCount -> {
            if (stepCount >= DAILY_STEP_GOAL) {
                return 100;
            } else {
                return (stepCount * 100) / DAILY_STEP_GOAL;
            }
        });

    }

    public void onStartStopClicked() {
        boolean currentState = _isCounting.getValue();
        _isCounting.setValue(!currentState);
    }
    public void onResetClicked() {
        _steps.setValue(0);
        _isCounting.setValue(false);
        getRepo().deleteAll();

    }


    public void onCountClicked() {
        if (Boolean.TRUE.equals(_isCounting.getValue())) {
            Step newStep = new Step(System.currentTimeMillis(), 1);
            getRepo().insert(newStep);
        }
    }

}
