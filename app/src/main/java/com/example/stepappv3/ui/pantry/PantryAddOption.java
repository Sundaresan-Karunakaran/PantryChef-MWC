package com.example.stepappv3.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stepappv3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PantryAddOption extends BottomSheetDialogFragment {


    public interface OnPantryOptionSelectedListener {
        void onAddManuallySelected();
        void onScanReceiptSelected();
    }

    private OnPantryOptionSelectedListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout you designed for this bottom sheet.
        return inflater.inflate(R.layout.pantry_add_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // STEP 2: Find the views and set click listeners
        view.findViewById(R.id.option_add_manually).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAddManuallySelected();
            }
            dismiss(); // Close the bottom sheet after an option is clicked
        });

        view.findViewById(R.id.option_scan_receipt).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onScanReceiptSelected();
            }
            dismiss(); // Close the bottom sheet
        });
    }

    // This method is called when the fragment attaches to its host (the Activity/parent Fragment).
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        // This is how we get a reference to our listener.
        // We assume the parent Fragment will implement the interface.
        if (getParentFragment() instanceof OnPantryOptionSelectedListener) {
            mListener = (OnPantryOptionSelectedListener) getParentFragment();
        } else {
            // You could also check if the activity implements it
            throw new RuntimeException(getParentFragment().toString()
                    + " must implement OnPantryOptionSelectedListener");
        }
    }
}