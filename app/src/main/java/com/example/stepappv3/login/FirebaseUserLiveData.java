package com.example.stepappv3.login;

import androidx.lifecycle.LiveData;import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUserLiveData extends LiveData<FirebaseUser> {

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        setValue(firebaseAuth.getCurrentUser());
    };

    @Override
    protected void onActive() {
        super.onActive();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}