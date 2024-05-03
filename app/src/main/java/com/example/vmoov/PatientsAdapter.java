package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.PatientViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;

    private Context context;
    private List<String> patientNames;

    public PatientsAdapter(Context context, List<String> patientNames, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.patientNames = patientNames;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setPatientNames(List<String> patientNames) {
        this.patientNames = patientNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.patient_row, parent, false);
        return new PatientViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        String patientName = patientNames.get(position);
        holder.bind(patientName);
    }

    @Override
    public int getItemCount() {
        return patientNames.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        private TextView patientNameTextView;

        public PatientViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientNameTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }

        public void bind(String patientName) {
            patientNameTextView.setText(patientName);
        }
    }
}
