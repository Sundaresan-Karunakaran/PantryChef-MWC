package com.example.stepappv3.ui.scan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stepappv3.R;
import com.example.stepappv3.ReceiptScannerActivity;
import com.google.android.material.button.MaterialButton;

public class ScanFragment extends Fragment {

    public ScanFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // BURADA fragment_scan inflate ediyoruz
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // layout'taki butonu bul
        MaterialButton buttonOpenScanner = view.findViewById(R.id.buttonOpenScanner);

        // ReceiptScannerActivity'yi aç
        buttonOpenScanner.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReceiptScannerActivity.class);
            startActivity(intent);
        });
    }
}

