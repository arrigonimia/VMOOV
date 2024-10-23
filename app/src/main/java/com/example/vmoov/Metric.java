package com.example.vmoov;

public class Metric {
    private String date;  // Add the date of the session
    private int trueCount;
    private double averageTime;
    private double duration;
    private int totalSteps;

    public Metric(String date, int trueCount, double averageTime, double duration, int totalSteps) {
        this.date = date;
        this.trueCount = trueCount;
        this.averageTime = averageTime;
        this.duration = duration;
        this.totalSteps = totalSteps;
    }

    public String getDate() {
        return date;
    }

    public int getTrueCount() {
        return trueCount;
    }

    public double getAverageTime() {
        return averageTime;
    }

    public double getDuration() {
        return duration;
    }

    public int getTotalSteps() {
        return totalSteps;
    }
}
