package com.example.stepappv3.ui.pantry;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PantryListViewModel extends AndroidViewModel {

    // The public, read-only LiveData the Fragment will observe.
    public final LiveData<List<PantryItemDisplay>> pantryItems;

    private final StepRepository repository;
    private final String userId;

    public PantryListViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        this.repository = new StepRepository(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();
        } else {
            throw new IllegalStateException("User cannot be null in PantryListViewModel");
        }
        String categoryName = savedStateHandle.get("categoryName");
        if (categoryName != null) {
            this.pantryItems = repository.getItemsByCategoryUser(categoryName, this.userId);
        } else {
            this.pantryItems = repository.getAllPantryItemsUser(this.userId);
        }
    }



}