package com.example.qasis.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.http.Tag;

public class Collecciones {

    @SerializedName("collection")
    private List<ShowAttendance> showAttendances;

    public Collecciones(List<ShowAttendance> showAttendances) {
        this.showAttendances = showAttendances;
    }

    public void setShowAttendances(List<ShowAttendance> showAttendances) {
        this.showAttendances = showAttendances;
    }

    public List<ShowAttendance> getShowAttendances() {
        return showAttendances;
    }
}
