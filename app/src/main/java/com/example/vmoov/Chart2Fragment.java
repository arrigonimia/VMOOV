package com.example.vmoov;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class Chart2Fragment extends Fragment {

    private DatabaseReference mDatabase;
    private String selectedNumber;
    private String selectedType;

    public static Chart2Fragment newInstance(DatabaseReference database, String selectedNumber, String selectedType) {
        Chart2Fragment fragment = new Chart2Fragment();
        Bundle args = new Bundle();
        args.putString("selectedNumber", selectedNumber);
        args.putString("selectedType", selectedType);
        fragment.setArguments(args);
        fragment.mDatabase = database;  // 🔹 Asignamos la referencia sin volver a declararla
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedNumber = getArguments().getString("selectedNumber");
            selectedType = getArguments().getString("selectedType");
        }
    }

    // 🔹 Método setter opcional si necesitas asignar `mDatabase` después de la creación del fragmento
    public void setDatabase(DatabaseReference database) {
        this.mDatabase = database;
    }
}
