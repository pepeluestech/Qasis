package com.example.qasis.webservice;

import com.example.qasis.models.Attendance;
import com.example.qasis.models.RespuestaBase64;
import com.example.qasis.models.Respuesta_Data;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.UpdateInfo;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServiceClient {

    public static final String BASE_URL = "https://test1.qastusoft.com.es/qasis/public/api/";

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("login")
    Call<Respuesta_General> doLogin (@Body HashMap<String,String> user);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("ficharApi")
    Call<Respuesta> doAttendance (@Body Attendance attendance);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("asistencias/editar")
    Call<Respuesta> updateAttendance (@Header("Authorization") String authKey, @Body UpdateInfo updateInfo);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("asistencias/eliminar")
    Call<Respuesta> destroyAttendance (@Header("Authorization") String authKey, @Body HashMap<String, String> attendance_id);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @GET("asistencias/{user_id}")
    Call<Respuesta_General> showAttendances (@Header("Authorization") String authKey , @Path("user_id") String user_id);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @GET("asistencias")
    Call<Respuesta_General> showAllAttendances(@Header("Authorization") String authKey);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("profile/new_pass")
    Call<Respuesta> changePassword(@Header("Authorization") String authKey, @Body HashMap<String, String> password);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("password/update")
    Call<Respuesta> forgotPassword(@Body HashMap<String,String> email);

    @Headers({"Content-Type: application/json", "Accept: */*"})
    @POST("registros/{user_id}/buscarApi")
    Call<Respuesta_Data> searchRegister(@Header("Authorization") String authKey , @Path("user_id") String user_id, @Body HashMap<String, String> month);
}
