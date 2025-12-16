package com.example.stepappv3.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DataLoadingManager {

    private static final String PREFS_NAME = "DataLoadingPrefs";
    private static final String KEY_IS_DATA_LOADED = "isDataLoaded";

    private static volatile DataLoadingManager instance;
    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<Boolean> _isDataReady = new MutableLiveData<>();
    public final LiveData<Boolean> isDataReady = _isDataReady;

    private DataLoadingManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoaded = sharedPreferences.getBoolean(KEY_IS_DATA_LOADED, false);
        _isDataReady.postValue(isLoaded);
    }

    public static DataLoadingManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DataLoadingManager.class) {
                if (instance == null) {
                    instance = new DataLoadingManager(context);
                }
            }
        }
        return instance;
    }

    // --- BU METOD EKSİKTİ, EKLENDİ ---
    public boolean isDataLoadingComplete() {
        return sharedPreferences.getBoolean(KEY_IS_DATA_LOADED, false);
    }

    public void setDataLoadingComplete() {
        if (!isDataLoadingComplete()) {
            sharedPreferences.edit().putBoolean(KEY_IS_DATA_LOADED, true).apply();
            _isDataReady.postValue(true);
        }
    }
}