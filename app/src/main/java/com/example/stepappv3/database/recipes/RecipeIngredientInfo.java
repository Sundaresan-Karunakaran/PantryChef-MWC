package com.example.stepappv3.database.recipes;

// This is NOT an entity. It's a simple POJO for holding query results.
public class RecipeIngredientInfo {

    public int recipeId;
    public String name;public String ingredients;
    public double calories;

    public RecipeIngredientInfo(int recipeId, String name, String ingredients, double calories) {
        this.recipeId = recipeId;
        this.name = name;
        this.ingredients = ingredients;
        this.calories = calories;
    }
}