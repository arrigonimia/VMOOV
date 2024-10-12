package com.example.vmoov;

public class HealthProfessionalActivity {
    private String userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
    private String profession;

    // Constructor vacío requerido por Firebase
    public HealthProfessionalActivity() {
    }

    // Constructor con parámetros
    public HealthProfessionalActivity(String userId, String firstName, String lastName, String dni, String email, String profession) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
        this.profession = profession;
    }

    // Getters y setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
}
