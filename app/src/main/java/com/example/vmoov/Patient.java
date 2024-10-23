package com.example.vmoov;

public class Patient {
    private String birthDate;
    private String contacto;
    private String obraSocial;
    private String numeroAfiliado;
    private int uniqueCode; // Nuevo atributo para almacenar el código único de 4 dígitos

    public Patient(String birthDate, String contacto, String obraSocial, String numeroAfiliado, int uniqueCode) {
        this.birthDate = birthDate;
        this.contacto = contacto;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.uniqueCode = uniqueCode;
    }

    // Getters y Setters
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
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

    public int getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(int uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
