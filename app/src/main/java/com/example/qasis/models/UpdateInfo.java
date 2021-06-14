package com.example.qasis.models;

public class UpdateInfo {

    private int id;

    public UpdateInfo(int id, String datetime) {
        this.id = id;
        this.datetime = datetime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public String getDatetime() {
        return datetime;
    }

    private String datetime;


}
