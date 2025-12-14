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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private BarChart barChartSteps;
    private PieChart pieChartPantry;
    private MaterialButtonToggleGroup toggleGroupTime;

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

    private void setupCharts() {
        // Bar Chart Ayarları
        barChartSteps.getDescription().setEnabled(false);
        barChartSteps.setDrawGridBackground(false);
        barChartSteps.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartSteps.getXAxis().setGranularity(1f);
        barChartSteps.getAxisRight().setEnabled(false); // Sağ ekseni kapat

        // Pie Chart Ayarları
        pieChartPantry.getDescription().setEnabled(false);
        pieChartPantry.setCenterText("Pantry\nItems");
        pieChartPantry.setCenterTextSize(14f);
        pieChartPantry.setHoleRadius(40f);
        pieChartPantry.setTransparentCircleRadius(45f);
        pieChartPantry.getLegend().setEnabled(false); // Efsaneyi kapat, dilimlerin üzerine yazacağız
    }

    private void setupToggle() {
        // Varsayılan seçim
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
        // Adım Grafiği Güncelleme
        homeViewModel.stepsGraphData.observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                BarDataSet set = new BarDataSet(entries, "Steps");
                set.setColors(ColorTemplate.MATERIAL_COLORS);
                set.setValueTextSize(10f);

                BarData data = new BarData(set);
                data.setBarWidth(0.9f); // Çubuk genişliği

                barChartSteps.setData(data);
                barChartSteps.animateY(1000);
                barChartSteps.invalidate(); // Grafiği yenile
            }
        });

        // Kiler Pasta Grafiği Güncelleme
        homeViewModel.pantryPieData.observe(getViewLifecycleOwner(), categoryCounts -> {
            List<PieEntry> pieEntries = new ArrayList<>();

            // Map'ten veriyi PieEntry'e çevir
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                // Sadece 0'dan büyük olanları ekle
                if (entry.getValue() > 0) {
                    pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }

            PieDataSet dataSet = new PieDataSet(pieEntries, "Categories");
            dataSet.setColors(ColorTemplate.JOYFUL_COLORS); // Renkli bir palet
            dataSet.setSliceSpace(2f);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.WHITE);

            PieData data = new PieData(dataSet);
            pieChartPantry.setData(data);
            pieChartPantry.animateXY(1000, 1000);
            pieChartPantry.invalidate();
        });
    }
}