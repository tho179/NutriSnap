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

public class HeightFragment extends Fragment implements SetupProfileActivity.SetupDataInterface {

    private TextView tvUnitCm, tvUnitFt;
    private EditText etHeight;
    private boolean isCmSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_height, container, false);

        tvUnitCm = view.findViewById(R.id.tv_unit_cm);
        tvUnitFt = view.findViewById(R.id.tv_unit_ft);
        etHeight = view.findViewById(R.id.et_setup_height);

        tvUnitCm.setOnClickListener(v -> selectUnit(true));
        tvUnitFt.setOnClickListener(v -> selectUnit(false));

        selectUnit(true);
        return view;
    }

    private void selectUnit(boolean isCm) {
        isCmSelected = isCm;
        if (isCm) {
            tvUnitCm.setBackgroundResource(R.drawable.bg_button_green);
            tvUnitCm.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tvUnitFt.setBackgroundResource(R.drawable.bg_unit_toggle);
            tvUnitFt.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            etHeight.setHint("170");
        } else {
            tvUnitFt.setBackgroundResource(R.drawable.bg_button_green);
            tvUnitFt.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tvUnitCm.setBackgroundResource(R.drawable.bg_unit_toggle);
            tvUnitCm.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            etHeight.setHint("5.6");
        }
    }

    @Override
    public void saveData() {
        String heightStr = etHeight.getText().toString();
        if (!heightStr.isEmpty()) {
            try {
                double height = Double.parseDouble(heightStr);
                // Nếu là feet, đổi sang cm để lưu thống nhất
                if (!isCmSelected) {
                    height = height * 30.48;
                }
                ((SetupProfileActivity) requireActivity()).updateUserField("height", height);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }
}
