package com.example.stepappv3.recommender;

import android.util.Log;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.recommender.Recommendation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRecommender {

    public List<Recommendation> recommend(List<PantryItem> pantryItems, List<Recipe> allRecipes) {
        if (pantryItems == null || allRecipes == null) {
            return new ArrayList<>();
        }

        Set<String> pantrySet = pantryItems.stream()
                .map(item -> item.name.toLowerCase().trim())
                .collect(Collectors.toSet());

        List<Recommendation> recommendations = new ArrayList<>();

        Log.d("Recommender", "Pantry Set: " + pantrySet.toString());

        // 2. Loop through every recipe to calculate its score.
        for (Recipe recipe : allRecipes) {
            if (recipe.steps == null || recipe.steps.isEmpty()) {
                continue; // Skip recipes with no ingredients listed.
            }
            // 3. Parse the ingredients string into a set.
            Set<String> recipeIngredients = Arrays.stream(recipe.ingredients.split(","))
                    .map(ingredient -> ingredient.toLowerCase().trim())
                    .collect(Collectors.toSet());

            Log.d("Recipe", "Ingredient Set: " + recipeIngredients.toString());

            // 4. Calculate the missing ingredients (the set difference).
            List<String> missing = new ArrayList<>();
            for (String ingredient : recipeIngredients) {
                if (!pantrySet.contains(ingredient)) {
                    missing.add(ingredient);
                }
            }

            // 5. Create a recommendation object.
            recommendations.add(new Recommendation(recipe, missing.size(), missing));
        }

        // 6. Sort the recommendations by the number of missing ingredients (ascending).
        Collections.sort(recommendations, (o1, o2) -> Integer.compare(o1.missingCount, o2.missingCount));

        return recommendations;
    }
}