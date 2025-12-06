package com.example.stepappv3.database.pantry;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PantryItem pantryItem);

    @Update
    void update(PantryItem pantryItem);

    @Query("DELETE FROM pantry_items WHERE id = :itemId")
    void deleteById(int itemId);

    @Query("SELECT * FROM pantry_items ORDER BY quantity ASC")
    LiveData<List<PantryItem>> getAllItems();

    @Query("SELECT * FROM pantry_items WHERE category = :categoryName ORDER BY quantity ASC")
    LiveData<List<PantryItem>> getItemsByCategory(String categoryName);
}