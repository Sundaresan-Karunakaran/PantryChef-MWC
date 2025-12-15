package com.example.stepappv3.database.profile;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;@Dao
public interface UserProfileDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfile userProfile);

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    LiveData<UserProfile> getProfile(String userId);
}