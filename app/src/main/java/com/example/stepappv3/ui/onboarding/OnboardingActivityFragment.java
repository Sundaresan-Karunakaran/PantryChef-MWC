package com.example.stepappv3.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.stepappv3.R;

public class OnboardingActivityFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private RadioGroup radioGroup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        radioGroup = view.findViewById(R.id.radioGroupActivity);

        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String activityLevel = "";

            if (selectedId == R.id.rbSedentary) activityLevel = "Sedentary";
            else if (selectedId == R.id.rbLight) activityLevel = "Light";
            else if (selectedId == R.id.rbModerate) activityLevel = "Moderate";
            else if (selectedId == R.id.rbActive) activityLevel = "Active";

            if (activityLevel.isEmpty()) {
                Toast.makeText(getContext(), "Please select an activity level", Toast.LENGTH_SHORT).show();
                return;
            }

            // DÜZELTME BURADA: getUserProfile() yerine tempProfile kullanıyoruz
            if (viewModel.tempProfile != null) {
                viewModel.tempProfile.activityLevel = activityLevel;
            }

            // Bir sonraki sayfaya (Goal) geç
            Navigation.findNavController(view).navigate(R.id.action_activity_to_goal);
        });
    }
}