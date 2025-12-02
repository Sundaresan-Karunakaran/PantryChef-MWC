package com.example.stepappv3.ui.profile;

import android.content.Context;
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

import com.example.stepappv3.R;
import com.example.stepappv3.utils.Constants;

public class ProfileFragment extends Fragment {

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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProfileInfo();

        // Şimdilik butonlar sadece placeholder olsun.
        // Room bağlayınca buraya gerçek çağrıları ekleyeceğiz.
        getStepsDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Steps Today buraya bağlanacak (istersen şimdilik boş bırak)
            }
        });

        getStepsWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Steps This Week
            }
        });

        getStepsMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Steps This Month
            }
        });

        totalStepsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Total steps
            }
        });
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
            allergiesText = sb.substring(0, sb.length() - 2); // sondaki ", " sil
        }

        tvAllergies.setText("Allergies: " + allergiesText);
    }
}
