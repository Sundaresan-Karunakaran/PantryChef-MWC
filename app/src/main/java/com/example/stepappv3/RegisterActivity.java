package com.example.stepappv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepappv3.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegEmail;
    private EditText etRegPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateAccountClicked();
            }
        });
    }

    private void onCreateAccountClicked() {
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_PASSWORD, password);

        // Yeni kullanıcı kayıt oldu
        editor.putBoolean(Constants.KEY_IS_PROFILE_DONE, false);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);

        editor.apply();

        // Profile setup ekranına git
        Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }
}
