package com.example.vmoov;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.List;

public class ChartUtils {

    public static void configureBarChart(BarChart chart, List<BarEntry> entries, List<String> labels) {
        Context context = chart.getContext();

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(context, R.color.violet),
                ContextCompat.getColor(context, R.color.blue)
        );
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setLabelRotationAngle(45);

        chart.getDescription().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(1500);
        chart.invalidate();
    }
}
