package com.example.nutrisnap.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;
import java.util.Locale;

public class AnalysisFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "image_uri";
    private Uri imageUri;
    private int quantity = 1;

    private float baseProtein = 10f;
    private float baseCarbs = 6f;
    private float baseFat = 4f;
    private int baseKcal = 150;

    public static AnalysisFragment newInstance(Uri imageUri) {
        AnalysisFragment fragment = new AnalysisFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        ImageView imgFood = view.findViewById(R.id.img_food_analysis);
        TextView tvFoodName = view.findViewById(R.id.tv_food_name_analysis);
        NutriChartView nutriChart = view.findViewById(R.id.nutri_chart_analysis);
        TextView tvKcal = view.findViewById(R.id.tv_kcal_analysis);
        
        TextView tvCarbsSummary = view.findViewById(R.id.tv_carbs_summary);
        TextView tvProteinSummary = view.findViewById(R.id.tv_protein_summary);
        TextView tvFatSummary = view.findViewById(R.id.tv_fat_summary);

        TextView tvQuantity = view.findViewById(R.id.tv_quantity_analysis);
        ImageView btnMinus = view.findViewById(R.id.btn_minus_analysis);
        ImageView btnPlus = view.findViewById(R.id.btn_plus_analysis);
        ImageView btnBack = view.findViewById(R.id.btn_back_analysis);
        Button btnAdd = view.findViewById(R.id.btn_add_to_meal);

        if (imageUri != null) {
            imgFood.setImageURI(imageUri);
        }

        updateNutrition(nutriChart, tvKcal, tvCarbsSummary, tvProteinSummary, tvFatSummary, tvQuantity);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                quantity++;
                updateNutrition(nutriChart, tvKcal, tvCarbsSummary, tvProteinSummary, tvFatSummary, tvQuantity);
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    updateNutrition(nutriChart, tvKcal, tvCarbsSummary, tvProteinSummary, tvFatSummary, tvQuantity);
                }
            });
        }

        btnAdd.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("food_name", tvFoodName.getText().toString());
            result.putInt("food_kcal", baseKcal * quantity);
            result.putDouble("food_protein", (double) baseProtein * quantity);
            result.putDouble("food_carbs", (double) baseCarbs * quantity);
            result.putDouble("food_fat", (double) baseFat * quantity);
            result.putParcelable("food_image", imageUri);
            
            getParentFragmentManager().setFragmentResult("add_food_request", result);
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void updateNutrition(NutriChartView chart, TextView tvKcal, TextView tvCarbs, TextView tvProtein, TextView tvFat, TextView tvQty) {
        tvQty.setText(String.valueOf(quantity));
        tvKcal.setText(String.valueOf(baseKcal * quantity));
        
        float currentProtein = baseProtein * quantity;
        float currentCarbs = baseCarbs * quantity;
        float currentFat = baseFat * quantity;

        tvProtein.setText(String.format(Locale.getDefault(), "● Protein-%.0fg", currentProtein));
        tvCarbs.setText(String.format(Locale.getDefault(), "● Carbs-%.0fg", currentCarbs));
        tvFat.setText(String.format(Locale.getDefault(), "● Fat-%.0fg", currentFat));

        if (chart != null) {
            chart.setData(currentProtein, currentCarbs, currentFat);
        }
    }
}
