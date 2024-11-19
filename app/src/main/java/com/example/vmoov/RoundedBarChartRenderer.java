package com.example.vmoov;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    private final float radius = 20f; // Ajusta este valor para cambiar el radio de las esquinas

    public RoundedBarChartRenderer(BarChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dataSet.getColor());

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);

            // Calcula el rectángulo manualmente
            RectF barRect = new RectF();
            barRect.left = entry.getX() - 0.3f;
            barRect.right = entry.getX() + 0.3f;
            barRect.top = Math.min(0, entry.getY());
            barRect.bottom = Math.max(0, entry.getY());

            // Convierte las coordenadas a píxeles
            mChart.getTransformer(dataSet.getAxisDependency()).rectValueToPixel(barRect);

            // Dibuja la barra con esquinas redondeadas
            Path path = new Path();
            path.addRoundRect(barRect, radius, radius, Path.Direction.CW);
            c.drawPath(path, paint);
        }
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        for (Highlight highlight : indices) {
            IBarDataSet set = mChart.getBarData().getDataSetByIndex(highlight.getDataSetIndex());

            if (set == null) continue;

            BarEntry entry = set.getEntryForXValue(highlight.getX(), highlight.getY());

            if (entry == null) continue;

            RectF barRect = new RectF();
            barRect.left = entry.getX() - 0.3f;
            barRect.right = entry.getX() + 0.3f;
            barRect.top = Math.min(0, entry.getY());
            barRect.bottom = Math.max(0, entry.getY());

            mChart.getTransformer(set.getAxisDependency()).rectValueToPixel(barRect);

            Paint highlightPaint = new Paint();
            highlightPaint.setColor(set.getHighLightColor());

            Path path = new Path();
            path.addRoundRect(barRect, radius, radius, Path.Direction.CW);
            c.drawPath(path, highlightPaint);
        }
    }
}
