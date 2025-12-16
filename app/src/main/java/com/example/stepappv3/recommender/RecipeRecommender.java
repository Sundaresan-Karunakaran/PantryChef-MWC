package com.example.stepappv3.recommender;

import android.util.Log;

// LiveData is no longer needed here!
// import androidx.lifecycle.LiveData;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRecommender {

    /**
     * Recommends recipes based on available pantry items.
     * This is a pure, synchronous method that contains only business logic.
     * It expects to be given fully resolved lists of data.
     *
     * @param pantryItems A List of items the user has.
     * @param allRecipes  A List of all possible recipes to compare against.
     * @return A sorted List of recommendations.
     */
    public List<Recommendation> recommend(List<PantryItem> pantryItems, List<RecipeIngredientInfo> allRecipes) {
        // The ViewModel is now responsible for ensuring these are not null.
        // However, defensive checks are still good practice.
        if (pantryItems == null || allRecipes == null) {
            return new ArrayList<>();
        }

        // 1. Convert the pantry items to a case-insensitive set for efficient lookups.
        Set<String> pantrySet = pantryItems.stream()
                .map(item -> item.name.toLowerCase().trim())
                .collect(Collectors.toSet());

        List<Recommendation> recommendations = new ArrayList<>();

        // 2. Loop through every recipe to calculate its score.
        // We now loop directly over the `allRecipes` parameter.
        for (RecipeIngredientInfo recipe : allRecipes) {

            // 3. Parse the ingredients string into a set.
            Set<String> recipeIngredients = Arrays.stream(recipe.ingredients.split(","))
                    .map(ingredient -> ingredient.toLowerCase().trim())
                    .collect(Collectors.toSet());

            // 4. Calculate the missing ingredients.
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
