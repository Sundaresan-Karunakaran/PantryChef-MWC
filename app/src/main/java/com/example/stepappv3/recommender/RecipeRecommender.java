package com.example.stepappv3.recommender;

import android.database.Cursor;
import android.util.Log;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeIngredientInfo;
import com.example.stepappv3.database.recipes.RecipeIngredientJoinDao;
import com.example.stepappv3.database.recipes.RecipeWithIngredients;
import com.example.stepappv3.recommender.Recommendation;
import com.example.stepappv3.util.HelperFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRecommender {




    public List<Recommendation> recommend(Set<Integer> pantryIngredientIds,List<RecipeWithIngredients> allRecipes) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (RecipeWithIngredients recipe : allRecipes) {
            if (recipe.ingredientIds == null || recipe.ingredientIds.isEmpty()) {
                continue;
            }
            int missingCount = 0;
            List<Integer> ingredientIds = Arrays.stream(recipe.ingredientIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            Set<Integer> recipeIngredientIds = new HashSet<>(ingredientIds);

            Set<Integer> missingIds = new HashSet<>(recipeIngredientIds);
            missingIds.removeAll(pantryIngredientIds);

            missingCount = missingIds.size();

            Recommendation recommendation = new Recommendation(recipe, missingCount);
            recommendations.add(recommendation);
        }
        Collections.sort(recommendations, (o1, o2) -> Integer.compare(o1.missingCount, o2.missingCount));
        int limit = Math.min(recommendations.size(), 5);
        return recommendations.subList(0, limit);
    }
}