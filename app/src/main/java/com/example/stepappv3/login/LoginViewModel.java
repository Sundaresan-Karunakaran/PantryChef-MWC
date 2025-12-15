package com.example.stepappv3.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.profile.UserProfile;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {

    // This is now the single source of truth, observed from our new class.
    public final LiveData<FirebaseUser> user;

    // This LiveData simply transforms the user object into the state for the UI.
    public final LiveData<AuthenticationState> authenticationState;
    public final StepRepository repository;
    public final LiveData<UserProfile> userProfile;


    public LoginViewModel(@NonNull Application application) {
        super(application);

        // 1. Get the live stream of the Firebase user.
        this.user = new FirebaseUserLiveData();
        this.repository = new StepRepository(application);

        this.authenticationState = Transformations.map(this.user, user -> {
            if (user != null) {
                // If the user object exists, the state is AUTHENTICATED.
                return AuthenticationState.AUTHENTICATED;
            } else {
                // If the user object is null, the state is UNAUTHENTICATED.
                return AuthenticationState.UNAUTHENTICATED;
            }
        });
        this.userProfile = Transformations.switchMap(this.user, user -> {
            if (user != null) {
                // If the user is logged in, "switch" to observing the user's profile from the database.
                return repository.getUserProfile(user.getUid());
            } else {
                // If the user is logged out, return a LiveData that holds null.
                return new MutableLiveData<>(null);
            }
        });
    }
}