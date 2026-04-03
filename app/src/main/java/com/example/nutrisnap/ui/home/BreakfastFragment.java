package com.example.nutrisnap.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nutrisnap.R;

public class BreakfastFragment extends BaseMealFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_breakfast_detail, container, false);
        setupBaseViews(view);
        return view;
    }

    @Override
    protected String getMealType() {
        return "Sáng";
    }
}
