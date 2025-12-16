package com.example.stepappv3.ui.step;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.steps.Step;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StepViewModel extends AndroidViewModel {

    private final StepRepository repository;
    private String userId;

    private final MutableLiveData<Integer> _steps = new MutableLiveData<>(0);
    public final LiveData<Integer> steps = _steps;

    private final MutableLiveData<Boolean> _isCounting = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCounting = _isCounting;

    private final MutableLiveData<Boolean> _isSessionActive = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSessionActive = _isSessionActive;

    public StepViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = ""; // Boş ise hata vermesin diye
        }
    }

    public void startCounting() {
        _isSessionActive.setValue(true);
        _isCounting.setValue(true);
    }

    public void togglePause() {
        Boolean current = _isCounting.getValue();
        _isCounting.setValue(current == null || !current);
    }

    public void onCountClicked() {
        if (Boolean.TRUE.equals(_isCounting.getValue())) {
            Integer current = _steps.getValue();
            _steps.postValue(current == null ? 1 : current + 1);
        }
    }

    public void finishAndSave() {
        Integer currentSteps = _steps.getValue();

        if (currentSteps != null && currentSteps > 0 && userId != null && !userId.isEmpty()) {
            // --- DÜZELTME BURADA ---
            // Step constructor'ı artık 3 parametre istiyor: (timestamp, steps, userId)
            // UserId'yi constructor içine ekledik.
            Step step = new Step(System.currentTimeMillis(), currentSteps, userId);

            repository.insert(step);
        }

        resetSession();
    }

    public void finishAndDiscard() {
        resetSession();
    }

    private void resetSession() {
        _steps.setValue(0);
        _isCounting.setValue(false);
        _isSessionActive.setValue(false);
    }
}