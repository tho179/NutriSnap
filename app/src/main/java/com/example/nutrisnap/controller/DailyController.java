package com.example.nutrisnap.controller;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.nutrisnap.model.DailyDataCallback;
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

    public void fetchDailySummary(String userId, String date, DailyDataCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot userDoc) {
                        if (!userDoc.exists()) {
                            callback.onFailure(new Exception("Không tìm thấy thông tin người dùng"));
                            return;
                        }

                        DailySummaryData summary = new DailySummaryData();
                        summary.targetCalories = userDoc.contains("targetCalories") ? userDoc.getDouble("targetCalories").intValue() : 2500;
                        summary.carbsTarget = userDoc.contains("targetCarbs") ? userDoc.getDouble("targetCarbs").intValue() : 224;
                        summary.proteinTarget = userDoc.contains("targetProtein") ? userDoc.getDouble("targetProtein").intValue() : 128;
                        summary.fatTarget = userDoc.contains("targetFat") ? userDoc.getDouble("targetFat").intValue() : 128;
                        summary.waterTarget = userDoc.contains("targetWater") ? userDoc.getDouble("targetWater").intValue() : 2000;

                        // Set meal targets (could be calculated or from DB)
                        summary.breakfastTarget = (int) (summary.targetCalories * 0.3);
                        summary.lunchTarget = (int) (summary.targetCalories * 0.35);
                        summary.dinnerTarget = (int) (summary.targetCalories * 0.25);
                        summary.snackTarget = (int) (summary.targetCalories * 0.1);

                        fetchDailyLogs(userId, date, summary, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private void fetchDailyLogs(String userId, String date, DailySummaryData summary, DailyDataCallback callback) {
        db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot dailyDoc) {
                        if (dailyDoc.exists()) {
                            summary.totalCaloriesIn = dailyDoc.contains("totalCaloriesIn") ? dailyDoc.getDouble("totalCaloriesIn").intValue() : 0;
                            summary.carbsEaten = dailyDoc.contains("totalCarbs") ? dailyDoc.getDouble("totalCarbs").intValue() : 0;
                            summary.proteinEaten = dailyDoc.contains("totalProtein") ? dailyDoc.getDouble("totalProtein").intValue() : 0;
                            summary.fatEaten = dailyDoc.contains("totalFat") ? dailyDoc.getDouble("totalFat").intValue() : 0;
                            summary.waterDrank = dailyDoc.contains("totalWater") ? dailyDoc.getDouble("totalWater").intValue() : 0;
                            
                            summary.breakfastKcal = dailyDoc.contains("breakfastKcal") ? dailyDoc.getDouble("breakfastKcal").intValue() : 0;
                            summary.lunchKcal = dailyDoc.contains("lunchKcal") ? dailyDoc.getDouble("lunchKcal").intValue() : 0;
                            summary.dinnerKcal = dailyDoc.contains("dinnerKcal") ? dailyDoc.getDouble("dinnerKcal").intValue() : 0;
                            summary.snackKcal = dailyDoc.contains("snackKcal") ? dailyDoc.getDouble("snackKcal").intValue() : 0;
                        } else {
                            summary.totalCaloriesIn = 0;
                            summary.carbsEaten = 0;
                            summary.proteinEaten = 0;
                            summary.fatEaten = 0;
                            summary.waterDrank = 0;
                            summary.breakfastKcal = 0;
                            summary.lunchKcal = 0;
                            summary.dinnerKcal = 0;
                            summary.snackKcal = 0;
                        }

                        summary.kcalLeft = summary.targetCalories - summary.totalCaloriesIn;
                        if (summary.kcalLeft < 0) summary.kcalLeft = 0;

                        callback.onSuccess(summary);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}
