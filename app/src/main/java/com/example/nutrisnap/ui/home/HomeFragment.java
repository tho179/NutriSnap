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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nutrisnap.R;
import com.example.nutrisnap.controller.DailyController;
import com.example.nutrisnap.model.DailyDataCallback;
import com.example.nutrisnap.model.DailySummaryData;
import com.example.nutrisnap.ui.notification.NotificationFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    // 1. Khai báo các biến giao diện
    private TextView tvHomeDate, tvWaterProgress;
    private ProgressBar progressWater;

    // Khai báo thêm UI cho Dinh dưỡng (Kcal, Carbs, Protein, Fat)
    private TextView tvKcalLeft, tvCarbs, tvProtein, tvFat;
    private ProgressBar progressKcal, progressCarbs, progressProtein, progressFat;

    // UI cho các bữa ăn
    private TextView tvBreakfastKcal, tvLunchKcal, tvDinnerKcal, tvSnacksKcal;
    private ProgressBar progressBreakfast, progressLunch, progressDinner, progressSnacks;

    private final Calendar calendar = Calendar.getInstance();

    // 2. Khai báo các biến Firebase
    private DailyController dailyController;
    private String currentUserId;
    private String currentDateStr; // Ngày đang chọn, định dạng yyyy-MM-dd

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Firebase Auth và Controller
        dailyController = new DailyController();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "dS9TNWPv9llMeaRfRFi9"; // Dùng tạm lúc test chưa code đăng nhập
        }

        // Ánh xạ View cơ bản
        tvHomeDate = view.findViewById(R.id.tv_home_date);
        tvWaterProgress = view.findViewById(R.id.tv_water_progress);
        progressWater = view.findViewById(R.id.progress_water);

        tvKcalLeft = view.findViewById(R.id.tv_kcal_left);
        tvCarbs = view.findViewById(R.id.tv_carbs);
        tvProtein = view.findViewById(R.id.tv_protein);
        tvFat = view.findViewById(R.id.tv_fat);

        progressKcal = view.findViewById(R.id.progress_kcal);
        progressCarbs = view.findViewById(R.id.progress_carbs);
        progressProtein = view.findViewById(R.id.progress_protein);
        progressFat = view.findViewById(R.id.progress_fat);

        // Ánh xạ View cho Meals
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
        ImageView btnAddWater = view.findViewById(R.id.btn_add_water);
        ImageView btnNotification = view.findViewById(R.id.btn_notification);

        // Sự kiện chuyển ngày
        imgCalendarPicker.setOnClickListener(v -> showDatePicker());
        imgPrevDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateLabelAndFetchData(); // Cập nhật ngày và tải lại dữ liệu
        });
        imgNextDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateLabelAndFetchData(); // Cập nhật ngày và tải lại dữ liệu
        });

        btnAddWater.setOnClickListener(v -> showDrinkWaterDialog());

        // Chạy lần đầu tiên khi mở Fragment
        updateDateLabelAndFetchData();

        // Các sự kiện chuyển trang (giữ nguyên)
        if (btnNotification != null) btnNotification.setOnClickListener(v -> navigateToFragment(new NotificationFragment()));
        view.findViewById(R.id.layout_breakfast).setOnClickListener(v -> navigateToFragment(new BreakfastFragment()));
        view.findViewById(R.id.layout_lunch).setOnClickListener(v -> navigateToFragment(new LunchFragment()));
        view.findViewById(R.id.layout_dinner).setOnClickListener(v -> navigateToFragment(new DinnerFragment()));
        view.findViewById(R.id.layout_snacks).setOnClickListener(v -> navigateToFragment(new SnacksFragment()));

        return view;
    }

    // =========================================================
    // PHẦN LOGIC TẢI DỮ LIỆU TỪ FIREBASE
    // =========================================================

    private void updateDateLabelAndFetchData() {
        // 1. Cập nhật Text hiển thị ngày
        Calendar today = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvHomeDate.setText("Today, " + new SimpleDateFormat("MMM dd", Locale.US).format(calendar.getTime()));
        } else {
            tvHomeDate.setText(new SimpleDateFormat("EEE, MMM dd", Locale.US).format(calendar.getTime()));
        }

        // 2. Chuyển đổi ngày đang chọn thành chuỗi "yyyy-MM-dd" để truy vấn Firebase
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        currentDateStr = dbFormat.format(calendar.getTime());

        // 3. Gọi Controller tải dữ liệu
        fetchDataForSelectedDate();
    }

    private void fetchDataForSelectedDate() {
        dailyController.fetchDailySummary(currentUserId, currentDateStr, new DailyDataCallback() {
            @Override
            public void onSuccess(DailySummaryData data) {
                // Kiểm tra Fragment còn tồn tại không trước khi update UI
                if (!isAdded()) return;

                // Cập nhật Kcal trung tâm
                if(tvKcalLeft != null) tvKcalLeft.setText(String.valueOf(data.kcalLeft));
                if(progressKcal != null) {
                    progressKcal.setMax(data.targetCalories);
                    progressKcal.setProgress(data.totalCaloriesIn);
                }

                // Cập nhật Carbs
                if(tvCarbs != null)
                    tvCarbs.setText(data.carbsEaten + "\n/" + data.carbsTarget + "g");
                if(progressCarbs != null) {
                    progressCarbs.setMax(data.carbsTarget); progressCarbs.setProgress(data.carbsEaten);
                }

                // Cập nhật Protein
                if(tvProtein != null)
                    tvProtein.setText(data.proteinEaten + "\n/" + data.proteinTarget + "g");
                if(progressProtein != null) {
                    progressProtein.setMax(data.proteinTarget); progressProtein.setProgress(data.proteinEaten);
                }

                // Cập nhật Fat
                if(tvFat != null)
                    tvFat.setText(data.fatEaten + "\n/" + data.fatTarget + "g");
                if(progressFat != null) {
                    progressFat.setMax(data.fatTarget); progressFat.setProgress(data.fatEaten);
                }

                // Cập nhật Water
                if(tvWaterProgress != null)
                    tvWaterProgress.setText(data.waterDrank + "\n/" + data.waterTarget + "ml");
                if(progressWater != null) {
                    progressWater.setMax(data.waterTarget); progressWater.setProgress(data.waterDrank);
                }

                // CẬP NHẬT DỮ LIỆU CÁC BỮA ĂN (MEALS)
                updateMealUI(tvBreakfastKcal, progressBreakfast, data.breakfastKcal, data.breakfastTarget);
                updateMealUI(tvLunchKcal, progressLunch, data.lunchKcal, data.lunchTarget);
                updateMealUI(tvDinnerKcal, progressDinner, data.dinnerKcal, data.dinnerTarget);
                updateMealUI(tvSnacksKcal, progressSnacks, data.snackKcal, data.snackTarget);
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Log.e("HomeFragment", "Lỗi tải dữ liệu: " + e.getMessage());
                Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMealUI(TextView tvKcal, ProgressBar progress, int current, int target) {
        if (tvKcal != null) {
            tvKcal.setText(current + " / " + target + " kcal");
        }
        if (progress != null) {
            progress.setMax(target);
            progress.setProgress(current);
        }
    }

    // =========================================================
    // PHẦN LOGIC THÊM NƯỚC UỐNG VÀ LƯU FIREBASE
    // =========================================================

    private void showDrinkWaterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_drink_water, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText edtAmount = dialogView.findViewById(R.id.edt_water_amount);
        Button btnDrink = dialogView.findViewById(R.id.btn_drink_dialog);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        btnDrink.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                saveWaterToFirebase(amount); // Đẩy lên DB
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập lượng nước", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveWaterToFirebase(int amountToAdd) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lưu dữ liệu vào document của ngày đang chọn
        Map<String, Object> updates = new HashMap<>();
        // Dùng FieldValue.increment() để Firebase tự động cộng dồn (rất an toàn)
        updates.put("totalWater", FieldValue.increment(amountToAdd));

        db.collection("users").document(currentUserId)
                .collection("daily_logs").document(currentDateStr)
                .set(updates, SetOptions.merge()) // SetOptions.merge() giúp giữ nguyên các trường khác (như calo, carbs)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã thêm " + amountToAdd + "ml nước", Toast.LENGTH_SHORT).show();
                    // Sau khi lưu thành công, tải lại toàn bộ giao diện cho mới
                    fetchDataForSelectedDate();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi lưu nước uống", Toast.LENGTH_SHORT).show();
                });
    }

    // =========================================================
    // PHẦN DATE PICKER VÀ NAVIGATE (Giữ nguyên)
    // =========================================================

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabelAndFetchData(); // Cập nhật ngày và tải lại dữ liệu
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}