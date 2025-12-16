package com.example.stepappv3.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.stepappv3.R;
import com.example.stepappv3.database.user.UserProfile;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    // UI Elemanları
    private ShapeableImageView profileImage;
    // --- YENİ ALAN: textActivityLevel eklendi ---
    private TextView textName, textHeight, textWeight, textAge, textGoal, textDiet, textAllergies, textActivityLevel;
    private MaterialButton btnSettings, btnLogout;

    // Tıklanabilir Kartlar
    // --- YENİ ALAN: cardActivityLevel eklendi ---
    private View cardDiet, cardAllergies, cardActivityLevel;

    // Fotoğraf Seçici
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // --- SEÇENEK LİSTELERİ ---
    private final String[] DIET_OPTIONS = {"None (Omnivore)", "Vegan", "Vegetarian", "Pescatarian", "Keto"};
    private final String[] GOAL_OPTIONS = {"Lose Weight", "Maintain Weight", "Gain Weight"};
    private final String[] ALLERGY_OPTIONS = {"Gluten", "Nuts", "Lactose", "Soy", "Seafood", "Egg"};
    // --- YENİ LİSTE: Aktivite Seviyesi Seçenekleri ---
    private final String[] ACTIVITY_OPTIONS = {"Sedentary", "Light", "Moderate", "Active"};


    // Anlık veriyi tutmak için
    private UserProfile currentUserProfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profileImage.setImageURI(selectedImageUri);
                            // Not: İleride bu resmi Storage'a yükleyip URL'sini kaydetmelisiniz.
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // --- ID EŞLEŞTİRMELERİ ---
        profileImage = view.findViewById(R.id.profileImage);
        textName = view.findViewById(R.id.textName);

        textHeight = view.findViewById(R.id.textHeight);
        textWeight = view.findViewById(R.id.textWeight);
        textAge = view.findViewById(R.id.textAge);
        textGoal = view.findViewById(R.id.textGoal);

        textDiet = view.findViewById(R.id.textDiet);
        textAllergies = view.findViewById(R.id.textAllergies);
        textActivityLevel = view.findViewById(R.id.textActivityLevel); // YENİ EŞLEŞTİRME

        // Ayrı Kartlar
        cardDiet = view.findViewById(R.id.cardDiet);
        cardAllergies = view.findViewById(R.id.cardAllergies);
        cardActivityLevel = view.findViewById(R.id.cardActivityLevel); // YENİ EŞLEŞTİRME

        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        // --- VERİLERİ GÖZLEMLEME ---
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentUserProfile = profile;
                updateUI(profile);
            }
        });

        setupClickListeners();
    }

    private void updateUI(UserProfile profile) {
        textName.setText(profile.name + " " + profile.surname);
        textHeight.setText(String.valueOf(profile.height));
        textWeight.setText(String.valueOf(profile.weight));
        textAge.setText(String.valueOf(profile.age));

        textGoal.setText(profile.goal != null ? profile.goal : "-");
        textDiet.setText(profile.dietType != null ? profile.dietType : "None");

        // --- YENİ ALAN: Activity Level'ı göster ---
        textActivityLevel.setText(profile.activityLevel != null ? profile.activityLevel : "-");

        textAllergies.setText((profile.allergies != null && !profile.allergies.isEmpty()) ? profile.allergies : "None");
    }

    private void setupClickListeners() {
        // Fotoğraf
        profileImage.setOnClickListener(v -> showPhotoOptionsDialog());

        // Metin/Sayı Düzenlemeler
        textName.setOnClickListener(v -> showEditDialog("Update Name", "name", false));

        ((View) textHeight.getParent()).setOnClickListener(v -> showEditDialog("Update Height (cm)", "height", true));
        ((View) textWeight.getParent()).setOnClickListener(v -> showEditDialog("Update Weight (kg)", "weight", true));
        ((View) textAge.getParent()).setOnClickListener(v -> showEditDialog("Update Age", "age", true));

        // Hedef (Goal) - Tekli Seçim
        ((View) textGoal.getParent()).setOnClickListener(v ->
                showSingleChoiceDialog("Update Goal", "goal", GOAL_OPTIONS, textGoal.getText().toString()));

        // --- AYRI KART TIKLAMALARI ---

        // 1. DIET KARTI -> Tekli Seçim
        cardDiet.setOnClickListener(v ->
                showSingleChoiceDialog("Select Diet Type", "dietType", DIET_OPTIONS, textDiet.getText().toString()));

        // --- YENİ TIKLAMA: ACTIVITY LEVEL KARTI -> Tekli Seçim ---
        cardActivityLevel.setOnClickListener(v ->
                showSingleChoiceDialog("Select Activity Level", "activityLevel", ACTIVITY_OPTIONS, textActivityLevel.getText().toString()));


        // 2. ALLERGIES KARTI -> Çoklu Seçim
        cardAllergies.setOnClickListener(v ->
                showAllergiesDialog());

        // --- BUTONLAR ---

        // SETTINGS -> Yeni sayfaya git
        btnSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_settings_page);
        });

        // LOGOUT -> Diyalog aç
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // --- YARDIMCI METODLAR ---

    // Tekli Seçim (Radio)
    private void showSingleChoiceDialog(String title, String fieldName, String[] options, String currentValue) {
        int checkedItem = -1;
        for (int i = 0; i < options.length; i++) {
            // Küçük/büyük harf duyarlılığı olmadan kontrol
            if (options[i].equalsIgnoreCase(currentValue)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selected = options[which];
                    profileViewModel.updateField(fieldName, selected);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Çoklu Seçim (Checkbox)
    private void showAllergiesDialog() {
        boolean[] checkedItems = new boolean[ALLERGY_OPTIONS.length];

        List<String> currentAllergies = new ArrayList<>();
        if (currentUserProfile != null && currentUserProfile.allergies != null && !currentUserProfile.allergies.isEmpty()) {
            String[] split = currentUserProfile.allergies.split(",");
            for (String s : split) currentAllergies.add(s.trim());
        }

        for (int i = 0; i < ALLERGY_OPTIONS.length; i++) {
            if (containsCaseInsensitive(currentAllergies, ALLERGY_OPTIONS[i])) {
                checkedItems[i] = true;
            }
        }

        List<String> selectedItems = new ArrayList<>(currentAllergies);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Allergies")
                .setMultiChoiceItems(ALLERGY_OPTIONS, checkedItems, (dialog, which, isChecked) -> {
                    String item = ALLERGY_OPTIONS[which];
                    if (isChecked) selectedItems.add(item);
                    else selectedItems.removeIf(s -> s.equalsIgnoreCase(item));
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    String result = String.join(",", selectedItems);
                    profileViewModel.updateField("allergies", result);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean containsCaseInsensitive(List<String> list, String value) {
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    // Metin/Sayı Girişi
    private void showEditDialog(String title, String fieldName, boolean isNumber) {
        final EditText input = new EditText(getContext());
        input.setInputType(isNumber ? (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL) : InputType.TYPE_CLASS_TEXT);

        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50; params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Update", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        if (isNumber) {
                            try {
                                double numVal = Double.parseDouble(value);
                                if (fieldName.equals("age")) {
                                    profileViewModel.updateField(fieldName, (int)numVal);
                                } else {
                                    profileViewModel.updateField(fieldName, numVal);
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            profileViewModel.updateField(fieldName, value);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Fotoğraf Seçenekleri
    private void showPhotoOptionsDialog() {
        String[] options = {"Change Photo", "Remove Photo"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else {
                        profileImage.setImageResource(R.drawable.ic_launcher_foreground);
                        Toast.makeText(getContext(), "Photo removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerLauncher.launch(intent);
    }

    // Çıkış
    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log Out", (dialog, which) -> {
                    AuthUI.getInstance()
                            .signOut(requireContext())
                            .addOnCompleteListener(task -> {
                                Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
                            });
                })
                .show();
    }
}