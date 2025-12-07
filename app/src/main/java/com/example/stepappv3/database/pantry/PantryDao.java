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

    @Query("UPDATE pantry_items SET name = :name , category = :category,quantity = :quantity, unit = :unit WHERE id = :id AND userId = :userId")
    void updateForUser(int id, String userId, String name, String category, int quantity, String unit);

    @Query("DELETE FROM pantry_items WHERE id = :itemId AND userId = :userId")
    void deleteByIdUser(int itemId,String userId);

    @Query("SELECT * FROM pantry_items WHERE userId = :userId ORDER BY quantity ASC")
    LiveData<List<PantryItem>> getAllItemsUser(String userId);

    @Query("SELECT * FROM pantry_items WHERE category = :categoryName AND userId = :userId ORDER BY quantity ASC")
    LiveData<List<PantryItem>> getItemsByCategoryUser(String categoryName,String userId);
}