package com.example.vmoov;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MetricsAdapter extends RecyclerView.Adapter<MetricsAdapter.MetricsViewHolder> {

    private List<Metric> metricsList;

    public MetricsAdapter(List<Metric> metricsList) {
        this.metricsList = metricsList;
    }

    @NonNull
    @Override
    public MetricsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.metric_item, parent, false);
        return new MetricsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetricsViewHolder holder, int position) {
        Metric metric = metricsList.get(position);

        // Set values for each item
        holder.dateTextView.setText("Fecha: " + metric.getDate());
        holder.successRateTextView.setText("Movimientos exitosos: " + metric.getTrueCount() + " de " + metric.getTotalSteps());
        holder.averageTimeTextView.setText("Tiempo promedio de ejecución: " + String.format("%.2f s", metric.getAverageTime()));
        holder.durationTextView.setText("Duración de la sesión: " + String.format("%.2f s", metric.getDuration()));
    }

    @Override
    public int getItemCount() {
        return metricsList.size();
    }

    public static class MetricsViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView successRateTextView;
        TextView averageTimeTextView;
        TextView durationTextView;

        public MetricsViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the TextViews from the item layout
            dateTextView = itemView.findViewById(R.id.dateTextView);
            successRateTextView = itemView.findViewById(R.id.successRateTextView);
            averageTimeTextView = itemView.findViewById(R.id.averageTimeTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
        }
    }
}

