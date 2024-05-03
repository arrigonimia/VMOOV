package com.example.vmoov;

public class Patient {
    private String birth;
    private String blood_type;
    private String epiTherapy;
    private String meds;
    private String pathologies;

    // Constructors
    public Patient(String birth, String blood_type, String epiTherapy, String meds, String pathologies) {
        this.birth = birth;
        this.blood_type = blood_type;
        this.epiTherapy = epiTherapy;
        this.meds = meds;
        this.pathologies = pathologies;
    }

    // Getters and Setters
    public String getBirthDate() {
        return birth;
    }

    public void setBirthDate(String firstName) {
        this.birth = birth;
    }

    public String getblood_type() {
        return blood_type;
    }

    public void setblood_type(String blood_type) {
        this.blood_type = blood_type;
    }

    public String getepiTherapy() {
        return epiTherapy;
    }

    public void setepiTherapy(String epiTherapy) {
        this.epiTherapy = epiTherapy;
    }

    public String getmeds() {
        return meds;
    }

    public void setmeds(String meds) {
        this.meds = meds;
    }

    public String getpathologies() {
        return pathologies;
    }

    public void setpathologies(String pathologies) {
        this.pathologies = pathologies;
    }

}
