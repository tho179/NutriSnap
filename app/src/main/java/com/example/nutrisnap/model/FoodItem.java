package com.example.nutrisnap.model;

public class FoodItem {
    private String name;
    private int amount;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    public FoodItem() {}

    public FoodItem(String name, int amount, double calories, double protein, double carbs, double fat) {
        this.name = name;
        this.amount = amount;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    // --- Getters & Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
}
