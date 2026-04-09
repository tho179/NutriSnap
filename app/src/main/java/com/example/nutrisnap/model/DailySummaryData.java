package com.example.nutrisnap.model;

public class DailySummaryData {
    public int kcalLeft;
    public int totalCaloriesIn;
    public int targetCalories;

    public int carbsEaten;
    public int carbsTarget;

    public int proteinEaten;
    public int proteinTarget;

    public int fatEaten;
    public int fatTarget;

    public int waterDrank;
    public int waterTarget;

    // Weight tracking
    public float weight;

    // Meal specific kcal
    public int breakfastKcal;
    public int lunchKcal;
    public int dinnerKcal;
    public int snackKcal;

    // Optional: Meal targets if stored in DB, otherwise we can calculate/use defaults
    public int breakfastTarget;
    public int lunchTarget;
    public int dinnerTarget;
    public int snackTarget;

    public DailySummaryData() {}
}
