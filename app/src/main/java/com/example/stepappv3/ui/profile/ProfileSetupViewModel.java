package com.example.stepappv3.ui.profile;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.profile.UserProfile;
import com.example.stepappv3.util.DataLoadingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private final Map<String, Double> activityLevelMap;

    // LiveData to signal when the profile has been successfully saved.
    private final MutableLiveData<Boolean> _profileSaveSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> isDataReady;



    public LiveData<Boolean> getProfileSaveSuccess() {
        return _profileSaveSuccess;
    }

    public ProfileSetupViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);
        isDataReady = DataLoadingManager.getInstance(application).isDataReady;


        // This map translates the user-friendly string to the calculation multiplier.
        activityLevelMap = new HashMap<>();
        activityLevelMap.put("Sedentary (little or no exercise)", 1.2);
        activityLevelMap.put("Lightly active (exercise 1-3 days/week)", 1.375);
        activityLevelMap.put("Moderately active (exercise 3-5 days/week)", 1.55);
        activityLevelMap.put("Very active (exercise 6-7 days/week)", 1.725);
        activityLevelMap.put("Super active (very hard exercise & physical job)", 1.9);
    }

    public void saveProfile(String gender, String ageStr, String weightStr, String heightStr, String activityLevelStr) {
        // --- 1. Validation ---
        // In a real app, you would also post error messages to LiveData for the UI.
        if (gender == null || gender.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || activityLevelStr.isEmpty()) {
            // Post an error message to a LiveData object for the fragment to observe.
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            Double activityLevel = activityLevelMap.get(activityLevelStr);

            if (activityLevel == null) {

                return;
            }

            // --- 2. Calculation ---
            double bmr;
            if ("m".equalsIgnoreCase(gender)) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            }

            double reqCalories = bmr * activityLevel;
            double reqSugar = 0.09 * reqCalories;
            double reqFat = 0.29 * reqCalories;
            double reqSalt = 5;
            double reqFruitVeg = 400;

            // --- 3. Persistence ---
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                // Cannot save profile without a user.
                return;
            }
            String userId = firebaseUser.getUid();

            UserProfile userProfile = new UserProfile(
                    userId, gender, age, weight, height, activityLevel,
                    reqCalories, reqSugar, reqFat, reqSalt, reqFruitVeg,bmr
            );

            repository.insertUserProfile(userProfile);

            // 4. Signal Success
            _profileSaveSuccess.postValue(true);

        } catch (NumberFormatException e) {
            // Handle error for invalid numbers
        }
    }
}