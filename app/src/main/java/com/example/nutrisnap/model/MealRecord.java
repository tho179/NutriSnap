package com.example.nutrisnap.model;

import java.util.List;

public class MealRecord {
    private String mealType; // Ví dụ: "Sáng", "Trưa", "Tối"
    private long timeLogged; // Lưu dưới dạng Timestamp
    private List<FoodItem> foods; // Mảng chứa các món ăn trong bữa này

    public MealRecord() {}

    public MealRecord(String mealType, long timeLogged, List<FoodItem> foods) {
        this.mealType = mealType;
        this.timeLogged = timeLogged;
        this.foods = foods;
    }

    // --- Getters & Setters ---
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public long getTimeLogged() { return timeLogged; }
    public void setTimeLogged(long timeLogged) { this.timeLogged = timeLogged; }

    public List<FoodItem> getFoods() { return foods; }
    public void setFoods(List<FoodItem> foods) { this.foods = foods; }
}