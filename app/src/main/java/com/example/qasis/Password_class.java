package com.example.qasis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.qasis.models.Code;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.webservice.WebServiceClient;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Password_class extends AppCompatActivity {
    
    private EditText old_pass_field, new_pass_field, repeat_pass_field;
    private String old_pass_text, new_pass_text, repeat_pass_text, token;
    private Button change, cancelar;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        old_pass_field = findViewById(R.id.old_pass);
        new_pass_field = findViewById(R.id.new_pass);
        repeat_pass_field = findViewById(R.id.repeat_pass);
        change = findViewById(R.id.change);
        cancelar = findViewById(R.id.cancelar_pass);
        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                old_pass_text = old_pass_field.getText().toString();
                new_pass_text = new_pass_field.getText().toString();
                repeat_pass_text = repeat_pass_field.getText().toString();

                if(old_pass_text.isEmpty()){
                    Toast.makeText(Password_class.this, "Introduce tu contraseña antigua", Toast.LENGTH_SHORT).show();
                }else if (new_pass_text.isEmpty()){
                    Toast.makeText(Password_class.this, "Introduce tu nueva contraseña", Toast.LENGTH_SHORT).show();
                }else if(repeat_pass_text.isEmpty()){
                    Toast.makeText(Password_class.this, "Introduce de nuevo tu nueva contraseña", Toast.LENGTH_SHORT).show();
                }else if (!new_pass_text.equals(repeat_pass_text)){
                    Toast.makeText(Password_class.this, "Introduce la misma contraseña para poder cambiarla", Toast.LENGTH_SHORT).show();
                }else if(old_pass_text.equals(new_pass_text)){
                    Toast.makeText(Password_class.this, "Escribe una contraseña distinta a la anterior", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(Password_class.this);
                    builder.setTitle("Cambio de contraseña");
                    builder.setMessage("¿Estas seguro de que deseas cambiar la contraeña?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lanzarPeticion("Bearer "+token,old_pass_text, new_pass_text, repeat_pass_text);
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void lanzarPeticion(String token, String old_pass, String new_pass, String verify_pass){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("old_password", old_pass);
        hashMap.put("new_password", new_pass);
        hashMap.put("verify_password", verify_pass);

        Call<Respuesta> llamada = client.changePassword(token, hashMap);
        llamada.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                Respuesta respuesta = response.body();
                
                if(response.isSuccessful()){
                    if(respuesta.getCode() == 1){
                        finish();
                    }else if (respuesta.getCode() == -1){
                        Toast.makeText(Password_class.this, "Comprueba que la antigua contraseña es la correcta", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Password_class.this, "Comprueba que la antigua contraseña es la correcta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {

            }
        });
        
    }
}
