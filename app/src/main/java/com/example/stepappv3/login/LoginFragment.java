package com.example.stepappv3.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.example.stepappv3.R;
import com.google.android.gms.common.SignInButton;

public class LoginFragment extends Fragment {

    // Declare member fields for the views and logic components
    private LoginViewModel loginViewModel;
    private SignInButton googleSignInButton;
    private ProgressBar loginProgressBar;

    // This is the modern replacement for onActivityResult, used to handle the sign-in result.
    private ActivityResultLauncher<android.content.Intent> signInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference to the ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // This sets up the contract for what to do when the sign-in flow returns a result.
        signInLauncher = registerForActivityResult(
                new com.firebase.ui.auth.FirebaseAuthUIActivityResultContract(),
                (result) -> { /* This is now intentionally empty */ }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This method's only job is to create the view hierarchy from your XML file.
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the views from the layout
        googleSignInButton = view.findViewById(R.id.google_sign_in_button);
        loginProgressBar = view.findViewById(R.id.login_progress_bar);

        // Set up the listener for the sign-in button
        googleSignInButton.setOnClickListener(v -> {

            loginProgressBar.setVisibility(View.VISIBLE);
            // This is the list of providers you want to support. For now, just Google.
            java.util.List<com.firebase.ui.auth.AuthUI.IdpConfig> providers = java.util.Collections.singletonList(
                    new com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder().build());

            // Create the sign-in intent using the FirebaseUI library.
            android.content.Intent signInIntent = com.firebase.ui.auth.AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();

            // Launch the sign-in flow. The result will be handled by the launcher we defined in onCreate.
            signInLauncher.launch(signInIntent);
        });

        // Set up the observer for the authentication state
        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), state -> {
            if (state == AuthenticationState.AUTHENTICATED) {
                loginProgressBar.setVisibility(View.VISIBLE);
                googleSignInButton.setEnabled(false);
            } else {

                loginProgressBar.setVisibility(View.GONE);
                googleSignInButton.setEnabled(true);
            }
        });

        // OBSERVER 2: This new observer handles the NAVIGATION decision.
        // It will only fire after a user logs in and the database check is complete.
        loginViewModel.userProfile.observe(getViewLifecycleOwner(), profile -> {

            if (loginViewModel.authenticationState.getValue() != AuthenticationState.AUTHENTICATED) {
                return;
            }

            if (profile != null) {

                String userId = profile.userId;
                NavDirections action =
                        LoginFragmentDirections.actionLoginFragmentToNavigationHome();
                androidx.navigation.Navigation.findNavController(view).navigate(action);
            } else {

                NavDirections action =
                        LoginFragmentDirections.actionLoginFragmentToProfileSetupFragment();
                androidx.navigation.Navigation.findNavController(view).navigate(action);
            }
        });
    }
}