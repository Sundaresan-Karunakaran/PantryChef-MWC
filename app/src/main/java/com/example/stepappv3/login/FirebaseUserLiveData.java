package com.example.stepappv3.login;

import androidx.lifecycle.LiveData;import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUserLiveData extends LiveData<FirebaseUser> {

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        // When the auth state changes, post the new user object to the LiveData.
        setValue(firebaseAuth.getCurrentUser());
    };

    // When this LiveData has an active observer, start listening.
    @Override
    protected void onActive() {
        super.onActive();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    // When this LiveData has no active observers, stop listening to save resources.
    @Override
    protected void onInactive() {
        super.onInactive();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}