package com.example.stepappv3.database.steps;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


@Dao
public interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Step step);

    @Query("DELETE FROM daily_steps WHERE userId = :userId")
    void deleteAllUser(String userId);

    @Query("SELECT * from daily_steps WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    LiveData<Step> getDailyStepUser(String userId);

    @Query("SELECT SUM(steps) FROM daily_steps WHERE userId = :userId")
    LiveData<Integer> getTotalStepsUser(String userId);

    @Query("SELECT IFNULL(SUM(steps),0) FROM daily_steps WHERE userId = :userId")
    int getTotalStepsAsyncUser(String userId);

    @Query("SELECT SUM(steps) FROM daily_steps WHERE timestamp >= :sinceTimestamp AND userId = :userId")
    int getStepsSinceUser(long sinceTimestamp,String userId);

    @Query("SELECT SUM(steps) FROM daily_steps WHERE timestamp >= :sinceTimestamp AND userId = :userId")
    LiveData<Integer> getStepsSinceLiveDataUser(long sinceTimestamp,String userId);

}
