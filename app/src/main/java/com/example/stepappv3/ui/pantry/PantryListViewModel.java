package com.example.stepappv3.ui.pantry;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.pantry.PantryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PantryListViewModel extends AndroidViewModel {

    // The public, read-only LiveData the Fragment will observe.
    public final LiveData<List<PantryItem>> pantryItems;

    private final StepRepository repository;
    private final String userId;

    public PantryListViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        this.repository = new StepRepository(application);

        // 1. Get the current user's ID directly from Firebase.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();
        } else {
            // This is a defensive check. In your app's flow, a user should always exist here.
            // We'll throw an exception to fail fast if this state is ever reached.
            throw new IllegalStateException("User cannot be null in PantryListViewModel");
        }

        // 2. Get the categoryName from the navigation arguments via the SavedStateHandle.
        // The key "categoryName" must exactly match the name you defined in mobile_navigation.xml.
        String categoryName = savedStateHandle.get("categoryName");

        // 3. Use the userId and categoryName to fetch the correct data.
        if (categoryName != null) {
            this.pantryItems = repository.getItemsByCategoryUser(categoryName, this.userId);
        } else {
            // This would happen if the argument wasn't passed correctly.
            // We can fall back to loading all items or an empty list.
            this.pantryItems = repository.getAllPantryItemsUser(this.userId);
        }
    }

    public void updatePantryItem(PantryItem item) {
        // We delegate the task of performing the database operation
        // on a background thread to the repository.
        repository.updatePantryItem(item);
    }

}