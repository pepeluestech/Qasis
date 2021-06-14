package com.example.qasis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qasis.models.Code;
import com.example.qasis.models.Collecciones;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.models.ShowAttendance;
import com.example.qasis.webservice.WebServiceClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Attendance_Admin_class extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Attendance_adapter adapter;
    private List<ShowAttendance> attendanceList;
    private FloatingActionButton sing_out2;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private GridLayoutManager layoutManager;
    private String token, user_id;
    private Switch myAttendances;
    static final int code_activity = 3;
    private Button settings_admin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_attendances_admin);
        super.onCreate(savedInstanceState);
        setTitle("Registro de Asistencias");
        sing_out2 = findViewById(R.id.sing_out2);
        settings_admin = findViewById(R.id.settings_admin);
        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        user_id = preferences.getString("user_id", "");
        token = preferences.getString("token", "");

        setupview();
        lanzarPeticion("Bearer "+token);

        myAttendances = findViewById(R.id.show);

        myAttendances.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    lanzarPeticion("Bearer "+token);
                }else{
                    LanzarPeticion2("Bearer "+token, user_id);
                }
            }
        });

        sing_out2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_Admin_class.this);

                builder.setTitle("Cerrar Sesión");
                builder.setMessage("¿Desea cerrar sesión de Qasis?");
                builder.setPositiveButton("Cerrar sesion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
        });

        settings_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Attendance_Admin_class.this, Settings_class.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_Admin_class.this);

        builder.setTitle("Cerrar Sesión");
        builder.setMessage("¿Desea cerrar sesión de Qasis?");
        builder.setPositiveButton("Cerrar sesion", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void setupview (){
        recyclerView = findViewById(R.id.recyclerView2);
        attendanceList = new ArrayList<>();
        adapter = new Attendance_adapter(attendanceList, this, new Attendance_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(ShowAttendance attendance) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_Admin_class.this);
                builder.setTitle("¿Que deseas hacer con la asistencia?");
                builder.setMessage("¿Deseas cambiar la algún ajuste de la asistencia o deseas ubicar la asistencia?");
                builder.setPositiveButton("Ajustar asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = attendance.getId();
                        Bundle extras = new Bundle();
                        extras.putInt("id_attendance", id);
                        Intent intent = new Intent(Attendance_Admin_class.this, Update_Attendance_class_v2.class);
                        intent.putExtras(extras);
                        Attendance_Admin_class.this.startActivityForResult(intent, code_activity);
                    }
                });
                builder.setNeutralButton("Eliminar la asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = attendance.getId();
                        lanzarPeticion3("Bearer "+token, id);
                    }
                });
                builder.setNegativeButton("Ubicar asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String latitude = attendance.getLatitude();
                        String longitude = attendance.getLongitude();
                        Uri intentUri = Uri.parse("geo:"+latitude+","+ longitude + "?z=<zoom>&q="+latitude+","+ longitude +"(Ubicación)");
                        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
                        Attendance_Admin_class.this.startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void lanzarPeticion(String token){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);
        Call<Respuesta_General> llamada = client.showAllAttendances(token);
        llamada.enqueue(new Callback<Respuesta_General>() {
            @Override
            public void onResponse(Call<Respuesta_General> call, Response<Respuesta_General> response) {
                if(response.isSuccessful()){
                    Respuesta_General respuesta_general = response.body();
                    Code code = respuesta_general.getResponse();

                    if(code.getCode() == 1){
                        JsonObject data = respuesta_general.getResponse().getData();
                        Gson gson = new Gson();
                        Collecciones collecciones = gson.fromJson(data, Collecciones.class);
                        attendanceList = collecciones.getShowAttendances();
                        adapter.setAttendances(attendanceList);
                    }else{
                        Toast.makeText(Attendance_Admin_class.this, "Ha habido un error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta_General> call, Throwable t) {
                Log.d("RETROFIT", "Error" + t.getMessage());
            }
        });

    }

    private void LanzarPeticion2(String token, String user_id){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);
        Call<Respuesta_General> llamada = client.showAttendances(token,user_id);
        llamada.enqueue(new Callback<Respuesta_General>() {
            @Override
            public void onResponse(Call<Respuesta_General> call, Response<Respuesta_General> response) {
                if(response.isSuccessful()){
                    Respuesta_General respuesta = response.body();
                    Code code = respuesta.getResponse();

                    if(code.getCode() == 1){
                        JsonObject data = respuesta.getResponse().getData();
                        Gson gson = new Gson();
                        Collecciones collecciones = gson.fromJson(data, Collecciones.class);
                        attendanceList = collecciones.getShowAttendances();
                        adapter.setAttendances(attendanceList);
                    }else{
                        Toast.makeText(Attendance_Admin_class.this, "Ha habido un error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta_General> call, Throwable t) {
                Log.d("RETROFIT", "Error" + t.getMessage());
            }
        });
    }

    private void lanzarPeticion3(String token_peticion ,int attendance_id){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        HashMap<String, String> hashMap = new HashMap<>();

        String id = String.valueOf(attendance_id);

        hashMap.put("id", id);

        WebServiceClient client = retrofit.create(WebServiceClient.class);
        Call<Respuesta> llamada = client.destroyAttendance(token_peticion, hashMap);
        llamada.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                if(response.isSuccessful()){
                    Respuesta respuesta = response.body();
                    if(respuesta.getCode() == 1){
                        if(myAttendances.isChecked()){
                            lanzarPeticion("Bearer "+token);
                        }else{
                            LanzarPeticion2("Bearer "+token, user_id);
                        }
                    }else{
                        Toast.makeText(Attendance_Admin_class.this, "La asistencia no pudó ser borrada", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == code_activity && resultCode == RESULT_OK){
            if(myAttendances.isChecked()){
                lanzarPeticion("Bearer "+token);
            }else{
                LanzarPeticion2("Bearer "+token, user_id);
            }
        }
    }
}
