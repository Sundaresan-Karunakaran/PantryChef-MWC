package com.example.stepappv3.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.stepappv3.LoginActivity;
import com.example.stepappv3.R;
import com.example.stepappv3.utils.Constants;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    // Profile info views
    private TextView tvAge;
    private TextView tvWeight;
    private TextView tvGoal;
    private TextView tvAllergies;

    // Steps UI (Week / Month / Total)
    private TextView stepsDayTextView;     // şimdilik kullanmayabiliriz
    private TextView stepsWeekTextView;    // steps_hour_textview -> this week
    private TextView stepsMonthTextView;   // steps_minute_textview -> this month

    private Button getStepsDayButton;
    private Button getStepsWeekButton;
    private Button getStepsMonthButton;

    private TextView totalStepsView;
    private Button totalStepsButton;

    // Logout button
    private Button buttonLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Profile info
        tvAge = root.findViewById(R.id.tvProfileAge);
        tvWeight = root.findViewById(R.id.tvProfileWeight);
        tvGoal = root.findViewById(R.id.tvProfileGoal);
        tvAllergies = root.findViewById(R.id.tvProfileAllergies);

        // Steps UI
        stepsDayTextView = root.findViewById(R.id.steps_day_textview);
        stepsWeekTextView = root.findViewById(R.id.steps_hour_textview);
        stepsMonthTextView = root.findViewById(R.id.steps_minute_textview);

        getStepsDayButton = root.findViewById(R.id.get_steps_day_button);
        getStepsWeekButton = root.findViewById(R.id.get_steps_hour_button);
        getStepsMonthButton = root.findViewById(R.id.get_steps_minute_button);

        totalStepsView = root.findViewById(R.id.totalStepsView);
        totalStepsButton = root.findViewById(R.id.totalStepsButton);

        // Logout button
        buttonLogout = root.findViewById(R.id.buttonLogout);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProfileInfo();

        // Set click listeners without lambdas or anonymous classes
        getStepsDayButton.setOnClickListener(this);
        getStepsWeekButton.setOnClickListener(this);
        getStepsMonthButton.setOnClickListener(this);
        totalStepsButton.setOnClickListener(this);
        buttonLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.get_steps_day_button) {
            // TODO: Steps Today will be implemented here
        } else if (id == R.id.get_steps_hour_button) {
            // TODO: Steps This Week
        } else if (id == R.id.get_steps_minute_button) {
            // TODO: Steps This Month
        } else if (id == R.id.totalStepsButton) {
            // TODO: Total steps
        } else if (id == R.id.buttonLogout) {
            logoutUser();
        }
    }

    private void logoutUser() {
        // Clear user-related preferences and mark as logged out
        Context context = requireActivity().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Clear all stored profile/login data
        editor.clear();

        // Make sure login flags are false
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false);
        editor.putBoolean(Constants.KEY_IS_PROFILE_DONE, false);

        editor.apply();

        // Go back to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Finish current activity to prevent going back to profile with back button
        requireActivity().finish();
    }

    private void loadProfileInfo() {
        Context context = requireContext();
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);

        int age = prefs.getInt(Constants.KEY_AGE, -1);
        float weight = prefs.getFloat(Constants.KEY_WEIGHT, -1f);
        String goal = prefs.getString(Constants.KEY_GOAL, "-");

        boolean nuts = prefs.getBoolean(Constants.KEY_ALLERGY_NUTS, false);
        boolean dairy = prefs.getBoolean(Constants.KEY_ALLERGY_DAIRY, false);
        boolean gluten = prefs.getBoolean(Constants.KEY_ALLERGY_GLUTEN, false);
        boolean eggs = prefs.getBoolean(Constants.KEY_ALLERGY_EGGS, false);
        boolean seafood = prefs.getBoolean(Constants.KEY_ALLERGY_SEAFOOD, false);
        boolean soy = prefs.getBoolean(Constants.KEY_ALLERGY_SOY, false);

        tvAge.setText("Age: " + (age == -1 ? "-" : age));
        tvWeight.setText("Weight: " + (weight <= 0 ? "-" : weight));
        tvGoal.setText("Goal: " + goal);

        StringBuilder sb = new StringBuilder();
        if (nuts) sb.append("Nuts, ");
        if (dairy) sb.append("Dairy, ");
        if (gluten) sb.append("Gluten, ");
        if (eggs) sb.append("Eggs, ");
        if (seafood) sb.append("Seafood, ");
        if (soy) sb.append("Soy, ");

        String allergiesText;
        if (sb.length() == 0) {
            allergiesText = "None";
        } else {
            allergiesText = sb.substring(0, sb.length() - 2); // remove last ", "
        }

        tvAllergies.setText("Allergies: " + allergiesText);
    }
}
