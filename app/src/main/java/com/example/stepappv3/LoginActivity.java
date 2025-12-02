package com.example.stepappv3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.stepappv3.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private Button registerBtn;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // Eğer kullanıcı daha önce giriş yaptıysa
        boolean isLoggedIn = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            boolean isProfileDone = prefs.getBoolean(Constants.KEY_IS_PROFILE_DONE, false);

            if (isProfileDone) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, ProfileSetupActivity.class));
            }
            finish();
            return;
        }

        emailEdit = findViewById(R.id.editTextEmail);
        passwordEdit = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        registerBtn = findViewById(R.id.buttonGoRegister);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void handleLogin() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String savedEmail = prefs.getString(Constants.KEY_EMAIL, "");
        String savedPassword = prefs.getString(Constants.KEY_PASSWORD, "");

        if (email.equals(savedEmail) && password.equals(savedPassword)) {

            prefs.edit().putBoolean(Constants.KEY_IS_LOGGED_IN, true).apply();

            boolean profileDone = prefs.getBoolean(Constants.KEY_IS_PROFILE_DONE, false);

            if (!profileDone) {
                startActivity(new Intent(LoginActivity.this, ProfileSetupActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            finish();

        } else {
            Toast.makeText(this, "Wrong email or password.", Toast.LENGTH_SHORT).show();
        }
    }
}
