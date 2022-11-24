package com.USE.sleepsmarter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.MyViewHolder> {

    Context context;

    ArrayList<Patient> list;

    public PatientAdapter(Context context, ArrayList<Patient> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.emp_list_item, parent, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Patient patient = list.get(position);
        holder.fullname.setText(patient.getFullName());
        holder.maxheartrate.setText(patient.getmaxRate());
        holder.lowheartrate.setText(patient.getlowRate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView fullname, maxheartrate, lowheartrate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.tvFullname);
            maxheartrate = itemView.findViewById(R.id.tvMaxHeartRate);
            lowheartrate = itemView.findViewById(R.id.tvMLowHeartRate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String patient = fullname.getText().toString().trim();
                    Intent intent = new Intent("patient_selected");
                    intent.putExtra("patient",patient);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });
        }
    }
}
