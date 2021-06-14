package com.example.qasis;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qasis.models.Respuesta;
import com.example.qasis.models.UpdateInfo;
import com.example.qasis.webservice.WebServiceClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Update_Attendance_class_v2 extends AppCompatActivity{
    TextView textView;
    Button button, cambiar;
    int id;
    String date, token;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_attendances);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.btnPick);
        cambiar = findViewById(R.id.cambiar);

        Intent intent = getIntent();
        id = intent.getExtras().getInt("id_attendance");
        Log.d("a", String.valueOf(id));

        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialog(textView);
            }
        });

        cambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = textView.getText().toString();
                UpdateInfo updateInfo = new UpdateInfo(id, date);
                lanzarPeticion("Bearer "+ token, updateInfo);

            }
        });
    }

    private void showDateTimeDialog(final TextView textView) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm");

                        Date date1 = calendar.getTime();

                        String fecha = simpleDateFormat.format(date1);

                        textView.setText(fecha);
                    }
                };
                new TimePickerDialog(Update_Attendance_class_v2.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
            }
        };

        new DatePickerDialog(Update_Attendance_class_v2.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void lanzarPeticion(String token, UpdateInfo updateInfo){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        Call<Respuesta> llamada = client.updateAttendance(token, updateInfo);
        llamada.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                Respuesta respuesta = response.body();
                if(response.isSuccessful()){
                    if(respuesta.getCode() == 1){
                        setResult(Activity.RESULT_OK);
                        finish();
                    }else{
                        Toast.makeText(Update_Attendance_class_v2.this, "No se pudó actualizar la asistencia", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(Update_Attendance_class_v2.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}


