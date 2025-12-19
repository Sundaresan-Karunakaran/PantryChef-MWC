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
    private androidx.navigation.ui.AppBarConfiguration appBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);
        appBarConfiguration = new androidx.navigation.ui.AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_pantry, R.id.navigation_recommendations)
                .build();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {

                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.mobile_navigation, true) // Pops everything off the stack
                        .build();

                navController.navigate(R.id.loginFragment, null, navOptions);
            }

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
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return androidx.navigation.ui.NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
