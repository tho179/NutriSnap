package com.example.nutrisnap.ui.home;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class HomeFragment extends Fragment {

    private TextView tvHomeDate, tvWaterProgressCircular;
    private ProgressBar progressWaterCircular;
    private TextView tvKcalLeft, tvCarbs, tvProtein, tvFat;
    private ProgressBar progressKcal, progressCarbs, progressProtein, progressFat;
    private TextView tvBreakfastKcal, tvLunchKcal, tvDinnerKcal, tvSnacksKcal;
    private ProgressBar progressBreakfast, progressLunch, progressDinner, progressSnacks;

    private final Calendar calendar = Calendar.getInstance();
    private DailyController dailyController;
    private String currentUserId;
    private String currentDateStr;
    private String lastWarningDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dailyController = new DailyController();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "dS9TNWPv9llMeaRfRFi9";
        }

        tvHomeDate = view.findViewById(R.id.tv_home_date);
        tvWaterProgressCircular = view.findViewById(R.id.tv_water_progress_circular);
        progressWaterCircular = view.findViewById(R.id.progress_water_circular);

        tvKcalLeft = view.findViewById(R.id.tv_kcal_left);
        tvCarbs = view.findViewById(R.id.tv_carbs);
        tvProtein = view.findViewById(R.id.tv_protein);
        tvFat = view.findViewById(R.id.tv_fat);

        progressKcal = view.findViewById(R.id.progress_kcal);
        progressCarbs = view.findViewById(R.id.progress_carbs);
        progressProtein = view.findViewById(R.id.progress_protein);
        progressFat = view.findViewById(R.id.progress_fat);

        tvBreakfastKcal = view.findViewById(R.id.tv_breakfast_kcal);
        tvLunchKcal = view.findViewById(R.id.tv_lunch_kcal);
        tvDinnerKcal = view.findViewById(R.id.tv_dinner_kcal);
        tvSnacksKcal = view.findViewById(R.id.tv_snacks_kcal);

        progressBreakfast = view.findViewById(R.id.progress_breakfast);
        progressLunch = view.findViewById(R.id.progress_lunch);
        progressDinner = view.findViewById(R.id.progress_dinner);
        progressSnacks = view.findViewById(R.id.progress_snacks);

        ImageView imgCalendarPicker = view.findViewById(R.id.img_calendar_picker);
        ImageView imgPrevDate = view.findViewById(R.id.img_prev_date);
        ImageView imgNextDate = view.findViewById(R.id.img_next_date);
        ImageView btnAddWaterTop = view.findViewById(R.id.btn_add_water_top);
        ImageView btnNotification = view.findViewById(R.id.btn_notification);

        updateDateLabelAndFetchData();

        imgCalendarPicker.setOnClickListener(v -> showDatePicker());
        imgPrevDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateLabelAndFetchData();
        });
        imgNextDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateLabelAndFetchData();
        });

        if (btnAddWaterTop != null) btnAddWaterTop.setOnClickListener(v -> showDrinkWaterDialog());

        if (btnNotification != null) btnNotification.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_notification));
        view.findViewById(R.id.layout_breakfast).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_breakfast));
        view.findViewById(R.id.layout_lunch).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_lunch));
        view.findViewById(R.id.layout_dinner).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_dinner));
        view.findViewById(R.id.layout_snacks).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_snacks));

        return view;
    }

    private void updateDateLabelAndFetchData() {
        Calendar today = Calendar.getInstance();
        Locale locale = Locale.getDefault();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvHomeDate.setText(getString(R.string.today) + ", " + new SimpleDateFormat("dd 'Th'M", locale).format(calendar.getTime()));
        } else {
            tvHomeDate.setText(new SimpleDateFormat("EEE, dd 'Th'M", locale).format(calendar.getTime()));
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        currentDateStr = dbFormat.format(calendar.getTime());
        fetchDataForSelectedDate();
    }

    private void fetchDataForSelectedDate() {
        dailyController.fetchDailySummary(currentUserId, currentDateStr, new DailyDataCallback() {
            @Override
            public void onSuccess(DailySummaryData data) {
                if (!isAdded()) return;

                if(tvKcalLeft != null) {
                    if (data.totalCaloriesIn > data.targetCalories) {
                        int exceeded = data.totalCaloriesIn - data.targetCalories;
                        tvKcalLeft.setText("-" + exceeded);
                        tvKcalLeft.setTextColor(Color.RED);
                        
                        // Hiển thị cảnh báo 1 lần cho mỗi ngày
                        if (!currentDateStr.equals(lastWarningDate)) {
                            Toast.makeText(getContext(), "Cảnh báo: Bạn đã ăn vượt " + exceeded + " kcal mục tiêu trong ngày!", Toast.LENGTH_SHORT).show();
                            lastWarningDate = currentDateStr;
                        }
                    } else {
                        tvKcalLeft.setText(String.valueOf(data.kcalLeft));
                        tvKcalLeft.setTextColor(Color.BLACK); // Hoặc màu mặc định của app
                        
                        // Reset cảnh báo nếu lượng calo giảm xuống dưới mức mục tiêu (nếu có thể)
                        if (currentDateStr.equals(lastWarningDate)) {
                            lastWarningDate = "";
                        }
                    }
                }

                if(progressKcal != null) {
                    progressKcal.setMax(data.targetCalories);
                    progressKcal.setProgress(data.totalCaloriesIn);
                }

                if(tvCarbs != null) tvCarbs.setText(getString(R.string.nutrient_format, data.carbsEaten, data.carbsTarget));
                if(progressCarbs != null) {
                    progressCarbs.setMax(data.carbsTarget); progressCarbs.setProgress(data.carbsEaten);
                }

                if(tvProtein != null) tvProtein.setText(getString(R.string.nutrient_format, data.proteinEaten, data.proteinTarget));
                if(progressProtein != null) {
                    progressProtein.setMax(data.proteinTarget); progressProtein.setProgress(data.proteinEaten);
                }

                if(tvFat != null) tvFat.setText(getString(R.string.nutrient_format, data.fatEaten, data.fatTarget));
                if(progressFat != null) {
                    progressFat.setMax(data.fatTarget); progressFat.setProgress(data.fatEaten);
                }

                if(tvWaterProgressCircular != null) tvWaterProgressCircular.setText(getString(R.string.water_unit, data.waterDrank, data.waterTarget));
                if(progressWaterCircular != null) {
                    progressWaterCircular.setMax(data.waterTarget); progressWaterCircular.setProgress(data.waterDrank);
                }

                updateMealUI(tvBreakfastKcal, progressBreakfast, data.breakfastKcal, data.breakfastTarget);
                updateMealUI(tvLunchKcal, progressLunch, data.lunchKcal, data.lunchTarget);
                updateMealUI(tvDinnerKcal, progressDinner, data.dinnerKcal, data.dinnerTarget);
                updateMealUI(tvSnacksKcal, progressSnacks, data.snackKcal, data.snackTarget);
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Log.e("HomeFragment", "Error: " + e.getMessage());
            }
        });
    }

    private void updateMealUI(TextView tvKcal, ProgressBar progress, int current, int target) {
        if (tvKcal != null) {
            tvKcal.setText(getString(R.string.meal_kcal_format, current, target));
        }
        if (progress != null) {
            progress.setMax(target);
            progress.setProgress(current);
        }
    }

    private void showDrinkWaterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_drink_water, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText edtAmount = dialogView.findViewById(R.id.edt_water_amount);
        Button btnDrink = dialogView.findViewById(R.id.btn_drink_dialog);
        Button btnRemove = dialogView.findViewById(R.id.btn_remove_water_dialog);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog);

        ImageView btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        btnDrink.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                saveWaterToFirebase(Integer.parseInt(amountStr));
                dialog.dismiss();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                saveWaterToFirebase(-Integer.parseInt(amountStr));
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveWaterToFirebase(int amount) {
        dailyController.updateWaterIntake(currentUserId, currentDateStr, amount, new DailyController.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    fetchDataForSelectedDate();
                }
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabelAndFetchData();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}
