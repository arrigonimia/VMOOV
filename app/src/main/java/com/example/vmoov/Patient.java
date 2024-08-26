package com.example.vmoov;

public class Patient {
    private String birthDate;
    private String bloodType;
    private String epilepsyTherapy;
    private String medications;
    private String pathologies;
    private int uniqueCode; // Nuevo atributo para almacenar el código único de 4 dígitos

    public Patient(String birthDate, String bloodType, String epilepsyTherapy, String medications, String pathologies, int uniqueCode) {
        this.birthDate = birthDate;
        this.bloodType = bloodType;
        this.epilepsyTherapy = epilepsyTherapy;
        this.medications = medications;
        this.pathologies = pathologies;
        this.uniqueCode = uniqueCode;
    }

    // Getters y Setters
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getEpilepsyTherapy() {
        return epilepsyTherapy;
    }

    public void setEpilepsyTherapy(String epilepsyTherapy) {
        this.epilepsyTherapy = epilepsyTherapy;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getPathologies() {
        return pathologies;
    }

    public void setPathologies(String pathologies) {
        this.pathologies = pathologies;
    }

    public int getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(int uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
