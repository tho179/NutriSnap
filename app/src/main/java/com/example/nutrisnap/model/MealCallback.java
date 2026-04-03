package com.example.nutrisnap.model;

public interface MealCallback {
    void onSuccess();
    void onFailure(Exception e);
}
