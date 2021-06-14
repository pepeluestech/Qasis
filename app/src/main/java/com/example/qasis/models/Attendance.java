package com.example.qasis.models;

public class Attendance {
    private String dni, date;
    private int type;
    private double latitude, longitude;

    public Attendance(String dni, String date, int type, double latitude, double longitude) {
        this.dni = dni;
        this.date = date;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getDni() {
        return dni;
    }

    public int getType() {
        return type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
