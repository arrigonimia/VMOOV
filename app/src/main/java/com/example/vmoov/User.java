package com.example.vmoov;

public class User {
    private String firstName;
    private String lastName;
    private String dni;
    private String gender;
    private String email;
    private String password;
    private int userType;

    // Constructor
    public User(String firstName, String lastName, String dni, String gender, String email, String password, int userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    // Getters y Setters para cada atributo
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
