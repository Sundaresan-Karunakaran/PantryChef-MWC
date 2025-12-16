package com.example.stepappv3.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.util.TypedValue; // DİNAMİK RENK İÇİN EKLENDİ

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private BarChart barChartSteps;
    private PieChart pieChartPantry;
    private MaterialButtonToggleGroup toggleGroupTime;

    // YENİ METOT: Temaya göre dinamik rengi çözer (colorOnSurface, colorOutline vb.)
    private int getThemeColor(int attrId) {
        TypedValue typedValue = new TypedValue();
        // com.google.android.material.R.attr.colorOnSurface gibi Materyal Theme Attribute'lerini çözer
        requireContext().getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        barChartSteps = view.findViewById(R.id.barChartSteps);
        pieChartPantry = view.findViewById(R.id.pieChartPantry);
        toggleGroupTime = view.findViewById(R.id.toggleGroupTime);

        setupCharts();
        setupToggle();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeViewModel != null) {
            homeViewModel.refresh();
        }
    }

    private void setupCharts() {
        // Tema bağımlı renkleri al
        int primaryTextColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface);
        int gridColor = getThemeColor(com.google.android.material.R.attr.colorOutline); // Ekseni daha yumuşak bir renkle çizmek için

        // --- 1. BAR CHART (ADIMLAR) AYARLARI ---
        barChartSteps.getDescription().setEnabled(false);
        barChartSteps.setDrawGridBackground(false);
        barChartSteps.setDrawBarShadow(false);
        barChartSteps.animateY(1500);

        XAxis xAxis = barChartSteps.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(primaryTextColor); // DİNAMİK RENK KULLANILDI
        xAxis.setTextSize(10f);
        xAxis.setGridColor(gridColor); // DİNAMİK RENK KULLANILDI

        YAxis leftAxis = barChartSteps.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(primaryTextColor); // DİNAMİK RENK KULLANILDI
        leftAxis.setTextSize(10f);
        leftAxis.setGridColor(gridColor); // DİNAMİK RENK KULLANILDI

        barChartSteps.getAxisRight().setEnabled(false);
        barChartSteps.getLegend().setEnabled(false);

        // --- 2. PIE CHART (DONUT) AYARLARI ---
        pieChartPantry.setUsePercentValues(true);
        pieChartPantry.getDescription().setEnabled(false);

        pieChartPantry.setCenterText("Pantry\nItems");
        pieChartPantry.setCenterTextSize(20f);

        pieChartPantry.setHoleRadius(40f);
        pieChartPantry.setTransparentCircleRadius(45f);
        pieChartPantry.getLegend().setEnabled(false);

        pieChartPantry.setEntryLabelColor(Color.WHITE);
        pieChartPantry.setEntryLabelTextSize(14f);
    }

    private void setupToggle() {
        toggleGroupTime.check(R.id.btnDay);
        toggleGroupTime.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnDay) {
                    homeViewModel.setFilter(HomeViewModel.FilterType.DAILY);
                } else if (checkedId == R.id.btnWeek) {
                    homeViewModel.setFilter(HomeViewModel.FilterType.WEEKLY);
                } else if (checkedId == R.id.btnMonth) {
                    homeViewModel.setFilter(HomeViewModel.FilterType.MONTHLY);
                }
            }
        });
    }

    private void setupObservers() {
        homeViewModel.stepsGraphData.observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                updateBarChart(entries, homeViewModel.currentFilter.getValue());
            }
        });

        homeViewModel.pantryPieData.observe(getViewLifecycleOwner(), categoryCounts -> {
            updatePieChart(categoryCounts);
        });
    }

    private void updateBarChart(List<BarEntry> entries, HomeViewModel.FilterType filterType) {
        int primaryTextColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface);

        BarDataSet set;
        if (barChartSteps.getData() != null && barChartSteps.getData().getDataSetCount() > 0) {
            set = (BarDataSet) barChartSteps.getData().getDataSetByIndex(0);
            set.setValues(entries);
            set.setColor(Color.parseColor("#FF4D00"));
            set.setValueTextColor(primaryTextColor); // DİNAMİK RENK KULLANILDI
            set.setValueTextSize(10f);
            barChartSteps.getData().setBarWidth(0.2f);
            barChartSteps.getData().notifyDataChanged();
            barChartSteps.notifyDataSetChanged();
        } else {
            set = new BarDataSet(entries, "Steps");
            set.setColor(Color.parseColor("#FF4D00"));
            set.setValueTextColor(primaryTextColor); // DİNAMİK RENK KULLANILDI
            set.setValueTextSize(10f);

            BarData data = new BarData(set);
            data.setBarWidth(0.2f);
            barChartSteps.setData(data);
        }

        XAxis xAxis = barChartSteps.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int val = (int) value;
                if (filterType == HomeViewModel.FilterType.DAILY) {
                    return String.format(Locale.getDefault(), "%02d:00", val);
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_YEAR, val);
                    return dayFormat.format(cal.getTime());
                }
            }
        });

        barChartSteps.invalidate();
        barChartSteps.animateY(1000);
    }

    private void updatePieChart(Map<String, Integer> categoryCounts) {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > 0) {
                pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (pieEntries.isEmpty()) {
            pieChartPantry.setCenterText("No Data");
            pieChartPantry.setData(null);
            pieChartPantry.invalidate();
            return;
        }

        pieChartPantry.setCenterText("Pantry\nItems");
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartPantry));
        data.setValueTextSize(16f);
        data.setValueTextColor(Color.WHITE);

        pieChartPantry.setData(data);
        pieChartPantry.animateY(1000);
        pieChartPantry.invalidate();
    }
}