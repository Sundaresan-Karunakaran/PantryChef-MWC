package com.example.stepappv3.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.user.UserProfile;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends AndroidViewModel {
    private final StepRepository repository;
    private final LiveData<UserProfile> userProfile;
    private final String currentUserId;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        userProfile = repository.getUserProfile(currentUserId);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    // YENİ: Alan Güncelleme Metodu
    public void updateField(String fieldName, Object value) {
        if (!currentUserId.isEmpty()) {
            repository.updateUserField(currentUserId, fieldName, value);
        }
    }
}