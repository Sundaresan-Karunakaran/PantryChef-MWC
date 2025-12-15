package com.example.stepappv3.ui.step;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.sensors.StepDetector;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StepFragment extends Fragment {

    private TextView stepsDisplay;
    private MaterialButton btnStartLarge;
    private LinearLayout layoutRunningButtons;
    private MaterialButton btnPauseResume;
    private MaterialButton btnFinish;

    private StepDetector stepDetector;
    private StepViewModel stepViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stepsDisplay = view.findViewById(R.id.steps);
        btnStartLarge = view.findViewById(R.id.btnStartLarge);
        layoutRunningButtons = view.findViewById(R.id.layoutRunningButtons);
        btnPauseResume = view.findViewById(R.id.btnPauseResume);
        btnFinish = view.findViewById(R.id.btnFinish);

        stepDetector = new StepDetector(requireContext());
        stepViewModel = new ViewModelProvider(this).get(StepViewModel.class);

        setupPermissions();
        setupObservers();
        setupClickListeners();
    }

    // ... setupPermissions() aynı kalacak ...

    private void setupObservers() {
        // 1. OTURUM DURUMU (Start'a basıldı mı?)
        stepViewModel.isSessionActive.observe(getViewLifecycleOwner(), isActive -> {
            if (isActive) {
                btnStartLarge.setVisibility(View.GONE);
                layoutRunningButtons.setVisibility(View.VISIBLE);
            } else {
                btnStartLarge.setVisibility(View.VISIBLE);
                layoutRunningButtons.setVisibility(View.GONE);
            }
        });

        // 2. SAYMA DURUMU (Pause / Resume Mantığı ve İKON DEĞİŞİMİ)
        stepViewModel.isCounting.observe(getViewLifecycleOwner(), isCounting -> {
            if (isCounting) {
                // --- KOŞUYOR (RUNNING) ---
                stepDetector.start();

                btnPauseResume.setText("PAUSE");
                // PAUSE rengi: #FF4D00
                btnPauseResume.setBackgroundColor(Color.parseColor("#FF4D00"));
                btnPauseResume.setIconResource(R.drawable.ic_pause);

            } else {
                // --- DURAKLATILDI (PAUSED) ---
                stepDetector.stop();

                btnPauseResume.setText("RESUME");
                // RESUME rengi: #FF4D00
                btnPauseResume.setBackgroundColor(Color.parseColor("#FF4D00"));
                btnPauseResume.setIconResource(R.drawable.ic_play);
            }

        });

        // 3. ADIM SAYISI
        stepViewModel.steps.observe(getViewLifecycleOwner(), stepCount -> {
            stepsDisplay.setText(String.valueOf(stepCount));
        });

        stepDetector.setOnStepDetectedListener(() -> {
            stepViewModel.onCountClicked();
        });
    }

    // ... setupClickListeners(), checkPermissionAndStart(), ve Diyalog metodları aynı kalacak ...
    // (Aşağıdaki kısım önceki cevaptaki kodun aynısıdır, tekrar eklemeye gerek yok ama bütünlük için bırakıyorum)

    private void setupPermissions() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                stepViewModel.startCounting();
            } else {
                Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnStartLarge.setOnClickListener(v -> checkPermissionAndStart());
        btnPauseResume.setOnClickListener(v -> stepViewModel.togglePause());
        btnFinish.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(stepViewModel.isCounting.getValue())) {
                stepViewModel.togglePause();
            }
            showFinishConfirmationDialog();
        });
    }

    private void checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                stepViewModel.startCounting();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION);
            }
        } else {
            stepViewModel.startCounting();
        }
    }

    private void showFinishConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Finish Activity?")
                .setMessage("Are you sure you want to finish this run?")
                .setNegativeButton("No", (dialog, which) -> stepViewModel.startCounting())
                .setPositiveButton("Yes", (dialog, which) -> showSaveConfirmationDialog())
                .show();
    }

    private void showSaveConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save Activity")
                .setMessage("Do you want to save this activity?")
                .setNegativeButton("No", (dialog, which) -> stepViewModel.finishAndDiscard())
                .setPositiveButton("Yes", (dialog, which) -> {
                    stepViewModel.finishAndSave();
                    Toast.makeText(getContext(), "Activity Saved!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}