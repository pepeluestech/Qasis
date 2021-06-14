package com.example.qasis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qasis.models.Code;
import com.example.qasis.models.Collecciones;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.models.UpdateInfo;
import com.example.qasis.webservice.WebServiceClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Update_Attendance_class extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    TextView textView;
    Button button, cambiar;
    int day, month, year, hour, minute, id;
    int myday, myMonth, myYear, myHour, myMinute;
    String date, token;
    private Retrofit retrofit;
    UpdateInfo updateInfo;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(Update_Attendance_class.this, Update_Attendance_class.this,year, month,day);
                datePickerDialog.show();
            }
        });

        cambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateInfo updateInfo = new UpdateInfo(id, date);
                lanzarPeticion("Bearer "+ token, updateInfo);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myYear = year;
        myday = day;
        myMonth = month + 1;

        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(Update_Attendance_class.this, Update_Attendance_class.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;

        textView.setText("Year: " + myYear + "\n" +
                "Month: " + myMonth + "\n" +
                "Day: " + myday + "\n" +
                "Hour: " + myHour + "\n" +
                "Minute: " + myMinute);
        date =  myday  + "/" + myMonth + "/" + myYear + " " + myHour + ":" + myMinute;
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
                        Toast.makeText(Update_Attendance_class.this, "No se pudó actualizar la asistencia", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(Update_Attendance_class.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


