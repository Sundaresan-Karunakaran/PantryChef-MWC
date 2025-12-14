package com.example.stepappv3.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.OnDataFetchedCallback;

import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.AuthUI;

public class ProfileViewModel extends AndroidViewModel {
    private StepRepository repo;
    private String userId;

    private StepRepository getRepo() {
        if (repo == null) {
            repo = new StepRepository(getApplication());
        }
        return repo;
    }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();
        }
    }

    // DÜZELTME: Parametreye <Integer> eklendi
    public void fetchStepsToday(OnDataFetchedCallback<Integer> callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfTodayTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(startOfTodayTimestamp, this.userId, callback);
    }

    // DÜZELTME: Parametreye <Integer> eklendi
    public void fetchStepsThisHour(OnDataFetchedCallback<Integer> callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        long hourAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(hourAgoTimestamp, this.userId, callback);
    }

    // DÜZELTME: Parametreye <Integer> eklendi
    public void fetchStepsThisMinute(OnDataFetchedCallback<Integer> callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        long minuteAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(minuteAgoTimestamp, this.userId, callback);
    }

    // DÜZELTME: Parametreye <Integer> eklendi
    public void getTotalSteps(OnDataFetchedCallback<Integer> callback) {
        getRepo().getTotalStepsAsyncUser(this.userId, callback);
    }

    public void logout() {
        AuthUI.getInstance()
                .signOut(getApplication().getApplicationContext());
    }
}