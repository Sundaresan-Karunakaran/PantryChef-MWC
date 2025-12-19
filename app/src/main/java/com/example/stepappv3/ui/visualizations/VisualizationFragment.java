package com.example.stepappv3.ui.visualization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.stepappv3.R;
import com.example.stepappv3.database.steps.DailyStep;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VisualizationFragment extends Fragment {

    private VisualizationViewModel viewModel;
    private LineChart stepsChart;
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.pantry_category_chart);
        viewModel = new ViewModelProvider(this).get(VisualizationViewModel.class);
        stepsChart = view.findViewById(R.id.daily_steps_chart);

        setupChart();
        setupLineChart();
        observeViewModel();
    }
    private void setupLineChart() {
        stepsChart.getDescription().setEnabled(false);
        stepsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        stepsChart.getXAxis().setGranularity(1f);
        stepsChart.getXAxis().setValueFormatter(new DateAxisValueFormatter()); // Use our custom formatter
        stepsChart.getAxisRight().setEnabled(false);
        stepsChart.animateX(1000);
    }

    private void updateLineChartData(List<DailyStep> dailySteps) {
        ArrayList<Entry> values = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (DailyStep step : dailySteps) {
            values.add(new Entry(step.day, step.steps));
        }

        LineDataSet set1 = new LineDataSet(values, "Daily Steps");
        set1.setDrawIcons(false);
        set1.setColor(getResources().getColor(R.color.md_theme_primary, null));
        set1.setCircleColor(getResources().getColor(R.color.md_theme_primary, null));
        set1.setLineWidth(2f);
        set1.setCircleRadius(4f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);
        set1.setFillFormatter((dataSet, dataProvider) -> stepsChart.getAxisLeft().getAxisMinimum());
        set1.setFillColor(getResources().getColor(R.color.md_theme_primaryContainer, null));

        LineData data = new LineData(set1);
        stepsChart.setData(data);
        stepsChart.invalidate(); // Refresh chart
    }


    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);    barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1500);
    }

    private void observeViewModel() {
        viewModel.getPantryCategoryCounts().observe(getViewLifecycleOwner(), categoryCounts -> {
            if (categoryCounts != null && !categoryCounts.isEmpty()) {
                updateChartData(categoryCounts);
            }
        });
        viewModel.getDailySteps().observe(getViewLifecycleOwner(), dailySteps -> {
            // This block will now execute whenever the step data changes in the database.

            // Add a check to ensure the list is not null or empty.
            if (dailySteps != null && !dailySteps.isEmpty()) {
                // Call your existing method to update the line chart.
                updateLineChartData(dailySteps);
            }
        });
    }
    private void updateChartData(Map<String, Integer> categoryCounts) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>(categoryCounts.keySet());

        for (int i = 0; i < labels.size(); i++) {
            String category = labels.get(i);
            Integer count = categoryCounts.get(category);
            if (count != null) {
                entries.add(new BarEntry(i, count));
            }
        }

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        BarDataSet dataSet = new BarDataSet(entries, "Pantry Categories");

        int[] chartColors = getResources().getIntArray(R.array.pantry_chart_colors);
        dataSet.setColors(chartColors);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.invalidate();
    }

    public class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat mFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            // value is the timestamp in milliseconds
            return mFormat.format(new Date((long) value));
        }
    }


}
    