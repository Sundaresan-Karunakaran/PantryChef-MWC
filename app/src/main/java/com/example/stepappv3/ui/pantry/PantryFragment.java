package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;
import com.example.stepappv3.database.PantryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Fragment for Pantry screen.
 * Shows items list and allows adding / increasing / decreasing / deleting.
 */
public class PantryFragment extends Fragment implements PantryAdapter.PantryItemListener {

    private PantryViewModel viewModel;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialButton buttonAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerPantry);
        buttonAdd = view.findViewById(R.id.buttonAddPantryItem);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        // Observe LiveData
        viewModel.getAllItems().observe(getViewLifecycleOwner(),
                new Observer<List<PantryItem>>() {
                    @Override
                    public void onChanged(List<PantryItem> pantryItems) {
                        adapter.setItems(pantryItems);
                    }
                });

        // Add item button
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
            }
        });
    }

    private void showAddItemDialog() {
        if (getContext() == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_pantry_item, null);

        final EditText editName = dialogView.findViewById(R.id.editItemName);
        final EditText editQuantity = dialogView.findViewById(R.id.editItemQuantity);
        final Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);

        // Units: piece, g, ml
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.pantry_units_array,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(spinnerAdapter);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add Pantry Item")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    String name = editName.getText().toString().trim();
                    String quantityStr = editQuantity.getText().toString().trim();
                    String unit = spinnerUnit.getSelectedItem().toString();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr)) {
                        Toast.makeText(getContext(),
                                "Name and quantity are required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double quantity;
                    try {
                        quantity = Double.parseDouble(quantityStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(),
                                "Invalid quantity",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // fromRecipe = false because user adds manually here
                    PantryItem item = new PantryItem(name, quantity, unit, false);
                    viewModel.insert(item);
                })
                .show();
    }

    // Called when user taps "+" on a row
    @Override
    public void onIncreaseClicked(PantryItem item) {
        double q = item.getQuantity();
        item.setQuantity(q + 1.0);
        viewModel.update(item);
    }

    // Called when user taps "-" on a row
    @Override
    public void onDecreaseClicked(PantryItem item) {
        double q = item.getQuantity();
        q = q - 1.0;
        if (q <= 0) {
            // If quantity goes to zero or below, delete item
            viewModel.delete(item);
        } else {
            item.setQuantity(q);
            viewModel.update(item);
        }
    }

    // Called when user taps "X" on a row
    @Override
    public void onDeleteClicked(PantryItem item) {
        viewModel.delete(item);
    }
}
