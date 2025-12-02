package com.example.stepappv3.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.stepappv3.database.PantryItem;
import com.example.stepappv3.database.PantryRepository;

import java.util.List;

/**
 * ViewModel for Pantry screen.
 * Holds reference to PantryRepository and exposes LiveData.
 */
public class PantryViewModel extends AndroidViewModel {

    private PantryRepository repository;
    private LiveData<List<PantryItem>> allItems;

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new PantryRepository(application);
        allItems = repository.getAllItems();
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return allItems;
    }

    public void insert(PantryItem item) {
        repository.insert(item);
    }

    public void update(PantryItem item) {
        repository.update(item);
    }

    public void delete(PantryItem item) {
        repository.delete(item);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    // İleride istersen Pantry ekranından da çağırabil diye:
    public void addItemsFromText(String text, boolean fromRecipe) {
        repository.addItemsFromText(text, fromRecipe);
    }
}
