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

public class OnboardingDietFragment extends Fragment {
    private OnboardingViewModel viewModel;
    private ChipGroup chipGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_diet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        chipGroup = view.findViewById(R.id.chipGroupDiet);

        MaterialButton next = view.findViewById(R.id.btnNext);
        // Skip butonu tanımlaması SİLİNDİ

        next.setOnClickListener(v -> {
            int id = chipGroup.getCheckedChipId();
            if (id != -1) {
                Chip chip = view.findViewById(id);
                viewModel.tempProfile.dietType = chip.getText().toString();
            }
            Navigation.findNavController(v).navigate(R.id.action_diet_to_goal);
        });

        // Skip listener SİLİNDİ
    }
}