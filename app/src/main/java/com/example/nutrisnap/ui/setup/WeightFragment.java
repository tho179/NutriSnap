package com.example.nutrisnap.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class WeightFragment extends Fragment {

    private TextView tvUnitKg, tvUnitLb;
    private EditText etWeight;
    private boolean isKgSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight, container, false);

        tvUnitKg = view.findViewById(R.id.tv_unit_kg);
        tvUnitLb = view.findViewById(R.id.tv_unit_lb);
        etWeight = view.findViewById(R.id.et_setup_weight);

        tvUnitKg.setOnClickListener(v -> selectUnit(true));
        tvUnitLb.setOnClickListener(v -> selectUnit(false));

        // Mặc định chọn kg
        selectUnit(true);

        return view;
    }

    private void selectUnit(boolean isKg) {
        isKgSelected = isKg;
        if (isKg) {
            // kg selected
            tvUnitKg.setBackgroundResource(R.drawable.bg_button_green);
            tvUnitKg.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            
            tvUnitLb.setBackgroundResource(R.drawable.bg_unit_toggle);
            tvUnitLb.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            
            etWeight.setHint("70");
        } else {
            // lb selected
            tvUnitLb.setBackgroundResource(R.drawable.bg_button_green);
            tvUnitLb.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            
            tvUnitKg.setBackgroundResource(R.drawable.bg_unit_toggle);
            tvUnitKg.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            
            etWeight.setHint("154");
        }
        // Xóa text cũ khi chuyển đơn vị để tránh nhầm lẫn
        etWeight.setText("");
    }

    public boolean isKgSelected() {
        return isKgSelected;
    }

    public String getWeightValue() {
        return etWeight.getText().toString();
    }
}