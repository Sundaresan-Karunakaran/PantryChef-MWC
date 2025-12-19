package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.stepappv3.database.ingredient.MasterIngredient;
import com.example.stepappv3.database.pantry.PantryItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PantryAddItemManualFragment extends DialogFragment {

    private PantryViewModel pantryViewModel;

    private AutoCompleteTextView nameAutoCompleteTextView;
    private AutoCompleteTextView categoryAutoComplete;
    private TextInputEditText quantityEditText;
    private AutoCompleteTextView unitAutoComplete;
    private Button cancelButton;
    private Button saveButton;
    private boolean isEditMode = false;
    private int editingItemId = -1;
    private ArrayAdapter<String> autoCompleteAdapter;
    private MasterIngredient selectedIngredient = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pantry_add_manual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireParentFragment()).get(PantryViewModel.class);
        initializeViews(view);
        setupAutoComplete();
        setupObservers();
        setupCategoryDropdown();
        setupUnitDropdown();
        setupClickListeners();
        checkForEditMode();
    }

    private void initializeViews(View view) {
        nameAutoCompleteTextView = view.findViewById(R.id.name_input_edittext);
        categoryAutoComplete = view.findViewById(R.id.category_input_autocomplete);
        quantityEditText = view.findViewById(R.id.quantity_input_edittext);
        unitAutoComplete = view.findViewById(R.id.unit_input_autocomplete);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.save_button);
    }
    private void setupAutoComplete() {
        autoCompleteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        nameAutoCompleteTextView.setAdapter(autoCompleteAdapter);

        nameAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedIngredient = null;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    pantryViewModel.searchMasterIngredients(s.toString());
                }
            }
        });

        nameAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            if (pantryViewModel.searchResults.getValue() != null) {
                pantryViewModel.searchResults.getValue().stream()
                        .filter(ingredient -> ingredient.name.equals(selectedName))
                        .findFirst()
                        .ifPresent(ingredient -> selectedIngredient = ingredient);
            }
        });
    }

    private void setupObservers() {
        pantryViewModel.searchResults.observe(getViewLifecycleOwner(), masterIngredients -> {
            if (masterIngredients != null) {
                autoCompleteAdapter.clear();
                List<String> names = masterIngredients.stream()
                        .map(ingredient -> ingredient.name)
                        .collect(Collectors.toList());
                autoCompleteAdapter.addAll(names);
                autoCompleteAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupCategoryDropdown() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
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
        String[] units = new String[]{"g", "ml", "No Unit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                units
        );
        unitAutoComplete.setAdapter(adapter);
    }


    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            savePantryItem();
        });
    }

    private void savePantryItem() {
        String category = categoryAutoComplete.getText().toString().trim();
        String quantityStr = quantityEditText.getText().toString().trim();
        String unit = unitAutoComplete.getText().toString().trim();
        String nameInput = nameAutoCompleteTextView.getText().toString().trim();
        if (selectedIngredient == null) {
            // Get the current list of search results from the ViewModel.
            List<MasterIngredient> currentResults = pantryViewModel.searchResults.getValue();
            if (currentResults != null) {
                currentResults.stream()
                        .filter(ingredient -> ingredient.name.equalsIgnoreCase(nameInput))
                        .findFirst()
                        .ifPresent(matchedIngredient -> selectedIngredient = matchedIngredient);
            }
        }
        if (selectedIngredient == null) {
            nameAutoCompleteTextView.setError("Please select a valid ingredient from the list.");
            Toast.makeText(getContext(), "Please select a valid ingredient", Toast.LENGTH_SHORT).show();
            return;
        }
        nameAutoCompleteTextView.setError(null);

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

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            quantityEditText.setError("Invalid number");
            return;
        }
        String currentUserId = pantryViewModel.getUserId();

        if (isEditMode) {
            PantryItem updatedItem = new PantryItem(selectedIngredient.id, category, quantity, unit, currentUserId);
            updatedItem.masterIngredientId = editingItemId;
            pantryViewModel.updatePantryItem(updatedItem);
            Toast.makeText(getContext(), selectedIngredient.name + " updated", Toast.LENGTH_SHORT).show();
        } else {
            PantryItem newItem = new PantryItem(
                    selectedIngredient.id,
                    category,
                    quantity,
                    unit,
                    currentUserId
            );

            pantryViewModel.insertPantryItem(newItem);
            Toast.makeText(getContext(), selectedIngredient.name + " added to pantry", Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }


    private void checkForEditMode() {
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = true;
            editingItemId = args.getInt("ITEM_MASTER_ID", -1);
            String itemName = args.getString("ITEM_NAME", "");
            Log.d("Master ID", String.valueOf(editingItemId));
            nameAutoCompleteTextView.setText(itemName);
            quantityEditText.setText(String.valueOf(args.getInt("ITEM_QUANTITY")));
            unitAutoComplete.setText(args.getString("ITEM_UNIT"), false);
            categoryAutoComplete.setText(args.getString("ITEM_CATEGORY"), false);
            selectedIngredient = new MasterIngredient(itemName);
            selectedIngredient.id = editingItemId;
            nameAutoCompleteTextView.setEnabled(false);
        }
    }
}