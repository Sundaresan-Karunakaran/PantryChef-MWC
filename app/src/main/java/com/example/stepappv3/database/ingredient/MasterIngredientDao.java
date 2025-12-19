package com.example.stepappv3.database.ingredient;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MasterIngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<MasterIngredient> ingredients);

    @Query("SELECT * FROM master_ingredient")
    List<MasterIngredient> getAllMasterIngredients();
    @Query("SELECT * FROM master_ingredient WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' LIMIT 10")
    List<MasterIngredient> searchIngredientsByName(String query);

    @Query("SELECT name FROM master_ingredient WHERE id IN (:ids)")
    List<String> getNamesbyId(List<Integer> ids);

}
