package com.example.stepappv3.database.recipes;

import java.util.List;

public class RecipeDetailDisplay {
    public final Recipe recipe;
    public final List<String> ingredientNames;

    public RecipeDetailDisplay(Recipe recipe, List<String> ingredientNames) {
        this.recipe = recipe;
        this.ingredientNames = ingredientNames;
    }
}
