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
        // --- KRİTİK EKLEME: Başlangıç temasını (Turuncu) kapatıp normal temaya (Beyaz) geçiyoruz ---
        setTheme(R.style.AppTheme);
        // ------------------------------------------------------------------------------------------

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Kullanıcı oturum durumu dinleyicisi
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // Kullanıcı çıkış yapmışsa Login ekranına at ve geri tuşu geçmişini temizle
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.mobile_navigation, true)
                        .build();

                navController.navigate(R.id.loginFragment, null, navOptions);
            }
        };

        // Hangi ekranda olduğumuzu dinleyip alt menüyü göster/gizle yapıyoruz
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Sadece Login ekranındaysak menüyü gizle
            if (destination.getId() == R.id.loginFragment) {
                navView.setVisibility(View.GONE);
            } else {
                // Diğer her yerde göster
                navView.setVisibility(View.VISIBLE);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        android.view.Menu menu = navView.getMenu();
        android.view.MenuItem pantryItem = menu.findItem(R.id.pantryFragment);

        android.text.SpannableString spanString = new android.text.SpannableString(pantryItem.getTitle());
        spanString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, spanString.length(), 0);
        pantryItem.setTitle(spanString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}