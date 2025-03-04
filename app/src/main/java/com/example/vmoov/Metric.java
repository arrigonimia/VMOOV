package com.example.vmoov;

public class Metric {
    private long startTime;
    private long endTime; // Añadido endTime
    private int trueCount;
    private double averageTime;
    private String gameDuration; // Formateado para mostrar horas:minutos
    private long gameDurationSeconds;  // Nuevo campo en segundos

    private int stepCount;

    // Constructor que incluye endTime
    public Metric(long startTime, long endTime, int trueCount, double averageTime, String gameDuration, int stepCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.trueCount = trueCount;
        this.averageTime = averageTime;
        this.gameDuration = gameDuration;
        this.stepCount = stepCount;
        this.gameDurationSeconds = (endTime - startTime) / 1000; // Calcular duración en segundos
    }


    // Getters
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; } // Nuevo getter para endTime
    public int getTrueCount() { return trueCount; }
    public double getAverageTime() { return averageTime; }
    public String getGameDuration() { return gameDuration; }
    public long getGameDurationSeconds() { return gameDurationSeconds; }

    public int getStepCount() { return stepCount; }
}
