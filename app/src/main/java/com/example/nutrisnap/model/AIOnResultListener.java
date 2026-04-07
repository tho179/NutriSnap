package com.example.nutrisnap.model;

public interface AIOnResultListener {
    void onSuccess(MealRecord recognizedMeal);
    void onError(String message);
}
