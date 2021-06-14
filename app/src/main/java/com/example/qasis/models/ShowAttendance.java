package com.example.qasis.models;

import java.util.List;

public class ShowAttendance{

    private User_att user;
    private String datetime, latitude, longitude, isUpdated,last_datetime;
    private int type,id;

    public ShowAttendance(User_att user, String datetime, String latitude, String longitude, String isUpdated, String last_datetime, int type, int id) {
        this.user = user;
        this.datetime = datetime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isUpdated = isUpdated;
        this.last_datetime = last_datetime;
        this.type = type;
        this.id = id;
    }

    public void setUser(User_att user) {
        this.user = user;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setIsUpdated(String isUpdated) {
        this.isUpdated = isUpdated;
    }

    public void setLast_datetime(String last_datetime) {
        this.last_datetime = last_datetime;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User_att getUser() {
        return user;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getIsUpdated() {
        return isUpdated;
    }

    public String getLast_datetime() {
        return last_datetime;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}
