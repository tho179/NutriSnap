package com.example.nutrisnap.model;

public class DailySummary {
    private String id;
    private String userId;
    private String date;
    private double totalCaloriesIn;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;

    public DailySummary() {}

    public DailySummary(String id, String userId, String date, double totalCaloriesIn, double totalProtein, double totalCarbs, double totalFat) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.totalCaloriesIn = totalCaloriesIn;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFat = totalFat;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTotalCaloriesIn() { return totalCaloriesIn; }
    public void setTotalCaloriesIn(double totalCaloriesIn) { this.totalCaloriesIn = totalCaloriesIn; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }
}
