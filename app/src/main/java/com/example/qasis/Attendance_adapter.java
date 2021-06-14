package com.example.qasis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qasis.models.ShowAttendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Attendance_adapter extends RecyclerView.Adapter<Attendance_adapter.AttendanceHolder> {

    private List<ShowAttendance> attendances;
    private Context context;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(ShowAttendance item);
    }

    public Attendance_adapter(List<ShowAttendance> attendances, Context context, OnItemClickListener listener) {
        this.attendances = attendances;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttendanceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_detail,parent , false);
        AttendanceHolder holder = new AttendanceHolder(itemview);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceHolder holder, int position) {
        ShowAttendance attendance = attendances.get(position);
        String dtStart = attendance.getDatetime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        SimpleDateFormat nuevo = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date date = format.parse(dtStart);
            String datetime = nuevo.format(date);
            holder.date.setText(datetime);
        } catch (ParseException e) {
            holder.date.setText(attendance.getDatetime());
            e.printStackTrace();
        }
        holder.id.setText(String.valueOf(attendance.getId()));
        if (attendance.getType() == 1){
            holder.type.setText("Entrada");
        }else{
            holder.type.setText("Salida");
        }
        holder.dni.setText(attendance.getUser().getDni());
        holder.latitude.setText(attendance.getLatitude());
        holder.longitude.setText(attendance.getLongitude());

        if(attendance.getType() == 1){
            holder.layout.setBackgroundResource(R.drawable.back_in);
        }else if(attendance.getType() == 2){
            holder.layout.setBackgroundResource(R.drawable.back_out);
        }

        if(attendance.getIsUpdated() != "false"){
            holder.date.setTextColor(ContextCompat.getColor(context, R.color.Red));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(attendance);
            }
        });

    }

    @Override
    public int getItemCount() {
        return attendances.size();
    }

    public void setAttendances(List<ShowAttendance> attendances) {
        this.attendances = attendances;
        notifyDataSetChanged();
    }

    static class AttendanceHolder extends RecyclerView.ViewHolder{

        TextView id,dni,date,type,latitude,longitude;
        LinearLayout layout;

        public AttendanceHolder(@NonNull View v){
            super(v);
            id = v.findViewById(R.id.id);
            dni = v.findViewById(R.id.user);
            date = v.findViewById(R.id.date);
            type = v.findViewById(R.id.type);
            latitude = v.findViewById(R.id.latitude);
            longitude = v.findViewById(R.id.longitude);
            layout = v.findViewById(R.id.pulsar);

        }
    }
}
