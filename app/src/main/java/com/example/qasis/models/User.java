package com.example.qasis.models;

import com.google.android.material.textfield.TextInputEditText;

public class User {
    private String name,email, created_at, updated_at, dayType,naf;
    private int id, role;
    private boolean active;

    public User(String name, String email, String created_at, String updated_at, String dayType, String naf, int id, int role, boolean active) {
        this.name = name;
        this.email = email;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.dayType = dayType;
        this.naf = naf;
        this.id = id;
        this.role = role;
        this.active = active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public void setNaf(String naf) {
        this.naf = naf;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getDayType() {
        return dayType;
    }

    public String getNaf() {
        return naf;
    }

    public int getId() {
        return id;
    }

    public int getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
