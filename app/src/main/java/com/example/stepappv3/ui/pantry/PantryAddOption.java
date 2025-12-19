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
        return inflater.inflate(R.layout.pantry_add_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.option_add_manually).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAddManuallySelected();
            }
            dismiss();
        });

        view.findViewById(R.id.option_scan_receipt).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onScanReceiptSelected();
            }
            dismiss();
        });
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnPantryOptionSelectedListener) {
            mListener = (OnPantryOptionSelectedListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment().toString()
                    + " must implement OnPantryOptionSelectedListener");
        }
    }
}