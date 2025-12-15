package com.example.stepappv3.database.recipes;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey
    public int recipeId;

    public String name;
    public String steps;
    public double calories;
    public double sugar;
    public double fat;
    public double fiber;
    public int serving;
    public String missingNutrients;
    public String servingSize;
    public String ingredients;

    public Recipe(int recipeId,String name, String servingSize, int serving,String steps,String ingredients ,String missingNutrients,double calories,double sugar,double fat,double fiber){
        this.recipeId = recipeId;
        this.name = name;
        this.steps = steps;
        this.calories = calories;
        this.sugar = sugar;
        this.fat = fat;
        this.fiber = fiber;
        this.serving = serving;
        this.missingNutrients = missingNutrients;
        this.servingSize = servingSize;
        this.ingredients = ingredients;
    }
}
