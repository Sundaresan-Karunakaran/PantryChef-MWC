package com.example.stepappv3.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface PantryDao {

    @Insert
    void insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items")
    void deleteAll();

    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    LiveData<List<PantryItem>> getAllItems();


}
