package com.example.stepappv3.ui.home;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.sensors.StepDetector;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HomeFragment extends androidx.fragment.app.Fragment {

    private TextView stepsDisplay;
    private MaterialButton startButton;
    private MaterialButton resetButton;
    private StepDetector stepDetector;
    private HomeViewModel home;
    private CircularProgressIndicator stepsProgress;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stepsDisplay = view.findViewById(R.id.steps);
        startButton = view.findViewById(R.id.start);
        resetButton = view.findViewById(R.id.reset);
        stepDetector = new StepDetector(requireContext());
        stepsProgress = view.findViewById(R.id.progressBar);
        home = new ViewModelProvider(this).get(HomeViewModel.class);

        setupObservers();
        setupClickListeners();

    }

    private void setupPermissions() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // The user just granted the permission. Now we can proceed with the action.
                home.onStartStopClicked();
            } else {
                // The user denied the permission. It's good practice to explain
                // why the feature is unavailable.
                Toast.makeText(getContext(), "Permission denied. Step counting cannot start.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        home.isCounting.observe(getViewLifecycleOwner(), counting -> {
            if (counting) {
                startButton.setText("Stop");
                startButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_stop_red));
                stepDetector.start();
            } else {
                startButton.setText("Start");
                startButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_start_green));
                stepDetector.stop();
            }
        });

        home.steps.observe(getViewLifecycleOwner(), stepCount -> {
            stepsDisplay.setText(String.valueOf(stepCount));
        });

        home.progressPercentage.observe(getViewLifecycleOwner(), progress -> {
            stepsProgress.setProgress(progress, true);
        });

        stepDetector.setOnStepDetectedListener(() -> {
            home.onCountClicked();
        });
    }

    private void setupClickListeners() {
        resetButton.setOnClickListener(v -> showResetConfirmationDialog());

        startButton.setOnClickListener(v -> {
            // First, check if we are on Android 10 (Q) or higher.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if we already have the permission.
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, proceed with the original action.
                    home.onStartStopClicked();
                } else {
                    // Permission has not been granted yet. Launch the request.
                    // The result will be handled by the launcher we registered in onCreate().
                    requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION);
                }

            } else {
                // We are on a device older than Android 10.
                // The permission is granted at install time if it's in the manifest.
                // We can proceed directly with the action.
                home.onStartStopClicked();
            }
        });

    }


    private void showResetConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Counter?")
                .setMessage("This will permanently delete all step history. Are you sure?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    home.onResetClicked();
                })
                .show();
    }
}
