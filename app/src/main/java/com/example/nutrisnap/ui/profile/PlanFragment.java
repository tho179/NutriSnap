package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class PlanFragment extends Fragment {

    // Daily Target
    private TextView tvTargetKcal, tvTargetCarbs, tvTargetProtein, tvTargetFat;

    // Nutrition Progress
    private ProgressBar pbKcal, pbCarbs, pbProtein, pbFat;
    private TextView tvKcalVal, tvCarbsVal, tvProteinVal, tvFatVal;

    // Meal Recommendations
    private TextView tvRecBreakfastTitle, tvRecBreakfastItem1, tvRecBreakfastItem2;
    private TextView tvRecLunchTitle, tvRecLunchItem1, tvRecLunchItem2;
    private TextView tvRecDinnerTitle, tvRecDinnerItem1, tvRecDinnerItem2;
    private TextView tvRecSnackTitle, tvRecSnackItem1, tvRecSnackItem2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        initViews(view);
        setupListeners(view);
        
        // Mock data update - later this will be from API
        updatePlanData();

        return view;
    }

    private void initViews(View view) {
        // Daily Target
        tvTargetKcal = view.findViewById(R.id.tv_plan_target_kcal);
        tvTargetCarbs = view.findViewById(R.id.tv_plan_target_carbs);
        tvTargetProtein = view.findViewById(R.id.tv_plan_target_protein);
        tvTargetFat = view.findViewById(R.id.tv_plan_target_fat);

        // Nutrition Progress
        pbKcal = view.findViewById(R.id.pb_plan_kcal);
        pbCarbs = view.findViewById(R.id.pb_plan_carbs);
        pbProtein = view.findViewById(R.id.pb_plan_protein);
        pbFat = view.findViewById(R.id.pb_plan_fat);
        
        tvKcalVal = view.findViewById(R.id.tv_plan_kcal_val);
        tvCarbsVal = view.findViewById(R.id.tv_plan_carbs_val);
        tvProteinVal = view.findViewById(R.id.tv_plan_protein_val);
        tvFatVal = view.findViewById(R.id.tv_plan_fat_val);

        // Meal Recommendations
        tvRecBreakfastTitle = view.findViewById(R.id.tv_plan_rec_breakfast_title);
        tvRecBreakfastItem1 = view.findViewById(R.id.tv_plan_rec_breakfast_item1);
        tvRecBreakfastItem2 = view.findViewById(R.id.tv_plan_rec_breakfast_item2);

        tvRecLunchTitle = view.findViewById(R.id.tv_plan_rec_lunch_title);
        tvRecLunchItem1 = view.findViewById(R.id.tv_plan_rec_lunch_item1);
        tvRecLunchItem2 = view.findViewById(R.id.tv_plan_rec_lunch_item2);

        tvRecDinnerTitle = view.findViewById(R.id.tv_plan_rec_dinner_title);
        tvRecDinnerItem1 = view.findViewById(R.id.tv_plan_rec_dinner_item1);
        tvRecDinnerItem2 = view.findViewById(R.id.tv_plan_rec_dinner_item2);

        tvRecSnackTitle = view.findViewById(R.id.tv_plan_rec_snack_title);
        tvRecSnackItem1 = view.findViewById(R.id.tv_plan_rec_snack_item1);
        tvRecSnackItem2 = view.findViewById(R.id.tv_plan_rec_snack_item2);
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_plan);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void updatePlanData() {
        // This is where you will call your API. For now, we set the values from the image.
        
        // Target
        tvTargetKcal.setText("2500");
        tvTargetCarbs.setText("224g");
        tvTargetProtein.setText("128g");
        tvTargetFat.setText("128g");

        // Progress (Current/Goal)
        tvKcalVal.setText("1200/2500");
        pbKcal.setProgress(48);

        tvCarbsVal.setText("40/224g");
        pbCarbs.setProgress(18);

        tvProteinVal.setText("80/128g");
        pbProtein.setProgress(62);

        tvFatVal.setText("100/128g");
        pbFat.setProgress(78);

        // Meal Recommendations
        tvRecBreakfastTitle.setText(getString(R.string.breakfast_kcal, 450));
        tvRecBreakfastItem1.setText(R.string.bread);
        tvRecBreakfastItem2.setText(R.string.egg);

        tvRecLunchTitle.setText(getString(R.string.lunch_kcal, 800));
        tvRecLunchItem1.setText(R.string.pho);
        tvRecLunchItem2.setText(R.string.salad);

        tvRecDinnerTitle.setText(getString(R.string.dinner_kcal, 800));
        tvRecDinnerItem1.setText(R.string.brown_rice);
        tvRecDinnerItem2.setText(R.string.egg);

        tvRecSnackTitle.setText(getString(R.string.snack_kcal, 450));
        tvRecSnackItem1.setText(R.string.greek_yogurt);
        tvRecSnackItem2.setText(R.string.almonds);
    }
}