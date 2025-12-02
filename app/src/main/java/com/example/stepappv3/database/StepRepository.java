package com.example.stepappv3.database;

import android.app.Application;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;

public class StepRepository {

    private final StepDao stepDao;

    public StepRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        stepDao = db.stepDao();
    }

    // Günlük (belirli zamandan bu yana) adımlar LiveData
    public LiveData<Integer> getDailySteps(long sinceTimestamp) {
        return stepDao.getStepsSinceLiveData(sinceTimestamp);
    }

    // Belirli zamandan bu yana adımlar (callback ile, arka thread + main thread)
    public void getStepsSince(long sinceTimestamp, final OnDataFetchedCallback callback) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final int total = stepDao.getStepsSince(sinceTimestamp);

                android.os.Handler handler =
                        new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onDataFetched(total);
                        }
                    }
                });
            }
        });
    }

    // Toplam adımlar (senkron, callback ile UI'ya dön)
    public void getTotalStepsAsync(final OnDataFetchedCallback callback) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final int total = stepDao.getTotalStepsAsync();
                android.os.Handler handler =
                        new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onDataFetched(total);
                        }
                    }
                });
            }
        });
    }

    // YENİ: Haftalık adım verisi (7 günlük dizi)
    public void getWeekStepsAsync(final long weekStartMillis,
                                  final OnWeekDataFetchedCallback callback) {

        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {

                long oneDay = 24L * 60L * 60L * 1000L;
                long weekEndMillis = weekStartMillis + 7L * oneDay;

                int[] result = new int[7];

                List<Step> stepsInRange = stepDao.getStepsBetween(weekStartMillis, weekEndMillis);
                if (stepsInRange != null) {
                    for (int i = 0; i < stepsInRange.size(); i++) {
                        Step s = stepsInRange.get(i);
                        long ts = s.getTimestamp();

                        if (ts >= weekStartMillis && ts < weekEndMillis) {
                            int index = (int) ((ts - weekStartMillis) / oneDay);
                            if (index >= 0 && index < 7) {
                                result[index] = result[index] + s.getSteps();
                            }
                        }
                    }
                }

                android.os.Handler handler =
                        new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onWeekDataFetched(result);
                        }
                    }
                });
            }
        });
    }

    // Insert
    public void insert(final Step step) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                stepDao.insert(step);
            }
        });
    }

    // Tüm kayıtları sil
    public void deleteAll() {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                stepDao.deleteAll();
            }
        });
    }
}
