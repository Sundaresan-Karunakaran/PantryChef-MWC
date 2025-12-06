package com.example.stepappv3.ui.pantry;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stepappv3.R;
import com.example.stepappv3.gemini.GeminiReceiptParser;
import com.example.stepappv3.util.Event;

import java.util.ArrayList;
import java.util.List;

public class PantryViewModel extends AndroidViewModel {

    // This is the public, read-only LiveData the Fragment will observe.
    public final LiveData<List<PantryCategory>> categories;

    // This is the private, writable version.
    private final MutableLiveData<List<PantryCategory>> _categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Event<String>> _parsedReceiptText = new MutableLiveData<>();
    public final LiveData<Event<String>> parsedReceiptText = _parsedReceiptText;

    private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
    public final LiveData<Event<String>> errorMessage = _errorMessage;
    private GeminiReceiptParser geminiParser;

    // ADD A LAZY-INITIALIZER GETTER FOR THE PARSER
    private GeminiReceiptParser getParser() {
        if (geminiParser == null) {
            // TODO: Replace gemini-key with a key stored securely, e.g., in BuildConfig
            geminiParser = new GeminiReceiptParser("gemini-key");
        }
        return geminiParser;
    }
    public void parseReceiptImage(Bitmap bitmap) {
        _isLoading.setValue(true); // Signal that loading has started

        getParser().parseReceipt(bitmap).whenComplete((text, throwable) -> {
            // This block executes when the Gemini API call is finished.
            // It's important to use postValue() as this may be on a background thread.
            _isLoading.postValue(false); // Loading is finished

            if (throwable != null) {
                // An error occurred
                _errorMessage.postValue(new Event<>("Error parsing receipt: " + throwable.getMessage()));
            } else {
                // Success! Post the recognized text.
                _parsedReceiptText.postValue(new Event<>(text));
            }
        });
    }

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