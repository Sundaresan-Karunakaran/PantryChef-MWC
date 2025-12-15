package com.example.stepappv3;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // USER IS LOGGED OUT!
                // Immediately navigate back to the login screen and
                // clear all other screens from the back stack.

                // Create navigation options to clear the entire app state.
                // This prevents the user from pressing "back" to get into the app.
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.mobile_navigation, true) // Pops everything off the stack
                        .build();

                // Perform the navigation to the LoginFragment
                navController.navigate(R.id.loginFragment, null, navOptions);
            }
            // If user is not null, we don't need to do anything.
            // The app is in its normal, logged-in state.
        };

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Check the ID of the new destination.
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.profileSetupFragment) {
                // If we are on the login screen, HIDE the bottom navigation bar.
                navView.setVisibility(View.GONE);
            } else {
                // For all other screens, SHOW the bottom navigation bar.
                navView.setVisibility(View.VISIBLE);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    protected void onResume() {
        super.onResume();
        // Tell the guard to start watching when the activity is active and in the foreground.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tell the guard to stop watching when the activity is paused to save resources.
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
