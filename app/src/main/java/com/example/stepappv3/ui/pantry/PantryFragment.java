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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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



public class PantryFragment extends Fragment implements PantryAddOption.OnPantryOptionSelectedListener, PantryCategoryAdapter.OnCategoryClickListener  {

    private PantryViewModel pantryViewModel;
    private RecyclerView categoryRecyclerView;
    private PantryCategoryAdapter adapter;
    private LinearLayout emptyPantryView;
    private FloatingActionButton addPantryItemFab;
    private ActivityResultLauncher<Void> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;


    private void setupRecyclerView() {
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 2. Pass 'this' (the fragment) as the listener when creating the adapter.
        adapter = new PantryCategoryAdapter(this);

        categoryRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(PantryCategory category) {
        // We have received the click event and the data for the clicked category.
        // Now, we perform the navigation.

        // Use the auto-generated Directions class for type-safe navigation
        PantryFragmentDirections.ActionPantryFragmentToPantryListFragment action =
                PantryFragmentDirections.actionPantryFragmentToPantryListFragment(category.getName());

        // Find the NavController and navigate
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(action);
    }





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



    // In PantryFragment.java

    private void setupObservers() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) {
                categoryRecyclerView.setVisibility(View.GONE);
                // emptyPantryView.setVisibility(View.VISIBLE);
            } else {
                categoryRecyclerView.setVisibility(View.VISIBLE);
                // emptyPantryView.setVisibility(View.GONE);

                // 4. Use the ListAdapter's built-in submitList method.
                adapter.submitList(categories);
            }
        });

        pantryViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                // When the API call is in progress, show a simple message.
                // In a more advanced UI, you would show a real ProgressBar.
                Toast.makeText(getContext(), "Scanning receipt...", Toast.LENGTH_SHORT).show();
            }
        });

        pantryViewModel.itemsAddedEvent.observe(getViewLifecycleOwner(), event -> {
            Integer itemsAddedCount = event.getContentIfNotHandled(); // Consume the event
            if (itemsAddedCount != null && itemsAddedCount > 0) {
                // Show a professional success message to the user!
                Toast.makeText(getContext(), itemsAddedCount + " items added to your pantry!", Toast.LENGTH_LONG).show();
                // In a real app, you might refresh the pantry list here.
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

    // In PantryFragment.java

    @Override
    public void onAddManuallySelected() {
        // Instead of a toast, we now launch our new dialog fragment.
        PantryAddItemManualFragment dialogFragment = new PantryAddItemManualFragment();
        // We use getChildFragmentManager() because this dialog is a child of the PantryFragment.
        dialogFragment.show(getChildFragmentManager(), "AddPantryItemDialog");
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