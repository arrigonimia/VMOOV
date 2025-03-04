package com.example.vmoov;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private int numberOfBars;
    private String type;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int numberOfBars, String type) {
        super(fragmentActivity);
        this.numberOfBars = numberOfBars;
        this.type = type;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return Chart1Fragment.newInstance(
                        "Duración Promedio",
                        "Tiempo promedio de ejecución de cada movimiento.",
                        type,
                        numberOfBars,
                        true,  // Este gráfico es de duración
                        false  // No es de cantidad de movimientos
                );
            case 1:
                return Chart1Fragment.newInstance(
                        "Movimientos Exitosos",
                        "Número de movimientos correctos realizados.",
                        type,
                        numberOfBars,
                        false, // No es de duración
                        true   // Este gráfico es de cantidad de movimientos
                );
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Dos gráficos en el ViewPager
    }
}
