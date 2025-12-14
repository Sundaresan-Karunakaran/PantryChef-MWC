package com.example.stepappv3.ui.onboarding;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.user.UserProfile;
import com.google.firebase.auth.FirebaseAuth; // YENİ

public class OnboardingViewModel extends AndroidViewModel {
    private final StepRepository repository;
    public UserProfile tempProfile;

    public OnboardingViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);

        // ŞU AN GİRİŞ YAPMIŞ OLAN KULLANICININ ID'SİNİ ALIYORUZ
        String uid = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Bu ID ile boş bir profil oluşturuyoruz
        tempProfile = new UserProfile(uid);
    }

    public void saveProfile() {
        if (tempProfile.userId != null && !tempProfile.userId.isEmpty()) {
            // Repository üzerinden Firebase'e kaydediyoruz
            repository.saveUserProfile(tempProfile);
        }
    }
}