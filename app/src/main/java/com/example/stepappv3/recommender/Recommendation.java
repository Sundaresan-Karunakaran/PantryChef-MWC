package com.example.stepappv3.recommender;

import com.example.stepappv3.database.recipes.Recipe;
import java.util.List;

public class Recommendation {
    public final Recipe recipe;
    public final int missingCount;
    public final List<String> missingIngredients;

    public Recommendation(Recipe recipe, int missingCount, List<String> missingIngredients) {
        this.recipe = recipe;
        this.missingCount = missingCount;
        this.missingIngredients = missingIngredients;
    }
}