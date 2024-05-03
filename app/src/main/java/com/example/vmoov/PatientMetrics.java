package com.example.vmoov;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PatientMetrics {
    private Float calibrate;
    private Integer seizureCount;
    private String seizureTimes;
    private Float seizureDuration;

    // Default constructor with default values
    public PatientMetrics() {
        this.calibrate = 0.0f;
        this.seizureCount = 0;
        this.seizureTimes = "";
        this.seizureDuration = 0.0f;
    }

    // Constructor with parameters
    public PatientMetrics(Float calibrate, Integer seizureCount, String seizureTimes, Float seizureDuration) {
        this.calibrate = calibrate;
        this.seizureCount = seizureCount;
        this.seizureTimes = seizureTimes;
        this.seizureDuration = seizureDuration;
    }

    // Getters and Setters
    public Float getCalibrate() {
        return calibrate;
    }

    public void setCalibrate(Float calibrate) {
        this.calibrate = calibrate;
    }

    public Integer getSeizureCount() {
        return seizureCount;
    }

    public void setSeizureCount(Integer seizureCount) {
        this.seizureCount = seizureCount;
    }

    public String getSeizureTimes() {
        return seizureTimes;
    }

    public void setSeizureTimes(String seizureTimes) {
        this.seizureTimes = seizureTimes;
    }

    public Float getSeizureDuration() {
        return seizureDuration;
    }

    public void setSeizureDuration(Float seizureDuration) {
        this.seizureDuration = seizureDuration;
    }

    // Method to update metrics when new measurements are added
    public void updateMetrics(Float newCalibrate, Integer newSeizureCount, String newSeizureTimes, Float newSeizureDuration) {
        this.calibrate = newCalibrate;
        this.seizureCount = newSeizureCount;
        this.seizureTimes = newSeizureTimes;
        this.seizureDuration = newSeizureDuration;
    }

    // Serialize LocalDateTime to a format Firebase can handle (e.g., as a String)
    public static String serializeLocalDateTime(LocalDateTime dateTime) {
        return DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
    }

    // Deserialize a String to LocalDateTime
    public static LocalDateTime deserializeLocalDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
    }

}
