package com.example.qasis.models;

public class User_att {

    private int id;
    private String dni;

    public User_att(int id, String dni) {
        this.id = id;
        this.dni = dni;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public int getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }
}
