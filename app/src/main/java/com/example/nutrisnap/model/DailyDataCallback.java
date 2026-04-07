package com.example.nutrisnap.model;

public interface DailyDataCallback {
    void onSuccess(DailySummaryData data);
    void onFailure(Exception e);
}
