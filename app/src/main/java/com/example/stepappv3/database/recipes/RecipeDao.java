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

    // DÜZELTİLDİ: Bu metot artık sadece senkronize (anlık) veri çekmek için kullanılır.
    // LiveData dönen aynı isimli metot kaldırıldı.
    @Query("SELECT recipeId, name, ingredients, calories FROM recipes")
    List<RecipeIngredientInfo> getRecipeIngredientInfoListSynchronous();


    // ORİJİNAL LIVE DATA METODU: Bu metot zaten mevcuttu.
    @Query("SELECT recipeId, name, ingredients, calories FROM recipes")
    LiveData<List<RecipeIngredientInfo>> getRecipeIngredientInfoList();


}