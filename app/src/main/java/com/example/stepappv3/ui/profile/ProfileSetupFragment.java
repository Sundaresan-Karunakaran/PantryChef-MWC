package com.example.stepappv3.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
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

    // 1. Declare member fields for the ViewModel and all UI Views
    private ProfileSetupViewModel viewModel;
    private RadioGroup genderRadioGroup;
    private TextInputEditText ageEditText, weightEditText, heightEditText;
    private AutoCompleteTextView activityLevelAutoComplete;
    private MaterialButton saveProfileButton;
    private View loadingOverlay;
    private View loadingSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 2. Inflate the layout
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Get the ViewModel and find all the views
        viewModel = new ViewModelProvider(this).get(ProfileSetupViewModel.class);
        initializeViews(view);

        // 4. Use helper methods to set up the UI and observers
        setupDropdown();
        setupClickListeners();
        setupObservers();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // By doing nothing here, we effectively disable the back button.
                // For a better user experience, show a Toast message.
                android.widget.Toast.makeText(getContext(), "Please complete your profile to continue.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Finds all the UI components from the layout file.
     */
    private void initializeViews(View view) {
        genderRadioGroup = view.findViewById(R.id.gender_radiogroup);
        ageEditText = view.findViewById(R.id.age_edittext);
        weightEditText = view.findViewById(R.id.weight_edittext);
        heightEditText = view.findViewById(R.id.height_edittext);
        activityLevelAutoComplete = view.findViewById(R.id.activity_level_autocomplete);
        saveProfileButton = view.findViewById(R.id.save_profile_button);
    }

    /**
     * Populates the Activity Level dropdown menu from the string array resource.
     */
    private void setupDropdown() {
        // Get the string array from resources.
        String[] activityLevels = getResources().getStringArray(R.array.activity_level_options);
        // Create the adapter.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, activityLevels);
        // Set the adapter on the AutoCompleteTextView.
        activityLevelAutoComplete.setAdapter(adapter);
    }

    /**
     * Sets the OnClickListener for the save button.
     */
    private void setupClickListeners() {
        saveProfileButton.setOnClickListener(v -> {
            // Get the raw input from the form and command the ViewModel to save.
            saveProfileData();
        });
    }

    /**
     * Gathers all the raw data from the UI fields and passes it to the ViewModel.
     */
    private void saveProfileData() {
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        MaterialRadioButton selectedGenderButton = genderRadioGroup.findViewById(selectedGenderId);
        String gender = "";
        if (selectedGenderButton != null) {
            // We pass "m" or "f" based on the button text, as per your Python logic.
            gender = selectedGenderButton.getText().toString().equalsIgnoreCase("Male") ? "m" : "f";
        }

        String age = ageEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        String height = heightEditText.getText().toString();
        String activityLevel = activityLevelAutoComplete.getText().toString();

        viewModel.saveProfile(gender, age, weight, height, activityLevel);
    }

    /**
     * Observes the ViewModel for a success signal and navigates to the home screen.
     */
    private void setupObservers() {
        viewModel.getProfileSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // The profile has been saved successfully. Navigate to the home screen.
                // We must pop the setup screen off the back stack so the user cannot navigate back to it.
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid();
                NavDirections action =
                        ProfileSetupFragmentDirections.actionProfileSetupFragmentToNavigationHome();
                Navigation.findNavController(requireView()).navigate(action);
            }
        });

        // You could also add another observer here for a viewModel.getErrorMessage() LiveData
        // to show validation errors to the user in a Toast or by setting errors on the TextInputLayouts.
    }
}