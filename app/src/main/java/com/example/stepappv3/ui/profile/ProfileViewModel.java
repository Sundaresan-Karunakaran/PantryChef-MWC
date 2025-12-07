package com.example.stepappv3.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.OnDataFetchedCallback;

import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.AuthUI;


public class ProfileViewModel extends AndroidViewModel {
    private StepRepository repo ;
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
        this.userId = currentUser.getUid();

    }
    public void fetchStepsToday(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfTodayTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(startOfTodayTimestamp, this.userId, callback);
    }

    // Method for the "Steps This Hour" button
    public void fetchStepsThisHour(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        long hourAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(hourAgoTimestamp, this.userId, callback);
    }

    // Method for the "Steps This Minute" button
    public void fetchStepsThisMinute(OnDataFetchedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        long minuteAgoTimestamp = calendar.getTimeInMillis();
        getRepo().getStepsSinceUser(minuteAgoTimestamp, this.userId, callback);
    }

    public void getTotalSteps(OnDataFetchedCallback callback) {

        getRepo().getTotalStepsAsyncUser(this.userId, callback);

    }

    public void logout() {
        // We get the application context, which is safe to use here.
        // This call securely clears the user's session and all associated tokens.
        AuthUI.getInstance()
                .signOut(getApplication().getApplicationContext());
    }
}
