package com.example.stepappv3.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {

    // This is now the single source of truth, observed from our new class.
    public final LiveData<FirebaseUser> user;

    // This LiveData simply transforms the user object into the state for the UI.
    public final LiveData<AuthenticationState> authenticationState;

    public LoginViewModel(@NonNull Application application) {
        super(application);

        // 1. Get the live stream of the Firebase user.
        this.user = new FirebaseUserLiveData();

        // 2. Transform the user LiveData into the AuthenticationState LiveData.
        this.authenticationState = Transformations.map(this.user, user -> {
            if (user != null) {
                // If the user object exists, the state is AUTHENTICATED.
                return AuthenticationState.AUTHENTICATED;
            } else {
                // If the user object is null, the state is UNAUTHENTICATED.
                return AuthenticationState.UNAUTHENTICATED;
            }
        });
    }
}