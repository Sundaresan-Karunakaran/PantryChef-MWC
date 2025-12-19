package com.example.stepappv3.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.stepappv3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileSetupFragment extends Fragment {

    private ProfileSetupViewModel viewModel;
    private RadioGroup genderRadioGroup;
    private TextInputEditText ageEditText, weightEditText, heightEditText;
    private AutoCompleteTextView activityLevelAutoComplete;
    private MaterialButton saveProfileButton;
    private View loadingOverlay;
    private View loadingSpinner;
    private TextView loadingTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile_setup, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileSetupViewModel.class);
        initializeViews(view);
        setupDropdown();
        setupClickListeners();
        setupObservers();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                android.widget.Toast.makeText(getContext(), "Please complete your profile to continue.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews(View view) {
        genderRadioGroup = view.findViewById(R.id.gender_radiogroup);
        ageEditText = view.findViewById(R.id.age_edittext);
        weightEditText = view.findViewById(R.id.weight_edittext);
        heightEditText = view.findViewById(R.id.height_edittext);
        activityLevelAutoComplete = view.findViewById(R.id.activity_level_autocomplete);
        saveProfileButton = view.findViewById(R.id.save_profile_button);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        loadingTextView = view.findViewById(R.id.loading_text);

    }

    private void setupDropdown() {
        String[] activityLevels = getResources().getStringArray(R.array.activity_level_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, activityLevels);
        activityLevelAutoComplete.setAdapter(adapter);
    }

    private void setupClickListeners() {
        saveProfileButton.setOnClickListener(v -> {
            saveProfileData();
        });
    }

    private void saveProfileData() {
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        MaterialRadioButton selectedGenderButton = genderRadioGroup.findViewById(selectedGenderId);
        String gender = "";
        if (selectedGenderButton != null) {
            gender = selectedGenderButton.getText().toString().equalsIgnoreCase("Male") ? "m" : "f";
        }

        String age = ageEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        String height = heightEditText.getText().toString();
        String activityLevel = activityLevelAutoComplete.getText().toString();

        viewModel.saveProfile(gender, age, weight, height, activityLevel);
    }

    private void setupObservers() {
        viewModel.getProfileSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid();
                NavDirections action =
                        ProfileSetupFragmentDirections.actionProfileSetupFragmentToNavigationHome();
                Navigation.findNavController(requireView()).navigate(action);
            }
        });

        viewModel.isDataReady.observe(getViewLifecycleOwner(), isReady -> {
            if (isReady != null && isReady) {
                loadingOverlay.setVisibility(View.GONE);
                loadingSpinner.setVisibility(View.GONE);
                saveProfileButton.setEnabled(true);
            } else {
                loadingOverlay.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                saveProfileButton.setEnabled(false);
            }
        });

        viewModel.loadingText.observe(getViewLifecycleOwner(), text -> {
            if (text != null) {
                loadingTextView.setText(text);
            }
        });
    }
}