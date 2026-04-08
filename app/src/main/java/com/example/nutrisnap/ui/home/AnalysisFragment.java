package com.example.nutrisnap.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nutrisnap.R;
import com.example.nutrisnap.model.FoodItem;
import com.example.nutrisnap.model.MealRecord;
import java.util.Locale;

public class AnalysisFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "image_uri";
    private Uri imageUri;
    private int quantity = 1;

    private FoodItem currentFood;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
            String name = getArguments().getString("food_name", "Unknown");
            int kcal = getArguments().getInt("food_kcal", 0);
            double protein = getArguments().getDouble("food_protein", 0);
            double carbs = getArguments().getDouble("food_carbs", 0);
            double fat = getArguments().getDouble("food_fat", 0);
            currentFood = new FoodItem(name, 1, kcal, protein, carbs, fat);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        ImageView imgFood = view.findViewById(R.id.img_food_analysis);
        EditText edtFoodName = view.findViewById(R.id.edt_food_name_analysis);
        ImageView btnEditName = view.findViewById(R.id.btn_edit_name);
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

        if (currentFood != null) {
            edtFoodName.setText(currentFood.getName());
            updateNutrition(nutriChart, tvKcal, tvCarbsSummary, tvProteinSummary, tvFatSummary, tvQuantity);
        }

        if (btnEditName != null) {
            btnEditName.setOnClickListener(v -> {
                edtFoodName.requestFocus();
                edtFoodName.setSelection(edtFoodName.getText().length());
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
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
            String editedName = edtFoodName.getText().toString().trim();
            if (editedName.isEmpty()) editedName = currentFood.getName();

            Bundle result = new Bundle();
            result.putString("food_name", editedName);
            result.putInt("food_kcal", (int) currentFood.getCalories() * quantity);
            result.putDouble("food_protein", currentFood.getProtein() * quantity);
            result.putDouble("food_carbs", currentFood.getCarbs() * quantity);
            result.putDouble("food_fat", currentFood.getFat() * quantity);
            result.putParcelable("food_image", imageUri);
            
            getParentFragmentManager().setFragmentResult("add_food_request", result);
            Navigation.findNavController(v).popBackStack();
        });

        return view;
    }

    private void updateNutrition(NutriChartView chart, TextView tvKcal, TextView tvCarbs, TextView tvProtein, TextView tvFat, TextView tvQty) {
        tvQty.setText(String.valueOf(quantity));
        tvKcal.setText(String.valueOf((int)currentFood.getCalories() * quantity));
        
        float currentProtein = (float) currentFood.getProtein() * quantity;
        float currentCarbs = (float) currentFood.getCarbs() * quantity;
        float currentFat = (float) currentFood.getFat() * quantity;

        tvProtein.setText(String.format(Locale.getDefault(), "● Protein-%.1fg", currentProtein));
        tvCarbs.setText(String.format(Locale.getDefault(), "● Carbs-%.1fg", currentCarbs));
        tvFat.setText(String.format(Locale.getDefault(), "● Fat-%.1fg", currentFat));

        if (chart != null) {
            chart.setData(currentProtein, currentCarbs, currentFat);
        }
    }
}