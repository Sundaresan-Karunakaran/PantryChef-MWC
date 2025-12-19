package com.example.stepappv3.database.recipes;

// This is NOT an entity. It's a simple POJO for holding query results.
public class RecipeIngredientInfo {

    public int recipeId;
    public String ingredients;
    public double calories;
    public String name;

    public RecipeIngredientInfo(int recipeId, String ingredients, double calories, String name) {
        this.recipeId = recipeId;
        this.ingredients = ingredients;
        this.calories = calories;
        this.name = name;
    }
}