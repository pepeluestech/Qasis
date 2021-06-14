package com.example.qasis.models;

import java.util.List;

public class Info {
    private List<ShowAttendance> attendances;

    public Info(List<ShowAttendance> attendances) {
        this.attendances = attendances;
    }

    public void setAttendances(List<ShowAttendance> attendances) {
        this.attendances = attendances;
    }

    public List<ShowAttendance> getAttendances() {
        return attendances;
    }
}
