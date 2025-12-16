package com.example.stepappv3.ui.pantry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

public class PantryFragment extends Fragment implements PantryAddOption.OnPantryOptionSelectedListener, PantryCategoryAdapter.OnCategoryClickListener {

    private PantryViewModel pantryViewModel;
    private RecyclerView categoryRecyclerView;
    private PantryCategoryAdapter adapter;
    // private LinearLayout emptyPantryView; // Layout dosyanızda varsa bunu açın
    private FloatingActionButton addPantryItemFab;

    // Launcher Tanımları
    private ActivityResultLauncher<Void> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher; // EKLENDİ: Galeri için

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. KAMERA: Fotoğraf çekildikten sonra çalışacak kod
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                pantryViewModel.parseReceiptImage(bitmap);
            } else {
                Toast.makeText(getContext(), "No image captured.", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. İZİN: Kamera izni istendikten sonra çalışacak kod
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                takePictureLauncher.launch(null);
            } else {
                Toast.makeText(getContext(), "Camera permission denied. Cannot scan receipt.", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. GALERİ: Galeriden resim seçildikten sonra çalışacak kod (EKLENDİ)
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    // Modern Android sürümleri için ImageDecoder kullanıyoruz
                    Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), uri));
                    // Seçilen resmi ViewModel'e gönder
                    pantryViewModel.parseReceiptImage(bitmap);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Kullanıcı seçim yapmadan geri döndü
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
        // emptyPantryView = view.findViewById(R.id.empty_pantry_view);
        addPantryItemFab = view.findViewById(R.id.add_pantry_item_fab);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Adapter oluştururken 'this' (Fragment) Listener olarak geçiliyor
        adapter = new PantryCategoryAdapter(this);
        categoryRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        pantryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) {
                categoryRecyclerView.setVisibility(View.GONE);
                // if (emptyPantryView != null) emptyPantryView.setVisibility(View.VISIBLE);
            } else {
                categoryRecyclerView.setVisibility(View.VISIBLE);
                // if (emptyPantryView != null) emptyPantryView.setVisibility(View.GONE);

                // Veriyi ListAdapter'a gönderiyoruz
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

    private void setupClickListeners() {
        addPantryItemFab.setOnClickListener(v -> {
            PantryAddOption bottomSheet = new PantryAddOption();
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    public void onAddManuallySelected() {
        PantryAddItemManualFragment dialogFragment = new PantryAddItemManualFragment();
        dialogFragment.show(getChildFragmentManager(), "AddPantryItemDialog");
    }

    @Override
    public void onScanReceiptSelected() {
        showImageSourceOptions();
    }

    private void launchCameraForReceipt() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
                        // Take a picture
                        launchCameraForReceipt();
                    } else if (which == 1) {
                        // Choose from gallery
                        // DÜZELTME BURADA: Sadece Toast mesajı değil, Launcher çalıştırılıyor.
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    @Override
    public void onCategoryClick(PantryCategory category) {
        // Navigasyon işlemi
        PantryFragmentDirections.ActionPantryFragmentToPantryListFragment action =
                PantryFragmentDirections.actionPantryFragmentToPantryListFragment(category.getName());
        Navigation.findNavController(requireView()).navigate(action);
    }
}