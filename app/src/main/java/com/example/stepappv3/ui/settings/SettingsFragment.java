package com.example.stepappv3.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.stepappv3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private RadioGroup themeRadioGroup;
    private MaterialButton btnDeleteAccount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeRadioGroup = view.findViewById(R.id.themeRadioGroup);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        setupThemeSelection();

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // --- 1. TEMA AYARLARI ---
    private void setupThemeSelection() {
        // Mevcut temayı kontrol et ve doğru butonu işaretle
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeRadioGroup.check(R.id.radioDark);
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeRadioGroup.check(R.id.radioLight);
        } else {
            themeRadioGroup.check(R.id.radioSystem);
        }

        // Seçim değiştiğinde temayı değiştir
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            // Not: Tema değişince Activity otomatik yeniden başlar, bu normaldir.
        });
    }

    // --- 2. HESAP SİLME ---
    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account permanently? All your data (steps, profile) will be lost.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteUserAccount();
                })
                .show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 1. Önce Veritabanındaki veriyi sil
            db.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 2. Sonra Auth hesabını sil
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Account deleted.", Toast.LENGTH_SHORT).show();
                                        // Giriş ekranına at
                                        Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
                                    } else {
                                        Toast.makeText(getContext(), "Failed to delete account. Please log in again and try.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error removing user data.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}