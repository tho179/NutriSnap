package com.example.nutrisnap.model;

import java.util.Objects;

public class FoodItem {
    private String name;
    private int amount;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    private String imageUrl;

    public FoodItem() {}

    public FoodItem(String name, int amount, double calories, double protein, double carbs, double fat, String imageUrl) {
        this.name = name;
        this.amount = amount;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return amount == foodItem.amount &&
                Double.compare(foodItem.calories, calories) == 0 &&
                Double.compare(foodItem.protein, protein) == 0 &&
                Double.compare(foodItem.carbs, carbs) == 0 &&
                Double.compare(foodItem.fat, fat) == 0 &&
                Objects.equals(name, foodItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, calories, protein, carbs, fat);
    }
}
