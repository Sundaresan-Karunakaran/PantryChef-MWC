package com.example.stepappv3.recommender;

import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.database.recipes.RecipeWithIngredients;

import java.util.List;

public class Recommendation {
    public final RecipeWithIngredients recipe;
    public final int missingCount;

    public Recommendation(RecipeWithIngredients recipe, int missingCount) {
        this.recipe = recipe;
        this.missingCount = missingCount;

    }
}