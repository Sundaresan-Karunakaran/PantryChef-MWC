package com.example.stepappv3.ui.pantry;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.BuildConfig;
import com.example.stepappv3.R;
import com.example.stepappv3.database.StepDatabase;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.database.ingredient.MasterIngredient;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.pantry.ParsedReceiptItem;
import com.example.stepappv3.gemini.GeminiReceiptParser;
import com.example.stepappv3.util.Event;
import com.example.stepappv3.util.ParseResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class PantryViewModel extends AndroidViewModel {

    public final LiveData<List<PantryCategory>> categories;
    private final MutableLiveData<List<PantryCategory>> _categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<Event<Integer>> _itemsAddedEvent = new MutableLiveData<>();
    public final LiveData<Event<Integer>> itemsAddedEvent = _itemsAddedEvent;
    private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
    public final LiveData<Event<String>> errorMessage = _errorMessage;
    private GeminiReceiptParser geminiParser;
    private StepRepository repo;
    private final Executor backgroundExecutor;
    private String userId;
    private List<MasterIngredient> masterIngredients = null;


    private final MutableLiveData<List<MasterIngredient>> _searchResults = new MutableLiveData<>();
    public final LiveData<List<MasterIngredient>> searchResults = _searchResults;



    private void ensureMasterLoaded(Runnable onLoaded) {
        if (masterIngredients != null) {
            onLoaded.run();
        } else {
            backgroundExecutor.execute(() -> {
                masterIngredients = repo.getAllMasterIngredients();
                onLoaded.run();
            });
        }
    }


    public void parseReceiptImage(Bitmap bitmap) {
        if (userId == null) {
            _errorMessage.setValue(new Event<>("You must be logged in to scan a receipt."));
            return;
        }
        _isLoading.setValue(true);

        geminiParser.parseReceipt(bitmap).whenComplete((parseResult, throwable) -> {
            if (throwable != null) {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>("Error parsing receipt: " + throwable.getMessage()));
                return;
            }

            if (parseResult.status == ParseResult.Status.SUCCESS) {
                backgroundExecutor.execute(() -> {
                    try {
                        ensureMasterLoaded(() -> {
                            List<PantryItem> itemsToInsert = new ArrayList<>();
                            List<String> unrecognizedItems = new ArrayList<>();
                            for (ParsedReceiptItem parsedItem : parseResult.items) {
                                String normalizedInput = parsedItem.name.toLowerCase().trim();
                                MasterIngredient matchedIngredient = null;

                                for (MasterIngredient master : masterIngredients) {
                                    if (normalizedInput.contains(master.name.toLowerCase())) {
                                        matchedIngredient = master;
                                        break;
                                    }
                                }

                                if (matchedIngredient != null) {
                                    try {
                                        int quantity = parsedItem.quantity;
                                        PantryItem newItem = new PantryItem(matchedIngredient.id, parsedItem.category, quantity, parsedItem.unit, userId);
                                        itemsToInsert.add(newItem);
                                    } catch (NumberFormatException e) {
                                        Log.w("PantryViewModel", "Could not parse quantity for: " + parsedItem.name);
                                        unrecognizedItems.add(parsedItem.name + " (invalid quantity)");
                                    }
                                } else {
                                    unrecognizedItems.add(parsedItem.name);
                                }
                            }

                            if (!itemsToInsert.isEmpty()) {
                                repo.insertAllItems(itemsToInsert);
                                _itemsAddedEvent.postValue(new Event<>(itemsToInsert.size()));
                            }
                            if (!unrecognizedItems.isEmpty()) {
                                _errorMessage.postValue(new Event<>("Could not recognize: " + String.join(", ", unrecognizedItems)));
                            }
                        });
                    } finally {
                        _isLoading.postValue(false);
                    }
                });
            } else {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>(parseResult.errorMessage));
            }
        });
    }


    public PantryViewModel(@NonNull Application application) {
        super(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();
        }
        this.backgroundExecutor = StepDatabase.databaseWriteExecutor;
        this.repo = new StepRepository(application);
        this.geminiParser = new GeminiReceiptParser(BuildConfig.GEMINI_API_KEY);
        categories = _categories;
        loadStaticCategories();
    }

    private void loadStaticCategories() {
        List<PantryCategory> staticList = new ArrayList<>();
        staticList.add(new PantryCategory("Dairy & Eggs", R.drawable.ic_dairy_eggs));
        staticList.add(new PantryCategory("Vegetables", R.drawable.ic_veg));
        staticList.add(new PantryCategory("Bakery", R.drawable.ic_bread));
        staticList.add(new PantryCategory("Meats & Seafood", R.drawable.ic_meat));
        staticList.add(new PantryCategory("Spices", R.drawable.ic_spices));
        staticList.add(new PantryCategory("Grains and Pulses", R.drawable.ic_grains_pulses));
        staticList.add(new PantryCategory("Fruits", R.drawable.ic_fruits));
        staticList.add(new PantryCategory("Oils", R.drawable.ic_oil));
        _categories.setValue(staticList);
    }
    public void insertPantryItem(PantryItem item) {
        backgroundExecutor.execute(() -> {
            this.repo.insertPantryItem(item);
        });
    }
    public String getUserId() {
        return userId;
    }

    public void searchMasterIngredients(String query) {
        backgroundExecutor.execute(() ->{
            List<MasterIngredient> results = this.repo.searchIngredientsByName(query);
            _searchResults.postValue(results);
        });
    }

    public void updatePantryItem(PantryItem item) {
        backgroundExecutor.execute(() -> {
            if(item.quantity == 0){
                this.repo.deleteByIdUser(item.masterIngredientId,userId);
                return;
            }
            else {
                this.repo.updatePantryItem(item);
            }

        });
    }

}