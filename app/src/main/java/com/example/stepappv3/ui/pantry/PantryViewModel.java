package com.example.stepappv3.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.R; // Make sure this import is correct

import java.util.ArrayList;
import java.util.List;

public class PantryViewModel extends AndroidViewModel {

    // This is the public, read-only LiveData the Fragment will observe.
    public final LiveData<List<PantryCategory>> categories;

    // This is the private, writable version.
    private final MutableLiveData<List<PantryCategory>> _categories = new MutableLiveData<>();

    public PantryViewModel(@NonNull Application application) {
        super(application);
        // Link the public LiveData to the private one.
        categories = _categories;

        // For now, load a static list of data.
        loadStaticCategories();
    }

    private void loadStaticCategories() {
        List<PantryCategory> staticList = new ArrayList<>();
        // In a real app, you would fetch this from the database.
        // Replace R.drawable... with real images you've added to your project.
        staticList.add(new PantryCategory("Dairy & Eggs", R.drawable.ic_dairy_eggs));
        staticList.add(new PantryCategory("Vegetables", R.drawable.ic_veg));
        staticList.add(new PantryCategory("Bakery", R.drawable.ic_bread));
        staticList.add(new PantryCategory("Meats & Seafood", R.drawable.ic_meat));
        staticList.add(new PantryCategory("Spices", R.drawable.ic_spices));
        staticList.add(new PantryCategory("Grains and Pulses", R.drawable.ic_grains_pulses));

        // Post the list to our LiveData stream.
        _categories.setValue(staticList);
    }
}