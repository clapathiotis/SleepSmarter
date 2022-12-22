package com.USE.sleepsmarter;

import static com.USE.sleepsmarter.Login.phoneval;
import static com.USE.sleepsmarter.Login.signed_in;
import static com.USE.sleepsmarter.MySleep.patient_num;
import static com.USE.sleepsmarter.Settings.Doctor;
import static com.USE.sleepsmarter.Settings.SHARED_PREFS;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Patients extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    PatientAdapter adapter;
    ArrayList<Patient> list;
    public static String patient_selected = "patient_selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.patients);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mysleep:
                        startActivity(new Intent(getApplicationContext(), MySleep.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext(), About.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.patients:
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.recycleviewpatients);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new PatientAdapter(this, list);
        recyclerView.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //Add patients to the recyclerview list to be displayed
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    list.add(patient);
                    adapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Selection observer from our intent
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("patient_selected"));

    }

    //Observe selection of patient
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String patient = intent.getStringExtra("patient");
            Toast.makeText(Patients.this,"Patient Selected: "+patient ,Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(patient_selected, patient).apply();DatabaseReference rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/");

            //Finding the SleepSmarter User from the database that was selected.
            rootRef.child("users").orderByChild("FullName").equalTo(patient).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data: dataSnapshot.getChildren()){

                        //Successfully getting patients phone (Unique ID)
                        //to help us fetch the graph data
                        String phone = data.child("Phone").getValue(String.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(patient_num, phone).apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Move to patients data graph activity
            Intent i = new Intent();
            i.setClassName("com.USE.sleepsmarter", "com.USE.sleepsmarter.MySleep");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    };

}