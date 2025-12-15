package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.database.pantry.PantryItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PantryAddItemManualFragment extends DialogFragment {

    private PantryViewModel pantryViewModel;

    private TextInputEditText nameEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private TextInputEditText quantityEditText;
    private AutoCompleteTextView unitAutoComplete;
    private Button cancelButton;
    private Button saveButton;
    private boolean isEditMode = false;
    private int editingItemId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout you designed.
        return inflater.inflate(R.layout.pantry_add_manual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to the parent fragment's ViewModel.
        // This is the correct way for a dialog to share a ViewModel.
        pantryViewModel = new ViewModelProvider(requireParentFragment()).get(PantryViewModel.class);

        // Find all the UI elements from the inflated view.
        nameEditText = view.findViewById(R.id.name_input_edittext);
        categoryAutoComplete = view.findViewById(R.id.category_input_autocomplete);
        quantityEditText = view.findViewById(R.id.quantity_input_edittext);
        unitAutoComplete = view.findViewById(R.id.unit_input_autocomplete);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.save_button);

        // Set up the logic for the views.
        setupCategoryDropdown();
        setupUnitDropdown();
        setupClickListeners();
        checkForEditMode();
    }

    private void setupCategoryDropdown() {
        // We observe the categories from the ViewModel to populate our dropdown.
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                // We need a list of strings for the adapter, not a list of PantryCategory objects.
                List<String> categoryNames = categories.stream()
                        .map(PantryCategory::getName)
                        .collect(Collectors.toList());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, categoryNames);
                categoryAutoComplete.setAdapter(adapter);
            }
        });
    }
    private void setupUnitDropdown() {
        // 1. Define the array of unit options.
        String[] units = new String[]{"g", "ml", "No Unit"};

        // 2. Create the adapter to bridge the data and the UI.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                units
        );

        // 3. Set the adapter on the AutoCompleteTextView.
        unitAutoComplete.setAdapter(adapter);
    }


    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            // When save is clicked, we validate and save the data.
            savePantryItem();
        });
    }

    private void savePantryItem() {
        String name = nameEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        String quantityStr = quantityEditText.getText().toString().trim();
        String unit = unitAutoComplete.getText().toString().trim();

        // --- Data Validation ---
        if (name.isEmpty()) {
            nameEditText.setError("Item name cannot be empty");
            return;
        }
        if (category.isEmpty()) {
            categoryAutoComplete.setError("Please select a category");
            return;
        }
        if (quantityStr.isEmpty()) {
            quantityEditText.setError("Quantity cannot be empty");
            return;
        }
        if (unit.isEmpty()) {
            unitAutoComplete.setError("Please select a unit");
            return;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            nameEditText.setError("Name can only contain letters and spaces");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            quantityEditText.setError("Invalid number");
            return;
        }
        String currentUserId = pantryViewModel.getUserId();
        // --- Save the Item ---
        // Create the new PantryItem object.
        if (isEditMode) {

            PantryItem updatedItem = new PantryItem(name, category, quantity, unit, currentUserId);
            updatedItem.id = editingItemId;
            PantryListViewModel listViewModel = new ViewModelProvider(requireParentFragment()).get(PantryListViewModel.class);
            listViewModel.updatePantryItem(updatedItem);
            Toast.makeText(getContext(), name + " updated", Toast.LENGTH_SHORT).show();
        } else {
            PantryItem newItem = new PantryItem(name, category, quantity, unit, currentUserId);
            pantryViewModel.insertPantryItem(newItem);
            Toast.makeText(getContext(), name + " added to pantry", Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    private void checkForEditMode() {        Bundle args = getArguments();
        if (args != null) {
            // If arguments exist, we are in "Edit Mode".
            isEditMode = true;
            editingItemId = args.getInt("ITEM_ID", -1);

            // Pre-fill the form with the existing item's data.
            nameEditText.setText(args.getString("ITEM_NAME"));
            quantityEditText.setText(String.valueOf(args.getInt("ITEM_QUANTITY")));

            // For AutoCompleteTextView, we must set the text and also show it.
            String unit = args.getString("ITEM_UNIT");
            unitAutoComplete.setText(unit, false);

            String category = args.getString("ITEM_CATEGORY");
            categoryAutoComplete.setText(category, false);

            // We don't pre-fill the category because the user might want to change it,
            // and the dropdown will be populated by the ViewModel.
        }
    }
}