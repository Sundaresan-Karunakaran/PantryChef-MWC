package com.example.stepappv3.database.recipes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeIngredientJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<RecipeIngredientJoin> joins);

    @Query("SELECT masterIngredientId FROM recipe_ingredient_join WHERE recipeId = :recipeId")
    List<Integer> getIngredientIdsForRecipe(int recipeId);

    @Query("SELECT DISTINCT recipeId FROM recipe_ingredient_join WHERE masterIngredientId IN (:pantryIds)")
    List<Integer> getRecipeCandidatesForPantry(List<Integer> pantryIds);
}
