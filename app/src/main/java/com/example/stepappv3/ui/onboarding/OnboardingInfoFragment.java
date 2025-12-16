package com.example.stepappv3.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.stepappv3.R;
import com.google.android.material.button.MaterialButton;

public class OnboardingInfoFragment extends Fragment {
    private OnboardingViewModel viewModel;
    private EditText name, surname, age, weight, height;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        name = view.findViewById(R.id.inputName);
        surname = view.findViewById(R.id.inputSurname);
        age = view.findViewById(R.id.inputAge);
        weight = view.findViewById(R.id.inputWeight);
        height = view.findViewById(R.id.inputHeight);

        MaterialButton next = view.findViewById(R.id.btnNext);
        // Skip butonu tanımlaması SİLİNDİ

        next.setOnClickListener(v -> {
            saveData();
            Navigation.findNavController(v).navigate(R.id.action_info_to_allergies);
        });

        // Skip listener SİLİNDİ
    }

    private void saveData() {
        viewModel.tempProfile.name = name.getText().toString();
        viewModel.tempProfile.surname = surname.getText().toString();
        try { viewModel.tempProfile.age = Integer.parseInt(age.getText().toString()); } catch(Exception e){}
        try { viewModel.tempProfile.weight = Double.parseDouble(weight.getText().toString()); } catch(Exception e){}
        try { viewModel.tempProfile.height = Double.parseDouble(height.getText().toString()); } catch(Exception e){}
    }
}