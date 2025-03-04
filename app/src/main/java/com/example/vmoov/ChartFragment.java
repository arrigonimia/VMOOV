package com.example.vmoov;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;

public class ChartFragment extends Fragment {

    private int chartIndex;

    public static ChartFragment newInstance(int index) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chartIndex = getArguments().getInt("index");
        }
    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        PieChart pieChart = view.findViewById(R.id.pieChart);

        switch (chartIndex) {
            case 0:
                setupChart4(pieChart); // 🔹 Puntaje General
                break;
            case 1:
                setupChart3(pieChart); // 🔹 Tiempo Promedio
                break;
            case 2:
                setupChart2(pieChart); // 🔹 Movimientos Exitosos
                break;
            case 3:
                setupChart1(pieChart); // 🔹 Sesiones Completadas
                break;
        }
        return view;
    }



    private void setupChart1(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Realizadas"));
        entries.add(new PieEntry(30f, "Totales"));

        PieDataSet dataSet = new PieDataSet(entries, "Datos");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.violet),
                ContextCompat.getColor(requireContext(), R.color.lightgrey)
        );

        // 🔹 Aumentar el tamaño de los valores dentro del gráfico
        dataSet.setValueTextSize(22f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        // 🔹 Aplicar estilo cuerpobold
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.verdanabold);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTypeface(typeface);

        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f); // 🔹 Reducir el radio del hueco central
        pieChart.setTransparentCircleRadius(35f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(typeface);
        pieChart.animateY(1500);
        pieChart.invalidate();
    }

    private void setupChart2(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(50f, "Exitosos"));
        entries.add(new PieEntry(50f, "Recetados"));

        PieDataSet dataSet = new PieDataSet(entries, "Movimientos");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.violet),
                ContextCompat.getColor(requireContext(), R.color.blue)
        );

        dataSet.setValueTextSize(22f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.verdanabold);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTypeface(typeface);

        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(35f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(typeface);
        pieChart.animateY(1500);
        pieChart.invalidate();
    }

    private void setupChart3(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(60f, "Completado"));
        entries.add(new PieEntry(40f, "Pendiente"));

        PieDataSet dataSet = new PieDataSet(entries, "Sesiones");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.violet),
                ContextCompat.getColor(requireContext(), R.color.blue)
        );

        dataSet.setValueTextSize(22f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.verdanabold);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTypeface(typeface);

        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(35f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(typeface);
        pieChart.animateY(1500);
        pieChart.invalidate();
    }

    private void setupChart4(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(80f, "Puntaje"));
        entries.add(new PieEntry(20f, "Restante"));

        PieDataSet dataSet = new PieDataSet(entries, "Puntaje General");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.violet),
                ContextCompat.getColor(requireContext(), R.color.blue)
        );

        dataSet.setValueTextSize(22f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.verdanabold);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTypeface(typeface);

        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(35f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(typeface);
        pieChart.animateY(1500);
        pieChart.invalidate();
    }

}
