package com.example.vmoov;

public class Patient {
    private String birthDate;
    private String obraSocial;
    private String numeroAfiliado;
    private String nombrePS;
    private String apellidoPS;
    private int uniqueCode; // Nuevo atributo para almacenar el código único de 4 dígitos

    public Patient(String birthDate, String obraSocial, String numeroAfiliado, String nombrePS, String apellidoPS, int uniqueCode) {
        this.birthDate = birthDate;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.nombrePS = nombrePS;
        this.apellidoPS = apellidoPS;
        this.uniqueCode = uniqueCode;
    }

    // Getters y Setters
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getobraSocial() {
        return obraSocial;
    }

    public void setobraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
    }

    public String getnumeroAfiliado() {
        return numeroAfiliado;
    }

    public void setnumeroAfiliado(String numeroAfiliado) {
        this.numeroAfiliado = numeroAfiliado;
    }

    public String getnombrePS() {
        return nombrePS;
    }

    public void setnombrePS(String nombrePS) {
        this.nombrePS = nombrePS;
    }

    public String getapellidoPS() {
        return apellidoPS;
    }

    public void setapellidoPS(String apellidoPS) {
        this.apellidoPS = apellidoPS;
    }

    public int getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(int uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
