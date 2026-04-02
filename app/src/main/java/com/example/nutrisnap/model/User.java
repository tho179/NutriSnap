package com.example.nutrisnap.model;

public class User {
    // Thông tin cơ bản
    private String username;
    private String name;
    private String dateOfBirth;
    private String gender;
    private String address;

    // Chỉ số cơ thể hiện tại (Từ bảng CurrentBMI)
    private double currentWeight;
    private double currentHeight;

    // Mục tiêu cơ thể (Từ bảng TargetBMI)
    private double targetWeight;
    private double targetHeight;

    // Mục tiêu dinh dưỡng (Từ bảng NutritionGoal)
    private double targetCalories;
    private double targetProtein;
    private double targetCarbs;
    private double targetFat;

    private double amount;

    public User() {}

    public User(String username, String name, String dateOfBirth, String gender, String address,
                double currentWeight, double currentHeight, double targetWeight, double targetHeight,
                double targetCalories, double targetProtein, double targetCarbs, double targetFat, double amount) {
        this.username = username;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.currentWeight = currentWeight;
        this.currentHeight = currentHeight;
        this.targetWeight = targetWeight;
        this.targetHeight = targetHeight;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
        this.amount = amount;
    }

    // --- Getters & Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }

    public double getCurrentHeight() { return currentHeight; }
    public void setCurrentHeight(double currentHeight) { this.currentHeight = currentHeight; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public double getTargetHeight() { return targetHeight; }
    public void setTargetHeight(double targetHeight) { this.targetHeight = targetHeight; }

    public double getTargetCalories() { return targetCalories; }
    public void setTargetCalories(double targetCalories) { this.targetCalories = targetCalories; }

    public double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(double targetProtein) { this.targetProtein = targetProtein; }

    public double getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(double targetCarbs) { this.targetCarbs = targetCarbs; }

    public double getTargetFat() { return targetFat; }
    public void setTargetFat(double targetFat) { this.targetFat = targetFat; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}