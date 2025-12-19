package com.example.stepappv3.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stepappv3.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.stepappv3.database.OnDataFetchedCallback;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends androidx.fragment.app.Fragment {

    MaterialButton logoutButton;
    private ProfileViewModel profileViewModel;
    private TextView profileNameTextView;
    private TextView profileEmailTextView;
    private TextView profileDetailsTextView;
    private TextView profileGoalsTextView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileNameTextView = view.findViewById(R.id.profile_name_textview);
        profileEmailTextView = view.findViewById(R.id.profile_email_textview);
        profileDetailsTextView = view.findViewById(R.id.profile_details_textview);
        profileGoalsTextView = view.findViewById(R.id.profile_goals_textview);

        logoutButton = view.findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            if (profileViewModel != null) {
                profileViewModel.logout();
            }
        });
        setupObservers();
        setupClickListeners();

    }

    private void setupObservers() {
        // This is the core of the reactive UI.
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                // We have data, so populate the UI.
                profileNameTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()); // Assuming you have 'name' in your UserProfile
                profileEmailTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                String biometricDetails = "Gender: " + userProfile.gender + "\n"
                        + "Age: " + userProfile.age + "\n"
                        + "Weight: " + userProfile.weight + " kg\n"
                        + "Height: " + userProfile.height + " cm";
                profileDetailsTextView.setText(biometricDetails);

                String goalDetails = "Calories: " + String.format("%.0f", userProfile.requiredCalories) + " kcal\n"
                        + "Fat: " + String.format("%.0f", userProfile.requiredFat) + " g\n"
                        + "Sugar: " + String.format("%.0f", userProfile.requiredSugar) + " g";
                profileGoalsTextView.setText(goalDetails);

            } else {
                // The user has no profile in the database.
                profileNameTextView.setText("Welcome!");
                profileDetailsTextView.setText("Please complete your profile to see your details here.");
                profileGoalsTextView.setText("Your daily goals will be calculated after setup.");
            }
        });
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            // Use the FirebaseUI sign-out method
            AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener(task -> {
                        // After signing out, navigate the user back to the login screen.
                        // You'll need to define this navigation action in your nav_graph.
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_global_to_loginFragment);
                    });
        });
    }




}
