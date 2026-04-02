package com.example.nutrisnap.controller;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.nutrisnap.model.DailySummaryData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DailyController {

    private FirebaseFirestore db;

    public DailyController() {
        db = FirebaseFirestore.getInstance();
    }

    // Tạo một Interface để truyền dữ liệu về màn hình UI sau khi tải xong
    public interface DailyDataCallback {
        void onSuccess(DailySummaryData data);
        void onFailure(Exception e);
    }

    /**
     * Hàm lấy tổng hợp dữ liệu của một ngày
     * @param userId ID của người dùng (vd: "user123")
     * @param date Ngày cần lấy (vd: "2026-04-01")
     * @param callback Hàm gọi lại khi hoàn tất
     */
    public void fetchDailySummary(String userId, String date, DailyDataCallback callback) {

        // 1. Lấy thông tin Mục tiêu (Targets) từ Profile User
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot userDoc) {
                        if (!userDoc.exists()) {
                            callback.onFailure(new Exception("Không tìm thấy thông tin người dùng"));
                            return;
                        }

                        // Khởi tạo đối tượng chứa dữ liệu
                        DailySummaryData summary = new DailySummaryData();

                        // Lấy các mục tiêu an toàn (tránh bị null)
                        int targetCalories = userDoc.contains("targetCalories") ? userDoc.getDouble("targetCalories").intValue() : 2500;
                        summary.carbsTarget = userDoc.contains("targetCarbs") ? userDoc.getDouble("targetCarbs").intValue() : 224;
                        summary.proteinTarget = userDoc.contains("targetProtein") ? userDoc.getDouble("targetProtein").intValue() : 128;
                        summary.fatTarget = userDoc.contains("targetFat") ? userDoc.getDouble("targetFat").intValue() : 128;
                        summary.waterTarget = userDoc.contains("targetWater") ? userDoc.getDouble("targetWater").intValue() : 2000;

                        // 2. Tiếp tục lấy thông tin đã tiêu thụ trong ngày hôm nay
                        fetchDailyLogs(userId, date, targetCalories, summary, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Hàm phụ trợ để lấy dữ liệu ăn uống trong ngày
    private void fetchDailyLogs(String userId, String date, int targetCalories,
                                DailySummaryData summary, DailyDataCallback callback) {

        db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot dailyDoc) {
                        int totalCaloriesIn = 0;

                        if (dailyDoc.exists()) {
                            // Lấy dữ liệu đã ăn/uống (nếu có bản ghi)
                            totalCaloriesIn = dailyDoc.contains("totalCaloriesIn") ? dailyDoc.getDouble("totalCaloriesIn").intValue() : 0;
                            summary.carbsEaten = dailyDoc.contains("totalCarbs") ? dailyDoc.getDouble("totalCarbs").intValue() : 0;
                            summary.proteinEaten = dailyDoc.contains("totalProtein") ? dailyDoc.getDouble("totalProtein").intValue() : 0;
                            summary.fatEaten = dailyDoc.contains("totalFat") ? dailyDoc.getDouble("totalFat").intValue() : 0;
                            summary.waterDrank = dailyDoc.contains("totalWater") ? dailyDoc.getDouble("totalWater").intValue() : 0;
                        } else {
                            // Nếu ngày mới chưa ăn gì, tất cả tiêu thụ = 0
                            summary.carbsEaten = 0;
                            summary.proteinEaten = 0;
                            summary.fatEaten = 0;
                            summary.waterDrank = 0;
                        }

                        // TÍNH TOÁN: Calo còn lại = Mục tiêu - Đã ăn
                        summary.kcalLeft = targetCalories - totalCaloriesIn;
                        // Đảm bảo số calo không bị âm (nếu ăn lố)
                        if (summary.kcalLeft < 0) summary.kcalLeft = 0;

                        // TRẢ KẾT QUẢ VỀ CHO GIAO DIỆN
                        callback.onSuccess(summary);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}
