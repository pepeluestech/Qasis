package com.example.qasis.models;

import com.google.gson.annotations.SerializedName;

public class Respuesta_Data {

    @SerializedName("response")
    private RespuestaBase64 respuestaBase64;

    public Respuesta_Data(RespuestaBase64 respuestaBase64) {
        this.respuestaBase64 = respuestaBase64;
    }

    public RespuestaBase64 getRespuestaBase64() {
        return respuestaBase64;
    }

    public void setRespuestaBase64(RespuestaBase64 respuestaBase64) {
        this.respuestaBase64 = respuestaBase64;
    }
}
