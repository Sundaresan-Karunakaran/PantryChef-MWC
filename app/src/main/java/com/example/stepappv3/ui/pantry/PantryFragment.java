package com.example.stepappv3.ui.pantry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stepappv3.R;
import java.util.ArrayList;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.LinearLayout;
import android.widget.Toast;



public class PantryFragment extends Fragment implements PantryAddOption.OnPantryOptionSelectedListener {

    private PantryViewModel pantryViewModel;
    private RecyclerView categoryRecyclerView;
    private PantryCategoryAdapter adapter;
    private LinearLayout emptyPantryView;
    private FloatingActionButton addPantryItemFab;
    private ActivityResultLauncher<Void> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher; // <-- ADD THIS LINE




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This sets up the contract for what to do AFTER the user takes a picture.
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                // Call Gemini from the viewModel to parse the image
                pantryViewModel.parseReceiptImage(bitmap);
            } else {
                // The user cancelled or an error occurred.
                Toast.makeText(getContext(), "No image captured.", Toast.LENGTH_SHORT).show();
            }
        });

        // This sets up the contract for handling the CAMERA PERMISSION request result.
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission was just granted by the user. Now we can launch the camera.
                takePictureLauncher.launch(null);
            } else {
                // The user denied the permission.
                Toast.makeText(getContext(), "Camera permission denied. Cannot scan receipt.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get the ViewModel
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        // 2. Find all the views
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        //emptyPantryView = view.findViewById(R.id.empty_pantry_view);
        addPantryItemFab = view.findViewById(R.id.add_pantry_item_fab);

        // 3. Set up the RecyclerView
        setupRecyclerView();

        // 4. Set up the observer to listen for data changes
        setupObservers();

        // 5. Set up any click listeners
        setupClickListeners();
    }

    private void setupRecyclerView() {
        // The LayoutManager is already set in XML, but it's good practice to be explicit.
        // The span count of 2 creates our 2-column grid.
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize the adapter with an empty list. It will be updated by the observer.
        adapter = new PantryCategoryAdapter(new ArrayList<>());
        categoryRecyclerView.setAdapter(adapter);
    }

    // In PantryFragment.java

    private void setupObservers() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            // This code runs whenever the ViewModel provides a new list.

            // The logic for the empty state is still perfect.
            if (categories == null || categories.isEmpty()) {
                categoryRecyclerView.setVisibility(View.GONE);
                // You'll need to uncomment this line and make sure emptyPantryView is initialized
                // emptyPantryView.setVisibility(View.VISIBLE);
            } else {
                categoryRecyclerView.setVisibility(View.VISIBLE);
                // emptyPantryView.setVisibility(View.GONE);

                // THE FIX: Use our new, efficient update method.
                adapter.updateData(categories);
            }
        });

        pantryViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                // When the API call is in progress, show a simple message.
                // In a more advanced UI, you would show a real ProgressBar.
                Toast.makeText(getContext(), "Scanning receipt...", Toast.LENGTH_SHORT).show();
            }
        });

        pantryViewModel.parsedReceiptText.observe(getViewLifecycleOwner(), event -> {
            // When we get a successful result, show it in a Material Alert Dialog.
            String text = event.getContentIfNotHandled();
            if( text != null && !text.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Scanned Items")
                        .setMessage(text) // Display the text returned by Gemini
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        pantryViewModel.errorMessage.observe(getViewLifecycleOwner(), event -> {
            // If an error occurs, show it to the user in a Toast.
            String error = event.getContentIfNotHandled();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onAddManuallySelected() {
        // TODO: Navigate to the "Add Item Manually" screen
        Toast.makeText(getContext(), "Add Manually Clicked!", Toast.LENGTH_SHORT).show();
    }


    public void onScanReceiptSelected() {
        // TODO: Launch the camera to scan a receipt
        showImageSourceOptions();
    }

    private void setupClickListeners() {
        addPantryItemFab.setOnClickListener(v -> {
            // THE FIX: Launch our new bottom sheet
            PantryAddOption bottomSheet = new PantryAddOption();
            // We use getChildFragmentManager() because the bottom sheet is a child of this fragment
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
    }
    private void launchCameraForReceipt() {
        // Check if we already have the permission.
        if (ContextCompat.checkSelfPermission( requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, launch the camera directly.
            takePictureLauncher.launch(null);
        } else {
            // Permission has not been granted yet. Launch the permission request.
            // The result will be handled by the launcher we registered in onCreate().
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceOptions() {
        // 1. Define the choices that will appear in the dialog.
        final CharSequence[] options = {"Take a picture", "Choose from gallery"};

        // 2. Create the Material-themed dialog builder.
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scan Receipt")
                // 3. Set the items and the click listener for the items.
                //    'setItems' creates a simple, tappable list.
                .setItems(options, (dialog, which) -> {
                    // The 'which' parameter tells you which item was clicked (0 for the first, 1 for the second).
                    if (which == 0) {
                        // "Take a picture" was clicked.
                        launchCameraForReceipt();
                    } else if (which == 1) {
                        // "Choose from gallery" was clicked.
                        // TODO: In a future step, we will implement the gallery launcher here.
                        Toast.makeText(getContext(), "Choose from gallery clicked!", Toast.LENGTH_SHORT).show();
                    }
                })

                // 4. Create and show the dialog.
                .show();
    }
}