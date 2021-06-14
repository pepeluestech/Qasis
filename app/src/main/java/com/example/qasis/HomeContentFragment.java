package com.example.qasis;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import static android.app.Activity.RESULT_OK;

public class HomeContentFragment extends Fragment {


  private static final String TEXT = "text";
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
  private Button settings;

  public static HomeContentFragment newInstance(String text) {
    HomeContentFragment frag = new HomeContentFragment();

    Bundle args = new Bundle();
    args.putString(TEXT, text);
    frag.setArguments(args);

    return frag;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
          Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.activity_attendances, container, false);

    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    sing_out = (FloatingActionButton) getView().findViewById(R.id.sing_out2);
    settings = (Button) getView().findViewById(R.id.settings);
    SharedPreferences preferences = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    user_id = preferences.getString("user_id", "");
    token = preferences.getString("token", "");
    setupview();
    lanzarPeticion("Bearer "+token,user_id);

      }

  public void onBackPressed() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setTitle("Cerrar Sesión");
    builder.setMessage("¿Desea cerrar sesión de Qasis?");
    builder.setPositiveButton("Cerrar sesion", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        getActivity().getFragmentManager().popBackStack();
      }
    });
    builder.setNegativeButton("Cancelar", null);
    builder.show();
  }

  private void setupview (){
    recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
    attendanceList = new ArrayList<>();
    adapter = new Attendance_adapter(attendanceList, getContext(), new Attendance_adapter.OnItemClickListener() {
      @Override
      public void onItemClick(ShowAttendance attendance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("¿Que deseas hacer con la asistencia?");
        builder.setMessage("¿Deseas cambiar la algún ajuste de la asistencia o deseas ubicar la asistencia?");
        builder.setPositiveButton("Ajustar asistencia", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            int id = attendance.getId();
            Bundle extras = new Bundle();
            extras.putInt("id_attendance", id);
            Intent intent = new Intent(getActivity(), Update_Attendance_class_v2.class);
            intent.putExtras(extras);
            getActivity().startActivityForResult(intent, code_activity);
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
            getActivity().startActivity(intent);
          }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
      }
    });
    layoutManager = new GridLayoutManager(getContext(), 1);
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
          }else{
            Toast.makeText(getActivity(), "Ha habido un error", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "La asistencia no pudó ser borrada", Toast.LENGTH_SHORT).show();
          }
        }
      }

      @Override
      public void onFailure(Call<Respuesta> call, Throwable t) {

      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == code_activity && resultCode == RESULT_OK){
      lanzarPeticion("Bearer "+token,user_id);
    }
  }

}


