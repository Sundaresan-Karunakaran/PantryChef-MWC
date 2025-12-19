package com.example.stepappv3.ui.profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.OnDataFetchedCallback;

import java.util.Calendar;

import com.example.stepappv3.database.profile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.AuthUI;


public class ProfileViewModel extends AndroidViewModel {
    private StepRepository repo ;
    private String userId;
    public final LiveData<UserProfile> userProfile;

    public ProfileViewModel(@NonNull Application application) {

        super(application);
        repo = new StepRepository(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userId = currentUser.getUid();
        userProfile = repo.getUserProfile(currentUser.getUid());


    }
    public void logout() {
        AuthUI.getInstance()
                .signOut(getApplication().getApplicationContext());
    }

}
