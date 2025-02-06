package com.example.vmoov;

public class User {
    private String firstName;
    private String lastName;
    private String dni;
    private String gender;
    private String phone; // Nuevo campo de número de teléfono
    private String email;
    private String password;
    private int userType;

    // Constructor vacío requerido por Firebase
    public User() {
    }

    // Constructor con todos los atributos
    public User(String firstName, String lastName, String dni, String gender, String phone, String email, String password, int userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.gender = gender;
        this.phone = phone; // Nuevo campo
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    // Getters y Setters
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

    public String getPhone() { // Getter para el número de teléfono
        return phone;
    }

    public void setPhone(String phone) { // Setter para el número de teléfono
        this.phone = phone;
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
