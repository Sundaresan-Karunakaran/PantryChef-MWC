package com.example.stepappv3.ui.home;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.Gravity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.database.OnWeekDataFetchedCallback;
import com.example.stepappv3.database.StepRepository;
import com.example.stepappv3.sensors.StepDetector;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HomeFragment extends Fragment {

    // Eski alanlar
    private TextView stepsDisplay;
    private MaterialButton startButton;
    private MaterialButton resetButton;
    private StepDetector stepDetector;
    private HomeViewModel home;
    private CircularProgressIndicator stepsProgress;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // YENİ: Haftalık grafik alanları
    private TextView textViewWeekRange;
    private LinearLayout layoutWeekBars;
    private MaterialButton buttonPrevWeek;
    private MaterialButton buttonNextWeek;
    private MaterialCardView cardWeekChart;

    private StepRepository stepRepository;
    private long currentWeekStartMillis;
    private float swipeStartX;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Eski setup
        stepsDisplay = view.findViewById(R.id.steps);
        startButton = view.findViewById(R.id.start);
        resetButton = view.findViewById(R.id.reset);
        stepDetector = new StepDetector(requireContext());
        stepsProgress = view.findViewById(R.id.progressBar);
        home = new ViewModelProvider(this).get(HomeViewModel.class);

        // YENİ: Repo ve haftalık grafik view'ları
        stepRepository = new StepRepository(requireActivity().getApplication());

        textViewWeekRange = view.findViewById(R.id.textViewWeekRange);
        layoutWeekBars = view.findViewById(R.id.layoutWeekBars);
        buttonPrevWeek = view.findViewById(R.id.buttonPrevWeek);
        buttonNextWeek = view.findViewById(R.id.buttonNextWeek);
        cardWeekChart = view.findViewById(R.id.card_week_chart);

        setupObservers();
        setupClickListeners();
        setupWeekNavigation();
    }

    // ---- İZİNLER ----
    private void setupPermissions() {
        requestPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                home.onStartStopClicked();
                            } else {
                                Toast.makeText(
                                        getContext(),
                                        "Permission denied. Step counting cannot start.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    // ---- VIEWMODEL OBSERVER’LARI ----
    private void setupObservers() {
        home.isCounting.observe(getViewLifecycleOwner(), counting -> {
            if (counting) {
                startButton.setText("Stop");
                startButton.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.button_stop_red));
                stepDetector.start();
            } else {
                startButton.setText("Start");
                startButton.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.button_start_green));
                stepDetector.stop();
            }
        });

        home.steps.observe(getViewLifecycleOwner(), stepCount -> {
            stepsDisplay.setText(String.valueOf(stepCount));
        });

        home.progressPercentage.observe(getViewLifecycleOwner(), progress -> {
            stepsProgress.setProgress(progress, true);
        });

        stepDetector.setOnStepDetectedListener(() -> {
            home.onCountClicked();
        });
    }

    // ---- BUTON CLICK’LERİ ----
    private void setupClickListeners() {
        resetButton.setOnClickListener(v -> showResetConfirmationDialog());

        startButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {
                    home.onStartStopClicked();
                } else {
                    requestPermissionLauncher.launch(
                            android.Manifest.permission.ACTIVITY_RECOGNITION);
                }

            } else {
                home.onStartStopClicked();
            }
        });
    }

    private void showResetConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Counter?")
                .setMessage("This will permanently delete all step history. Are you sure?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    home.onResetClicked();
                    // Reset sonrası haftalık grafiği de sıfırla
                    int[] empty = new int[7];
                    drawWeekBars(empty);
                })
                .show();
    }

    // ---- YENİ: HAFTALIK GRAFİK KISMI ----
    private void setupWeekNavigation() {
        // Şu anki haftanın pazartesi 00:00’ı
        currentWeekStartMillis = getStartOfCurrentWeek();
        loadWeek(currentWeekStartMillis);

        // Önceki hafta
        buttonPrevWeek.setOnClickListener(v -> {
            long oneWeek = 7L * 24L * 60L * 60L * 1000L;
            currentWeekStartMillis -= oneWeek;
            loadWeek(currentWeekStartMillis);
        });

        // Sonraki hafta
        buttonNextWeek.setOnClickListener(v -> {
            long oneWeek = 7L * 24L * 60L * 60L * 1000L;
            currentWeekStartMillis += oneWeek;
            loadWeek(currentWeekStartMillis);
        });

        // Kart üzerinde sağ/sol swipe
        cardWeekChart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                swipeStartX = event.getX();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                float deltaX = event.getX() - swipeStartX;
                float threshold = 100f;
                long oneWeek = 7L * 24L * 60L * 60L * 1000L;

                if (deltaX > threshold) {
                    // Sağa doğru -> önceki hafta
                    currentWeekStartMillis -= oneWeek;
                    loadWeek(currentWeekStartMillis);
                } else if (deltaX < -threshold) {
                    // Sola doğru -> sonraki hafta
                    currentWeekStartMillis += oneWeek;
                    loadWeek(currentWeekStartMillis);
                }
                return true;
            }
            return false;
        });
    }

    // Bu haftanın pazartesi 00:00 zaman damgası
    private long getStartOfCurrentWeek() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);

        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int diff = (dayOfWeek + 6) % 7; // Pazartesi = 0
        cal.add(java.util.Calendar.DAY_OF_MONTH, -diff);

        return cal.getTimeInMillis();
    }

    // Belirli haftayı DB’den çek
    private void loadWeek(long weekStartMillis) {
        long oneDay = 24L * 60L * 60L * 1000L;

        java.text.SimpleDateFormat fmt =
                new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());
        String rangeText =
                fmt.format(new java.util.Date(weekStartMillis)) +
                        " – " +
                        fmt.format(new java.util.Date(weekStartMillis + 6L * oneDay));

        textViewWeekRange.setText(rangeText);

        stepRepository.getWeekStepsAsync(weekStartMillis, new OnWeekDataFetchedCallback() {
            @Override
            public void onWeekDataFetched(int[] dailySteps) {
                drawWeekBars(dailySteps);
            }
        });
    }

    // Bar grafiği çiz
    private void drawWeekBars(int[] dailySteps) {
        layoutWeekBars.removeAllViews();

        if (dailySteps == null || dailySteps.length == 0) {
            return;
        }

        int max = 0;
        int i;
        for (i = 0; i < dailySteps.length; i++) {
            if (dailySteps[i] > max) {
                max = dailySteps[i];
            }
        }
        if (max == 0) {
            max = 1;
        }

        String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (i = 0; i < 7; i++) {

            LinearLayout column = new LinearLayout(getContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            int barWidth =
                    (int) (16 * getResources().getDisplayMetrics().density);
            int maxBarHeight =
                    (int) (120 * getResources().getDisplayMetrics().density);

            float ratio = dailySteps[i] / (float) max;
            int barHeight = (int) (maxBarHeight * ratio);

            View bar = new View(getContext());
            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(barWidth, barHeight);
            barParams.setMargins(8, 0, 8, 4);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(0xFF8D6E63); // basit kahverengi ton

            TextView label = new TextView(getContext());
            label.setText(labels[i]);
            label.setTextSize(10);
            label.setGravity(Gravity.CENTER);

            column.addView(bar);
            column.addView(label);

            layoutWeekBars.addView(column);
        }
    }
}
