package com.example.stepappv3.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.example.stepappv3.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

// Artık kullanılmadığı için Navigation, MaterialButton, Dialog, Firebase importları silinebilir
// Ancak projenin bütünlüğü için bu importlar diğer modüllerde kullanılıyorsa kalabilir.
// Bu sürümde sadece gerekli olanları bıraktım.

public class SettingsFragment extends Fragment {

    private RadioGroup themeRadioGroup;
    // Hesap silme ile ilgili değişken kaldırıldı.

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeRadioGroup = view.findViewById(R.id.themeRadioGroup);
        // btnDeleteAccount eşleştirmesi kaldırıldı.

        setupThemeSelection();

        // Buton tıklama olayı kaldırıldı.
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
        });
    }

    // Hesap silme ile ilgili hiçbir yardımcı metot bulunmamaktadır.
}