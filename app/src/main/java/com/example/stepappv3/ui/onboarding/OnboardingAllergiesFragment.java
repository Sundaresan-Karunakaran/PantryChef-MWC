package com.example.stepappv3.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.stepappv3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class OnboardingAllergiesFragment extends Fragment {
    private OnboardingViewModel viewModel;
    private ChipGroup chipGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_allergies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        chipGroup = view.findViewById(R.id.chipGroupAllergies);

        MaterialButton next = view.findViewById(R.id.btnNext);
        // Skip butonu tanımlaması SİLİNDİ

        next.setOnClickListener(v -> {
            saveData();
            Navigation.findNavController(v).navigate(R.id.action_allergies_to_diet);
        });

        // Skip listener SİLİNDİ
    }

    private void saveData() {
        List<String> allergies = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) allergies.add(chip.getText().toString());
        }
        viewModel.tempProfile.allergies = String.join(",", allergies);
    }
}