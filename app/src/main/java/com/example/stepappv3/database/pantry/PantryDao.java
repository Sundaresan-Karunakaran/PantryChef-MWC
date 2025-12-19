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


    @Query("DELETE FROM pantry_items WHERE masterIngredientId = :itemId AND userId = :userId")
    void deleteByIdUser(int itemId,String userId);

    @Query("SELECT * FROM pantry_items WHERE userId = :userId ORDER BY quantity ASC")
    LiveData<List<PantryItem>> getAllItemsUser(String userId);

    @Query("SELECT p.*,m.name FROM pantry_items p JOIN master_ingredient m ON p.masterIngredientId = m.id WHERE p.category = :categoryName AND p.userId = :userId")
    LiveData<List<PantryItemDisplay>> getItemsByCategoryUser(String categoryName,String userId);

    @Query("SELECT p.*,m.name FROM pantry_items p JOIN master_ingredient m ON p.masterIngredientId = m.id WHERE p.userId = :userId")
    LiveData<List<PantryItemDisplay>> getItemsWithNames(String userId);

    @androidx.room.Update
    void update(PantryItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PantryItem> items);
}