package com.example.vmoov;


public class PatientMetrics {

    private String userId;

    // Constructor vacío necesario para Firebase
    public PatientMetrics() {
    }

    // Constructor que solo toma el userId
    public PatientMetrics(String userId) {
        this.userId = userId;
    }

    // Getter y Setter para userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
