package com.example.stepappv3.database.recipes;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;


@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    LiveData<List<Recipe>> getAllRecipes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRecipes(List<Recipe> recipes);


    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId")
    LiveData<Recipe> getRecipeById(int recipeId);


}
