package com.example.qasis;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qasis.models.Attendance;
import com.example.qasis.models.Code;
import com.example.qasis.models.Collecciones;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.RespuestaBase64;
import com.example.qasis.models.Respuesta_Data;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.models.ShowAttendance;
import com.example.qasis.webservice.WebServiceClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Attendance_class extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Attendance_adapter adapter;
    private List<ShowAttendance> attendanceList;
    private FloatingActionButton sing_out;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private GridLayoutManager layoutManager;
    private String token, user_id;
    static final int code_activity = 3;
    private Button settings, search;
    private ProgressBar loadProgress;
    private static final String FILE_NAME = "texto.txt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_attendances);
        super.onCreate(savedInstanceState);
        setTitle("Registro de Asistencias");
        sing_out = findViewById(R.id.exit);
        settings = findViewById(R.id.settings);
        search = findViewById(R.id.search);
        loadProgress = findViewById(R.id.progressBar);
        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        user_id = preferences.getString("user_id", "");
        token = preferences.getString("token", "");
        setupview();
        lanzarPeticion("Bearer "+token,user_id);

        sing_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_class.this);

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

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_class.this);
                builder.setTitle("Introduce una fecha para ver las asistencias registradas");
                builder.setMessage("El formato de la fecha debe ser: mm/YYYY");
                final EditText input = new EditText(Attendance_class.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String month = input.getText().toString();
                        lanzarPeticion3("Bearer " + token, user_id, month);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Attendance_class.this, Settings_class.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_class.this);

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
        recyclerView = findViewById(R.id.recyclerView);
        attendanceList = new ArrayList<>();
        adapter = new Attendance_adapter(attendanceList, this, new Attendance_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(ShowAttendance attendance) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Attendance_class.this);
                builder.setTitle("¿Que deseas hacer con la asistencia?");
                builder.setMessage("¿Deseas cambiar la algún ajuste de la asistencia o deseas ubicar la asistencia?");
                builder.setPositiveButton("Ajustar asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = attendance.getId();
                        Bundle extras = new Bundle();
                        extras.putInt("id_attendance", id);
                        Intent intent = new Intent(Attendance_class.this, Update_Attendance_class_v2.class);
                        intent.putExtras(extras);
                        Attendance_class.this.startActivityForResult(intent, code_activity);
                    }
                });
                builder.setNeutralButton("Eliminar la asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = attendance.getId();
                        lanzarPeticion2("Bearer "+token, id);
                    }
                });
                builder.setNegativeButton("Ubicar asistencia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String latitude = attendance.getLatitude();
                        String longitude = attendance.getLongitude();
                        Uri intentUri = Uri.parse("geo:"+latitude+","+ longitude + "?z=<zoom>&q="+latitude+","+ longitude +"(Ubicación)");
                        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
                        Attendance_class.this.startActivity(intent);
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

    private void lanzarPeticion(String token, String user_id){
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
                        loadProgress.setVisibility(View.GONE);
                    }else{
                        Toast.makeText(Attendance_class.this, "Ha habido un error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta_General> call, Throwable t) {
                Log.d("RETROFIT", "Error" + t.getMessage());
            }
        });
    }

    private void lanzarPeticion2(String token_peticion ,int attendance_id){
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
                        lanzarPeticion("Bearer "+token,user_id);
                    }else{
                    Toast.makeText(Attendance_class.this, "La asistencia no pudó ser borrada", Toast.LENGTH_SHORT).show();
                    }
        }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {

            }
        });
    }

    private void lanzarPeticion3(String token, String user_id , String month){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("month", month);

        Call<Respuesta_Data> llamada = client.searchRegister(token, user_id, hashMap);
        llamada.enqueue(new Callback<Respuesta_Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<Respuesta_Data> call, Response<Respuesta_Data> response) {
                if(response.isSuccessful()){
                    Respuesta_Data respuesta = response.body();

                    int permissionCheck = ContextCompat.checkSelfPermission(Attendance_class.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    ActivityCompat.requestPermissions(Attendance_class.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        Log.i("Mensaje", "No se tiene permiso para leer.");
                    } else {
                        saveFile();
                    }



//                    try (FileOutputStream fos = new FileOutputStream(file); ) {
//                        String b64 = respuesta.getRespuestaBase64().getData();
//
//                        byte[] decoder = Base64.getDecoder().decode(String.valueOf(b64));
//
//                        fos.write(decoder);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta_Data> call, Throwable t) {
                Log.d("a", t.getMessage());
            }
        });
    }


    private void saveFile(){
        String textoASalvar = "hola";
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(textoASalvar.getBytes());
            Log.d("TAG1", "Fichero Salvado en: " + getFilesDir() + "/" + FILE_NAME);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fileOutputStream != null){
                try{
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == code_activity && resultCode == RESULT_OK){
            lanzarPeticion("Bearer "+token,user_id);
        }
    }


}
