package com.example.stepappv3.ui.profile;

import android.app.Application;
import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.profile.UserProfile;
import com.example.stepappv3.util.DataLoadingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileSetupViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private final Map<String, Double> activityLevelMap;
    private final MutableLiveData<Boolean> _profileSaveSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> isDataReady;

    private final MutableLiveData<String> _loadingText = new MutableLiveData<>();
    public final LiveData<String> loadingText = _loadingText;
    private final Handler textUpdateHandler = new Handler(Looper.getMainLooper());
    private int loadingTextIndex = 0;
    private final List<String> loadingMessages = Arrays.asList(
            "Chefs convening ...",
            "Arguments ensue ...",
            "Diplomatic solutions reached...",
            "Almost there..."
    );



    public LiveData<Boolean> getProfileSaveSuccess() {
        return _profileSaveSuccess;
    }

    public ProfileSetupViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);
        isDataReady = DataLoadingManager.getInstance(application).isDataReady;
        if (Boolean.FALSE.equals(isDataReady.getValue())) {
            startLoadingTextUpdates();
        }

        activityLevelMap = new HashMap<>();
        activityLevelMap.put("Sedentary (little or no exercise)", 1.2);
        activityLevelMap.put("Lightly active (exercise 1-3 days/week)", 1.375);
        activityLevelMap.put("Moderately active (exercise 3-5 days/week)", 1.55);
        activityLevelMap.put("Very active (exercise 6-7 days/week)", 1.725);
        activityLevelMap.put("Super active (very hard exercise & physical job)", 1.9);
    }

    private final Runnable textUpdater = new Runnable() {
        @Override
        public void run() {
            _loadingText.setValue(loadingMessages.get(loadingTextIndex));
            loadingTextIndex = (loadingTextIndex + 1) % loadingMessages.size();
            textUpdateHandler.postDelayed(this, 2500); // 2.5 seconds
        }
    };

    private void startLoadingTextUpdates() {
        textUpdateHandler.post(textUpdater);
    }

    private void stopLoadingTextUpdates() {
        textUpdateHandler.removeCallbacks(textUpdater);
    }

    public void saveProfile(String gender, String ageStr, String weightStr, String heightStr, String activityLevelStr) {

        if (gender == null || gender.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || activityLevelStr.isEmpty()) {
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

            double bmr;
            if ("m".equalsIgnoreCase(gender)) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            }

            double reqCalories = bmr * activityLevel;
            double reqSugar = 0.09 * reqCalories / 4;
            double reqFat = 0.29 * reqCalories / 9;
            double reqSalt = 5;
            double reqFruitVeg = 400;
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            String userId = firebaseUser.getUid();

            UserProfile userProfile = new UserProfile(
                    userId, gender, age, weight, height, activityLevel,
                    reqCalories, reqSugar, reqFat, reqSalt, reqFruitVeg,bmr
            );

            repository.insertUserProfile(userProfile);
            _profileSaveSuccess.postValue(true);

        } catch (NumberFormatException e) {
        }
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        stopLoadingTextUpdates();
    }
}