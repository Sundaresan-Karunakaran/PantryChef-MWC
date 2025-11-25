package com.example.stepappv3.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stepappv3.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.database.OnDataFetchedCallback;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends androidx.fragment.app.Fragment {
    private TextView totalSteps;
    private MaterialButton stepsTaken;

    private TextView stepsTodayView;
    private TextView stepsHourView;
    private TextView stepsMinuteView;

    // We get the buttons but won't use them yet, as LiveData handles updates.
    private MaterialButton getStepsDayButton;
    private MaterialButton getStepsHourButton;
    private MaterialButton getStepsMinuteButton;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        totalSteps = view.findViewById(R.id.totalStepsView);
        stepsTaken = view.findViewById(R.id.totalStepsButton);
        stepsTodayView = view.findViewById(R.id.steps_day_textview);
        stepsHourView = view.findViewById(R.id.steps_hour_textview);
        stepsMinuteView = view.findViewById(R.id.steps_minute_textview);
        getStepsDayButton = view.findViewById(R.id.get_steps_day_button);
        getStepsHourButton = view.findViewById(R.id.get_steps_hour_button);
        getStepsMinuteButton = view.findViewById(R.id.get_steps_minute_button);

        ProfileViewModel profile = new ViewModelProvider(this).get(ProfileViewModel.class);


        totalSteps.setText("");
        stepsTaken.setOnClickListener(v -> {
            profile.getTotalSteps(new OnDataFetchedCallback(){
                @Override
                public void onDataFetched(int total){
                    totalSteps.setText(String.valueOf(total));
                }
            });
        });

        stepsTodayView.setText("");
        stepsHourView.setText("");
        stepsMinuteView.setText("");

        // 3. Set up click listener for the "Day" button
        getStepsDayButton.setOnClickListener(v -> {
            profile.fetchStepsToday(new OnDataFetchedCallback() {
                @Override
                public void onDataFetched(int total) {
                    stepsTodayView.setText(String.valueOf(total));
                }
            });
        });

        // 4. Set up click listener for the "Hour" button
        getStepsHourButton.setOnClickListener(v -> {
            profile.fetchStepsThisHour(new OnDataFetchedCallback() {
                @Override
                public void onDataFetched(int total) {
                    stepsHourView.setText(String.valueOf(total));
                }
            });
        });

        // 5. Set up click listener for the "Minute" button
        getStepsMinuteButton.setOnClickListener(v -> {
            profile.fetchStepsThisMinute(new OnDataFetchedCallback() {
                @Override
                public void onDataFetched(int total) {
                    stepsMinuteView.setText(String.valueOf(total));
                }
            });
        });

    }


}
