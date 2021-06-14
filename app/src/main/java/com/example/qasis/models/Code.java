package com.example.qasis.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Code {
    private int code;
    private JsonObject data;

    public Code(int code, JsonObject data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public JsonObject getData() {
        return data;
    }
}
