package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nutrisnap.R;
import com.example.nutrisnap.controller.DailyController;
import com.example.nutrisnap.model.DailyDataCallback;
import com.example.nutrisnap.model.DailySummaryData;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PlanFragment extends Fragment {

    // Daily Target
    private TextView tvTargetKcal, tvTargetCarbs, tvTargetProtein, tvTargetFat;

    // Nutrition Progress
    private ProgressBar pbKcal, pbCarbs, pbProtein, pbFat;
    private TextView tvKcalVal, tvCarbsVal, tvProteinVal, tvFatVal;

    private DailyController dailyController;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        dailyController = new DailyController();
        mAuth = FirebaseAuth.getInstance();

        initViews(view);
        setupListeners(view);
        
        // Load real data from Firestore
        fetchPlanData();

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
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_plan);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void fetchPlanData() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());

        dailyController.fetchDailySummary(userId, today, new DailyDataCallback() {
            @Override
            public void onSuccess(DailySummaryData data) {
                if (!isAdded()) return;

                // 1. Cập nhật Daily Target
                tvTargetKcal.setText(String.valueOf(data.targetCalories));
                tvTargetCarbs.setText(data.carbsTarget + "g");
                tvTargetProtein.setText(data.proteinTarget + "g");
                tvTargetFat.setText(data.fatTarget + "g");

                // 2. Cập nhật Nutrition Progress
                // Kcal
                tvKcalVal.setText(data.totalCaloriesIn + "/" + data.targetCalories);
                pbKcal.setMax(data.targetCalories);
                pbKcal.setProgress(data.totalCaloriesIn);

                // Carbs
                tvCarbsVal.setText(data.carbsEaten + "/" + data.carbsTarget + "g");
                pbCarbs.setMax(data.carbsTarget);
                pbCarbs.setProgress(data.carbsEaten);

                // Protein
                tvProteinVal.setText(data.proteinEaten + "/" + data.proteinTarget + "g");
                pbProtein.setMax(data.proteinTarget);
                pbProtein.setProgress(data.proteinEaten);

                // Fat
                tvFatVal.setText(data.fatEaten + "/" + data.fatTarget + "g");
                pbFat.setMax(data.fatTarget);
                pbFat.setProgress(data.fatEaten);
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
