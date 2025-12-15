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

    // These fields will now be populated reactively.
    public final LiveData<Integer> steps;
    public final LiveData<Integer> progressPercentage;

    // We will get the userId from the user LiveData stream.
    private String userId;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repo = new StepRepository(application);

        // 1. Get the single source of truth for the user's authentication state.
        FirebaseUserLiveData userLiveData = new FirebaseUserLiveData();

        // 2. Use Transformations.switchMap to create a chain.
        //    When the user logs in, we "switch" to observing the database query.
        LiveData<Integer> rawStepsData = Transformations.switchMap(userLiveData, user -> {
            if (user != null) {
                // User is logged in. Store their ID for use in other methods.
                this.userId = user.getUid();

                // Calculate the timestamp for the start of today.
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                long startOfTodayTimestamp = calendar.getTimeInMillis();

                // Return the LiveData stream from the repository. This is the "switch".
                return repo.getDailyStepsUser(startOfTodayTimestamp, this.userId);
            } else {
                // User is logged out. Clear the userId and return a LiveData holding null.
                this.userId = null;
                return new MutableLiveData<>(null);
            }
        });

        // 3. Create the FINAL, public `steps` LiveData by transforming the raw data.
        //    This map's ONLY job is to convert a null from the database into a 0 for the UI.
        this.steps = Transformations.map(rawStepsData, stepCount -> {
            if (stepCount == null) {
                return 0; // If the database returns null (no steps), we tell the UI it's 0.
            }
            return stepCount; // Otherwise, pass the real value through.
        });


        // 3. The progressPercentage transformation remains the same.
        //    It will now correctly react to changes in the new `steps` LiveData.
        this.progressPercentage = Transformations.map(this.steps, stepCount -> {
            if (stepCount == null) return 0;
            if (stepCount >= DAILY_STEP_GOAL) {
                return 100;
            } else {
                return (stepCount * 100) / DAILY_STEP_GOAL;
            }
        });
    }

    // The rest of your methods are fine, but they must use the reactive userId field.
    // Make sure they check if userId is not null before using.

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