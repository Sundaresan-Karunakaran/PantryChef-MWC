package com.example.stepappv3.ui.recipes;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.stepappv3.database.StepDatabase;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDetailDisplay;
import com.example.stepappv3.database.recipes.RecipeIngredientJoinDao;
import com.example.stepappv3.util.HelperFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeDetailViewModel extends AndroidViewModel {

    public final LiveData<RecipeDetailDisplay> recipe;

    private final StepRepository repository;
    private final MutableLiveData<Integer> _currentServingSize = new MutableLiveData<>();

    public final MediatorLiveData<Double> displayedCalories = new MediatorLiveData<>();
    public final MediatorLiveData<Double> displayedFat = new MediatorLiveData<>();
    public final MediatorLiveData<Double> displayedSugars = new MediatorLiveData<>();
    public final MediatorLiveData<Double> displayedFiber = new MediatorLiveData<>();
    public final MediatorLiveData<List<String>> missingIngredients = new MediatorLiveData<>();

    private final LiveData<List<PantryItemDisplay>> pantryItems;



    public RecipeDetailViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        this.repository = new StepRepository(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.pantryItems = (currentUser != null) ? repository.getAllPantryItemsUser(currentUser.getUid()) : new MutableLiveData<>(new java.util.ArrayList<>());


        Integer recipeId = savedStateHandle.get("recipeId");
        if (recipeId != null) {
            this.recipe = repository.getRecipeForDetailDisplay(recipeId);
        } else {
            this.recipe = repository.getRecipeForDetailDisplay(-1);
        }
        displayedCalories.addSource(this.recipe, recipe -> calculateDisplayedNutrients());
        displayedCalories.addSource(_currentServingSize, servingSize -> calculateDisplayedNutrients());

        displayedFat.addSource(this.recipe, recipe -> calculateDisplayedNutrients());
        displayedFat.addSource(_currentServingSize, servingSize -> calculateDisplayedNutrients());

        displayedSugars.addSource(this.recipe, recipe -> calculateDisplayedNutrients());
        displayedSugars.addSource(_currentServingSize, servingSize -> calculateDisplayedNutrients());

        displayedFiber.addSource(this.recipe, recipe -> calculateDisplayedNutrients());
        displayedFiber.addSource(_currentServingSize, servingSize -> calculateDisplayedNutrients());

        missingIngredients.addSource(this.recipe, recipe -> prepareMissingList());
        missingIngredients.addSource(this.pantryItems, pantryItems -> prepareMissingList());


    }
    public void updateServingSize(int newSize) {
        _currentServingSize.setValue(newSize);
    }

    private void calculateDisplayedNutrients() {
        if(this.recipe.getValue() == null) return;
        Recipe currentRecipe = this.recipe.getValue().recipe;
        Integer currentServings = _currentServingSize.getValue();

        if (currentRecipe == null || currentServings == null || currentRecipe.serving == 0) {
            return;
        }

        double caloriesPerServing = currentRecipe.calories / currentRecipe.serving;
        double fatPerServing = currentRecipe.fat / currentRecipe.serving;
        double sugarPerServing = currentRecipe.sugar / currentRecipe.serving;
        double fiberPerServing = currentRecipe.fiber / currentRecipe.serving;

        displayedCalories.postValue(caloriesPerServing * currentServings);
        displayedFat.postValue(fatPerServing * currentServings);
        displayedSugars.postValue(sugarPerServing * currentServings);
        displayedFiber.postValue(fiberPerServing * currentServings);
    }

    private void prepareMissingList(){
        if(this.recipe.getValue() == null || this.pantryItems.getValue() == null) return;
        List<String> recipeIngredients = this.recipe.getValue().ingredientNames;
        List<PantryItemDisplay> currentPantry = this.pantryItems.getValue();
        Set<String> pantryIngredients = currentPantry.stream()
                .map(pantryItem -> pantryItem.name)
                .collect(Collectors.toSet());

        List<String> missing = new ArrayList<>();
        for (String ingredient : recipeIngredients) {
            if (!pantryIngredients.contains(ingredient)) {
                missing.add(ingredient);
            }
        }
        missingIngredients.postValue(missing);
    }

}