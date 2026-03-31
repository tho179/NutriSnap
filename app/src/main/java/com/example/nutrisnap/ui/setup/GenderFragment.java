package com.example.nutrisnap.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class GenderFragment extends Fragment {

    private LinearLayout layoutMale, layoutFemale;
    private ImageView imgMale, imgFemale;
    private TextView tvMale, tvFemale;
    private boolean isMaleSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gender, container, false);

        layoutMale = view.findViewById(R.id.layout_male);
        layoutFemale = view.findViewById(R.id.layout_female);
        imgMale = view.findViewById(R.id.img_male);
        imgFemale = view.findViewById(R.id.img_female);
        tvMale = view.findViewById(R.id.tv_male);
        tvFemale = view.findViewById(R.id.tv_female);

        layoutMale.setOnClickListener(v -> selectGender(true));
        layoutFemale.setOnClickListener(v -> selectGender(false));

        // Mặc định chọn Nam
        selectGender(true);

        return view;
    }

    private void selectGender(boolean isMale) {
        isMaleSelected = isMale;
        if (isMale) {
            // Male selected
            imgMale.setBackgroundResource(R.drawable.bg_circle_green);
            tvMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvMale.setTypeface(null, android.graphics.Typeface.BOLD);

            imgFemale.setBackgroundResource(R.drawable.bg_circle_gray);
            tvFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvFemale.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            // Female selected
            imgFemale.setBackgroundResource(R.drawable.bg_circle_green);
            tvFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvFemale.setTypeface(null, android.graphics.Typeface.BOLD);

            imgMale.setBackgroundResource(R.drawable.bg_circle_gray);
            tvMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvMale.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    public boolean isMaleSelected() {
        return isMaleSelected;
    }
}