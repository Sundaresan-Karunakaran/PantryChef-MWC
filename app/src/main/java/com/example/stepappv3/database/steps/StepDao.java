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

    @Query("DELETE FROM daily_steps")
    void deleteAll();

    @Query("SELECT * from daily_steps ORDER BY timestamp DESC LIMIT 1")
    LiveData<Step> getDailyStep();

    @Query("SELECT SUM(steps) FROM daily_steps")
    LiveData<Integer> getTotalSteps();

    @Query("SELECT IFNULL(SUM(steps),0) FROM daily_steps")
    int getTotalStepsAsync();

    @Query("SELECT SUM(steps) FROM daily_steps WHERE timestamp >= :sinceTimestamp")
    int getStepsSince(long sinceTimestamp);

    @Query("SELECT SUM(steps) FROM daily_steps WHERE timestamp >= :sinceTimestamp")
    LiveData<Integer> getStepsSinceLiveData(long sinceTimestamp);

}
