package com.example.qasis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.qasis.models.Attendance;
import com.example.qasis.models.Respuesta;
import com.example.qasis.models.Respuesta_General;
import com.example.qasis.models.Token;
import com.example.qasis.models.User;
import com.example.qasis.webservice.WebServiceClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Frag_Administrador extends Fragment  {

    private TextInputEditText dni, pass;
    private CheckBox recuerdame;
    private String dni_text, pass_text;
    private Boolean first_access;
    private Button inicio, huella;
    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private TextView forget;
    private String email_forgotten;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dni_text = dni.getText().toString();
                pass_text = pass.getText().toString();

                if(dni_text.isEmpty()){
                    Toast.makeText(getActivity(), "Introduce tu DNI para poder iniciar sesión", Toast.LENGTH_SHORT).show();
                }else if (dni_text.length() < 9){
                    Toast.makeText(getActivity(), "Introduce un DNI valido", Toast.LENGTH_SHORT).show();
                }else if(pass_text.isEmpty()){
                    Toast.makeText(getActivity(), "Introduce tu contraseña para poder iniciar sesión", Toast.LENGTH_SHORT).show();
                }else{
                    lanzarPeticion(dni_text, pass_text);
                }

            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Escribe tu correo electronico para recuperar la contraseña");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        email_forgotten = input.getText().toString();
                        if(validarEmail(email_forgotten)){
                            lanzarPeticion2(email_forgotten);
                        }else{
                            Toast.makeText(getActivity(), "Introduce un email que sea correcto", Toast.LENGTH_SHORT).show();
                        }
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

//        oscuro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                boolean value = true;
//                SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
//                SharedPreferences.Editor edit = preferences.edit();
//                edit.putBoolean("oscuro", value);
//
//            }
//        });

        executor = ContextCompat.getMainExecutor(getContext());
        biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getActivity(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();
                String dni = preferences.getString("dni_user_login", "");
                String pass = preferences.getString("pass_user_login", "");
                if(dni.isEmpty()){
                    Toast.makeText(getActivity(), "Inicia sesión por primera vez para usar la huella", Toast.LENGTH_SHORT).show();
                }else if (pass.isEmpty()){
                    Toast.makeText(getActivity(), "Inicia sesión por primera vez para usar la huella", Toast.LENGTH_SHORT).show();
                }else{
                    lanzarPeticion(dni, pass);
                    Toast.makeText(getActivity(), "Authentication succeed...!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getActivity(), "Authentication failed...!", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        huella.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });

    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_administrador, container, false);

        dni = view.findViewById(R.id.dni);
        pass = view.findViewById(R.id.pass);
        recuerdame = view.findViewById(R.id.recuerdame);
        inicio = view.findViewById(R.id.iniciar);
        huella = view.findViewById(R.id.huella);
        forget = view.findViewById(R.id.forget);

        //prefs = getSharedPreferences("datos", Context.MODE_PRIVATE);
        //editor = prefs.edit();

        SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        dni.setText(preferences.getString("dni_user_login", ""));
        pass.setText(preferences.getString("pass_user_login", ""));

        return view;
    }

    private void lanzarPeticion(String dni, String password){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);


        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("dni", dni);
        hashMap.put("password", password);

        Call<Respuesta_General> llamada = client.doLogin(hashMap);
        llamada.enqueue(new Callback<Respuesta_General>() {
            @Override
            public void onResponse(Call<Respuesta_General> call, Response<Respuesta_General> response) {
                Respuesta_General respuesta_general = response.body();
                if(respuesta_general != null){
                    if(response.isSuccessful()){
                        int code = respuesta_general.getResponse().getCode();
                        if(code == 1){
                            JsonObject data = respuesta_general.getResponse().getData();
                            Gson gson = new Gson();
                            Token token = gson.fromJson(data,Token.class);
                            int roleType = token.getUser().getRole();
                            String user_id = String.valueOf(token.getUser().getId());
                            SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();
                            if(roleType == 1){
                                Intent intent = new Intent(getContext(), Attendance_Admin_class.class);
                                edit.putString("token", String.valueOf(token.getToken()));
                                edit.putString("user_id", user_id);
                                if(recuerdame.isChecked()){
                                    edit.putString("dni_user_login",dni);
                                    edit.putString("pass_user_login",password);
                                    edit.commit();
                                    startActivity(intent);
                                }else{
                                    edit.putString("token",token.getToken());
                                    edit.putString("pass_user_login",password);
                                    edit.commit();
                                    startActivity(intent);
                                }
                            }else if (roleType == 2){
                                Intent intent = new Intent(getContext(), Attendance_class.class);
                                edit.putString("token", String.valueOf(token.getToken()));
                                edit.putString("user_id", user_id);
                                if(recuerdame.isChecked()){
                                    edit.putString("dni_user_login",dni);
                                    edit.putString("pass_user_login",password);
                                    edit.commit();
                                    startActivity(intent);
                                }else{
                                    edit.putString("token",token.getToken());
                                    edit.putString("pass_user_login",password);
                                    edit.commit();
                                    startActivity(intent);
                                }
                            }
                        }else{
                            Toast.makeText(getActivity(), "Sus credenciales son invalidas, prueba de nuevo", Toast.LENGTH_SHORT).show();
                        }

                    }
                }else{
                    Toast.makeText(getActivity(), "Sus credenciales son invalidas, prueba de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta_General> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void lanzarPeticion2(String email){
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);


        retrofit = new Retrofit.Builder()
                .baseUrl(WebServiceClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        WebServiceClient client = retrofit.create(WebServiceClient.class);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("email", email);

        Call<Respuesta> llamada = client.forgotPassword(hashMap);
        llamada.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                Respuesta respuesta = response.body();
                if(respuesta != null){
                    if(response.isSuccessful()){
                        int code = respuesta.getCode();
                        if(code == 1){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Recuperación de contraseña");
                            builder.setMessage("El email con la nueva contraseña ha sido mandada al correo electronico");
                            builder.setPositiveButton("Aceptar", null);

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage("El email no pertenece a ningún usuario");
                    builder.setPositiveButton("Aceptar", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
