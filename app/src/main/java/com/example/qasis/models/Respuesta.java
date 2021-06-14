package com.example.qasis.models;

public class Respuesta {
    private int code;
    private String response;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public Respuesta(int code, String response) {
        this.code = code;
        this.response = response;
    }
}
