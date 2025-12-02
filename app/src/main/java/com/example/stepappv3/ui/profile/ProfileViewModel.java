package com.example.stepappv3.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.OnDataFetchedCallback;

import java.util.Calendar;


public class ProfileViewModel extends AndroidViewModel {
    private StepRepository repo ;

    private StepRepository getRepo() {
        if (repo == null) {
            repo = new StepRepository(getApplication());
        }
        return repo;
    }
    public ProfileViewModel(@NonNull Application application) {
        super(application);
    }
    public void fetchStepsToday(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfTodayTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSince(startOfTodayTimestamp, callback);
    }

    // Method for the "Steps This Hour" button
    public void fetchStepsThisHour(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        long hourAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSince(hourAgoTimestamp, callback);
    }

    // Method for the "Steps This Minute" button
    public void fetchStepsThisMinute(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        long minuteAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSince(minuteAgoTimestamp, callback);
    }

    public void getTotalSteps(OnDataFetchedCallback callback) {

        getRepo().getTotalStepsAsync(callback);

    }
}
