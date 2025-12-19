package com.example.stepappv3.database.recipes;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.stepappv3.database.ingredient.MasterIngredient;

@Entity(tableName = "recipe_ingredient_join",
        primaryKeys = {"recipeId", "masterIngredientId"},
        foreignKeys = {
                @ForeignKey(entity = Recipe.class,
                        parentColumns = "recipeId",
                        childColumns = "recipeId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = MasterIngredient.class,
                        parentColumns = "id",
                        childColumns = "masterIngredientId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("recipeId"), @Index("masterIngredientId")}
)
public class RecipeIngredientJoin {

    public int recipeId;
    public int masterIngredientId;

    public RecipeIngredientJoin(int recipeId, int masterIngredientId) {
        this.recipeId = recipeId;
        this.masterIngredientId = masterIngredientId;
    }

}
