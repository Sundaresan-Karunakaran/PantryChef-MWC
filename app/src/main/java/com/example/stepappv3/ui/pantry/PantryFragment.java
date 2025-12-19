package com.example.stepappv3.ui.pantry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;
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
    private ActivityResultLauncher<String> pickImageLauncher;


    private void setupRecyclerView() {
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new PantryCategoryAdapter(this);
        categoryRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(PantryCategory category) {
        PantryFragmentDirections.ActionPantryFragmentToPantryListFragment action =
                PantryFragmentDirections.actionPantryFragmentToPantryListFragment(category.getName());
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(action);
    }





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                pantryViewModel.parseReceiptImage(bitmap);
            } else {
                Toast.makeText(getContext(), "No image captured.", Toast.LENGTH_SHORT).show();
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                takePictureLauncher.launch(null);
            } else {
                Toast.makeText(getContext(), "Camera permission denied. Cannot scan receipt.", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), uri));
                    Log.d("ImageDecoder", "Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    pantryViewModel.parseReceiptImage(bitmap);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No image selected.", Toast.LENGTH_SHORT).show();
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
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        addPantryItemFab = view.findViewById(R.id.add_pantry_item_fab);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
    }
    private void setupObservers() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) {
                categoryRecyclerView.setVisibility(View.GONE);
            } else {
                categoryRecyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(categories);
            }
        });

        pantryViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                Toast.makeText(getContext(), "Scanning receipt...", Toast.LENGTH_SHORT).show();
            }
        });

        pantryViewModel.itemsAddedEvent.observe(getViewLifecycleOwner(), event -> {
            Integer itemsAddedCount = event.getContentIfNotHandled();
            if (itemsAddedCount != null && itemsAddedCount > 0) {
                Toast.makeText(getContext(), itemsAddedCount + " items added to your pantry!", Toast.LENGTH_LONG).show();
            }
        });

        pantryViewModel.errorMessage.observe(getViewLifecycleOwner(), event -> {
            String error = event.getContentIfNotHandled();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onAddManuallySelected() {
        PantryAddItemManualFragment dialogFragment = new PantryAddItemManualFragment();
        dialogFragment.show(getChildFragmentManager(), "AddPantryItemDialog");
    }


    public void onScanReceiptSelected() {
        showImageSourceOptions();
    }

    private void setupClickListeners() {
        addPantryItemFab.setOnClickListener(v -> {
            PantryAddOption bottomSheet = new PantryAddOption();
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
    }
    private void launchCameraForReceipt() {
        if (ContextCompat.checkSelfPermission( requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null);
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceOptions() {
        final CharSequence[] options = {"Take a picture", "Choose from gallery"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scan Receipt")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCameraForReceipt();
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }
}