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
import androidx.navigation.NavController; // EKLENDİ
import androidx.navigation.Navigation;    // EKLENDİ

import com.example.stepappv3.R;
import com.example.stepappv3.database.StepRepository;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser; // EKLENDİ

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
                (result) -> { /* Boş kalabilir, ViewModel durumu zaten dinliyor */ }
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

        // --- GÜNCELLENEN KISIM ---
        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case AUTHENTICATED:
                    FirebaseUser currentUser = loginViewModel.user.getValue();
                    if (currentUser != null) {
                        // DÜZELTME BURADA: Direkt gitmek yerine kontrol metodunu çağırıyoruz
                        checkUserProfileAndNavigate(currentUser);
                    }
                    break;
                case IN_PROGRESS:
                    loginProgressBar.setVisibility(View.VISIBLE);
                    googleSignInButton.setEnabled(false);
                    break;
                case UNAUTHENTICATED:
                    loginProgressBar.setVisibility(View.GONE);
                    googleSignInButton.setEnabled(true);
                    break;
            }
        });
    }

    // Profil kontrolü ve yönlendirme mantığı
    private void checkUserProfileAndNavigate(FirebaseUser user) {
        if (user == null) return;

        StepRepository repo = new StepRepository(requireActivity().getApplication());

        // Veritabanını kontrol et: Profil var mı?
        repo.getUserProfile(user.getUid()).observe(getViewLifecycleOwner(), profile -> {
            // Gözlemciyi kaldır ki döngüye girmesin (önemli!)
            repo.getUserProfile(user.getUid()).removeObservers(getViewLifecycleOwner());

            NavController navController = Navigation.findNavController(requireView());

            if (profile == null) {
                // Profil yok -> İlk kez giriyor -> ONBOARDING'e git
                navController.navigate(R.id.action_loginFragment_to_onboardingInfoFragment);
            } else {
                // Profil var -> Normal akış -> HOME'a git
                // HomeFragment userId beklediği için Bundle ile gönderiyoruz
                Bundle args = new Bundle();
                args.putString("userId", user.getUid());
                navController.navigate(R.id.action_loginFragment_to_homeFragment, args);
            }
        });
    }
}