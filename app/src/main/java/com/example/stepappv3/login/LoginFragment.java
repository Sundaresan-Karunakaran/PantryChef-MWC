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

    private LoginViewModel loginViewModel;
    private SignInButton googleSignInButton;
    private ProgressBar loginProgressBar;

    private ActivityResultLauncher<android.content.Intent> signInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        signInLauncher = registerForActivityResult(
                new com.firebase.ui.auth.FirebaseAuthUIActivityResultContract(),
                (result) -> { /* This is now intentionally empty */ }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        googleSignInButton = view.findViewById(R.id.google_sign_in_button);
        loginProgressBar = view.findViewById(R.id.login_progress_bar);
        googleSignInButton.setOnClickListener(v -> {

            loginProgressBar.setVisibility(View.VISIBLE);
            java.util.List<com.firebase.ui.auth.AuthUI.IdpConfig> providers = java.util.Collections.singletonList(
                    new com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder().build());

            android.content.Intent signInIntent = com.firebase.ui.auth.AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();

            signInLauncher.launch(signInIntent);
        });

        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), state -> {
            if (state == AuthenticationState.AUTHENTICATED) {
                loginProgressBar.setVisibility(View.VISIBLE);
                googleSignInButton.setEnabled(false);
            } else {

                loginProgressBar.setVisibility(View.GONE);
                googleSignInButton.setEnabled(true);
            }
        });

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