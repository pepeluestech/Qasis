package com.example.qasis;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.qasis.models.Attendance;
import com.example.qasis.models.Respuesta;
import com.example.qasis.webservice.WebServiceClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

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

public class Frag_Fichar extends Fragment {

    private TextInputEditText dni;
    private CheckBox recuerdame;
    private Button fichar, salir;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private Attendance attendance;
    private FusedLocationProviderClient locationClient;
    private double longitude;
    private double latitude;
    private static final int REQUEST_CODE = 10;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        checkLocation();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean value = preferences.getBoolean("oscuro", false);
        if(value){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        fichar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dni_text = dni.getText().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss. SSS'Z'", Locale.getDefault());
                Date date = new Date();
                String fecha = dateFormat.format(date);

                if (dni_text.isEmpty()) {
                    Toast.makeText(getActivity(), "Introduce tu DNI para poder fichar", Toast.LENGTH_SHORT).show();
                } else if (dni_text.length() < 9) {
                    Toast.makeText(getActivity(), "Introduce un DNI valido", Toast.LENGTH_SHORT).show();
                } else {
                    attendance = new Attendance(dni_text, fecha,1, latitude, longitude );
                    lanzarPeticion(dni_text, 0);
                }
            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dni_text = dni.getText().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss. SSS'Z'", Locale.getDefault());
                Date date = new Date();

                String fecha = dateFormat.format(date);
                if (dni_text.isEmpty()) {
                    Toast.makeText(getActivity(), "Introduce tu DNI para poder salir", Toast.LENGTH_SHORT).show();
                } else if (dni_text.length() < 9) {
                    Toast.makeText(getActivity(), "Introduce un DNI valido", Toast.LENGTH_SHORT).show();
                } else {
                    attendance = new Attendance(dni_text,fecha ,2, latitude, longitude);
                    lanzarPeticion(dni_text, 1);
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_fichar, container, false);

        dni = view.findViewById(R.id.dni);
        recuerdame = view.findViewById(R.id.recuerdame);
        fichar = view.findViewById(R.id.fichar);
        salir = view.findViewById(R.id.iniciar);

        SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        dni.setText(preferences.getString("dni_user", ""));

        return view;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!= null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        });
    }

    private void checkLocation(){
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(getActivity(), "No se han aceptado los permisos de ubicacion", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void lanzarPeticion(String dni_text, int type){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        Call<Respuesta> llamada = client.doAttendance(attendance);
        llamada.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                Respuesta respuesta = response.body();
                if(respuesta != null){
                    String mensaje  = respuesta.getResponse();
                    if(response.isSuccessful()){
                        if(type == 0){
                            Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                        }else if (type == 1){
                            Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                        }

                        SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = preferences.edit();
                        if(recuerdame.isChecked()){
                            edit.putString("dni_user", dni_text);
                            edit.commit();
                        }else{
                            edit.putString("dni_user", "");
                            edit.commit();
                        }
                }
                }else{
                    Toast.makeText(getActivity(), "Las credenciales introducidas son invalidas", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
