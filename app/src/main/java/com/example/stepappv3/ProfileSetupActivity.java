package com.example.stepappv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepappv3.utils.Constants;

import java.util.HashSet;
import java.util.Set;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etAge;
    private EditText etWeight;
    private RadioGroup rgGoal;

    private CheckBox cbNuts;
    private CheckBox cbDairy;
    private CheckBox cbGluten;
    private CheckBox cbEggs;
    private CheckBox cbSeafood;
    private CheckBox cbSoy;

    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        rgGoal = findViewById(R.id.rgGoal);

        cbNuts = findViewById(R.id.cbNuts);
        cbDairy = findViewById(R.id.cbDairy);
        cbGluten = findViewById(R.id.cbGluten);
        cbEggs = findViewById(R.id.cbEggs);
        cbSeafood = findViewById(R.id.cbSeafood);
        cbSoy = findViewById(R.id.cbSoy);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveProfileClicked();
            }
        });
    }

    private void onSaveProfileClicked() {
        String ageText = etAge.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (ageText.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "Please enter age and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGoalId = rgGoal.getCheckedRadioButtonId();
        if (selectedGoalId == -1) {
            Toast.makeText(this, "Please select your goal", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGoalButton = findViewById(selectedGoalId);
        String goalValue = selectedGoalButton.getTag() != null
                ? selectedGoalButton.getTag().toString()
                : selectedGoalButton.getText().toString();

        int age;
        float weight;

        try {
            age = Integer.parseInt(ageText);
            weight = Float.parseFloat(weightText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Allergies (şimdilik sadece prefs'e boolean olarak kaydediyoruz)
        boolean nuts = cbNuts.isChecked();
        boolean dairy = cbDairy.isChecked();
        boolean gluten = cbGluten.isChecked();
        boolean eggs = cbEggs.isChecked();
        boolean seafood = cbSeafood.isChecked();
        boolean soy = cbSoy.isChecked();

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(Constants.KEY_AGE, age);
        editor.putFloat(Constants.KEY_WEIGHT, weight);
        editor.putString(Constants.KEY_GOAL, goalValue);

        editor.putBoolean(Constants.KEY_ALLERGY_NUTS, nuts);
        editor.putBoolean(Constants.KEY_ALLERGY_DAIRY, dairy);
        editor.putBoolean(Constants.KEY_ALLERGY_GLUTEN, gluten);
        editor.putBoolean(Constants.KEY_ALLERGY_EGGS, eggs);
        editor.putBoolean(Constants.KEY_ALLERGY_SEAFOOD, seafood);
        editor.putBoolean(Constants.KEY_ALLERGY_SOY, soy);

        editor.putBoolean(Constants.KEY_IS_PROFILE_DONE, true);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);

        editor.apply();

        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
