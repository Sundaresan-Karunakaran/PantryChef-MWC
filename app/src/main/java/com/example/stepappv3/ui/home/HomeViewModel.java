package com.example.stepappv3.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.login.FirebaseUserLiveData;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;

public class HomeViewModel extends AndroidViewModel {

    // For the counting state
    private final MutableLiveData<Boolean> _isCounting = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCounting = _isCounting;
    private static final int DAILY_STEP_GOAL = 100;
    private final StepRepository repo;
    public final LiveData<Integer> steps;
    public final LiveData<Integer> progressPercentage;
    private String userId;
    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repo = new StepRepository(application);

        FirebaseUserLiveData userLiveData = new FirebaseUserLiveData();

        LiveData<Integer> rawStepsData = Transformations.switchMap(userLiveData, user -> {
            if (user != null) {
                this.userId = user.getUid();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                long startOfTodayTimestamp = calendar.getTimeInMillis();
                return repo.getDailyStepsUser(startOfTodayTimestamp, this.userId);
            } else {
                this.userId = null;
                return new MutableLiveData<>(null);
            }
        });
        this.steps = Transformations.map(rawStepsData, stepCount -> {
            if (stepCount == null) {
                return 0;
            }
            return stepCount;
        });

        this.progressPercentage = Transformations.map(this.steps, stepCount -> {
            if (stepCount == null) return 0;
            if (stepCount >= DAILY_STEP_GOAL) {
                return 100;
            } else {
                return (stepCount * 100) / DAILY_STEP_GOAL;
            }
        });
    }

    public void onStartStopClicked() {
        _isCounting.setValue(!Boolean.TRUE.equals(_isCounting.getValue()));
    }

    public void onResetClicked() {
        _isCounting.setValue(false);
        if (userId != null) {
            repo.deleteAllUser(this.userId);
        }
    }

    public void onCountClicked() {
        if (Boolean.TRUE.equals(_isCounting.getValue()) && userId != null) {
            Step newStep = new Step(System.currentTimeMillis(), 1, this.userId);
            repo.insert(newStep);
        }
    }
}