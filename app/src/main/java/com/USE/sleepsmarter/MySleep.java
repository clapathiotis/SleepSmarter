package com.USE.sleepsmarter;

import static com.USE.sleepsmarter.Settings.Doctor;
import static com.USE.sleepsmarter.Settings.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MySleep extends AppCompatActivity {
    public String phone;
    public static String patient_num = "patient_num";

    ArrayList<HeartrateData> arrayList;
    ArrayList<TimeStamps> timeList;


    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/").child("users");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String phone = sharedPreferences.getString("phoneval", "");
                Doctor = snapshot.child(phone).child("Personal_Doctor").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        arrayList = new ArrayList<HeartrateData>();
        timeList = new ArrayList<TimeStamps>();

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.mysleep);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mysleep:
                        return true;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext(), About.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.patients:
                        if (Doctor.equals("me")) {
                            startActivity(new Intent(getApplicationContext(), Patients.class));
                            overridePendingTransition(0, 0);
                            return true;
                        } else {
                            Toast.makeText(getBaseContext(), "Only Doctors can access this", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String name = sharedPreferences.getString("patient_selected", "");

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/");

        //Finding the SleepSmarter User from the database that was selected.
        rootRef.child("users").orderByChild("FullName").equalTo(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    //Successfully getting patients phone (Unique ID)
                    //to help us fetch the graph data
                    phone = data.child("Phone").getValue(String.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(patient_num, phone).apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Fetch users data -- DELETE ONCE WE TEST THAT ITS OK FETCHING FRM ARDUINO
        // ####### DELETE FROM HERE ########
        ArrayList<Integer> x_axis = new ArrayList<>();
        ArrayList<Integer> y_axis = new ArrayList<>();
        String phone = sharedPreferences.getString(patient_num, ""); // KEEP THIS THOUGH

        ArrayList<Integer> movement_axis = new ArrayList<>();
        ArrayList<Integer> heartrate_axis = new ArrayList<>();
        ArrayList<Integer> timestamp_axis = new ArrayList<>();


        //TESTING FETCHING FROM ARDUINO -- DELETE LATER

        // HEARTRATE

        rootRef.child("users").child(phone).child("HeartRate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    heartrate_axis.add(Integer.parseInt((String) childDataSnapshot.getValue()));
                    Log.d("here ", "heartrate: " + heartrate_axis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // MOVEMENT
        rootRef.child("users").child(phone).child("AccelerationX").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    movement_axis.add(Integer.parseInt((String) childDataSnapshot.getValue()));
                    Log.d("here ", "accelerationX: " + movement_axis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // TIMESTAMPS
        rootRef.child("users").child(phone).child("TimeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    timestamp_axis.add(Integer.parseInt((String) childDataSnapshot.getValue()));
                    Log.d("here ", "accelerationX: " + timestamp_axis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Handler handler = new Handler();
        int delay = 1000;

        TextView maxrate, lowrate;
        maxrate = findViewById(R.id.tvMaxToUpdate);
        lowrate = findViewById(R.id.tvLowToUpdate);

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!timestamp_axis.isEmpty() && !heartrate_axis.isEmpty())//checking if the data is loaded or not
                {
                    //Graph is being generated here
                   ArrayList<Entry> data = new ArrayList<>();
                   ArrayList<Entry> accelerationData = new ArrayList<>();

                   //Transform data into Entries so we can use MPAndroid for graphing
                   for (int j=0; j<heartrate_axis.size(); j++) {
                       data.add(new Entry((float) timestamp_axis.get(j), (float) heartrate_axis.get(j)));
                       accelerationData.add(new Entry((float) timestamp_axis.get(j), (float) movement_axis.get(j)));
                   }

                    LineChart mpLineChart;
                    mpLineChart = findViewById(R.id.line_chart);

                    //Heartrate Dataset
                    LineDataSet lineDataSetHeartrate = new LineDataSet(data, "Heartrate");
                    lineDataSetHeartrate.setLineWidth(5);
                    lineDataSetHeartrate.setCircleRadius(3);
                    lineDataSetHeartrate.setCircleHoleRadius(4);
                    lineDataSetHeartrate.setValueTextSize(10);
                    lineDataSetHeartrate.setCircleColor(Color.rgb(127, 0, 225));
                    lineDataSetHeartrate.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                    lineDataSetHeartrate.setAxisDependency(YAxis.AxisDependency.LEFT);

                    //Acceleration Dataset
                    LineDataSet lineDataSetAcceleration = new LineDataSet(accelerationData, "Movement");
                    lineDataSetAcceleration.setLineWidth(4);
                    lineDataSetAcceleration.setCircleRadius(3);
                    lineDataSetAcceleration.setCircleHoleRadius(4);
                    lineDataSetAcceleration.setValueTextSize(10);
                    lineDataSetAcceleration.setColor(Color.rgb(127, 0, 225));
                    lineDataSetAcceleration.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    lineDataSetAcceleration.enableDashedLine(10, 10 , 0);
                    lineDataSetAcceleration.setAxisDependency(YAxis.AxisDependency.RIGHT);

                    mpLineChart.getDescription().setText("Movement and Heartrate readings at time of sleep (hhmm)");
                    //use them to update text below graph and at patients tab
                    String max =  String.valueOf(lineDataSetHeartrate.getYMax());
                    String min = String.valueOf(lineDataSetHeartrate.getYMin());
                    maxrate.setText(max);
                    lowrate.setText(min);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(lineDataSetHeartrate);
                    dataSets.add(lineDataSetAcceleration);

                    LineData lineData = new LineData(dataSets);
                    mpLineChart.setData(lineData);
                    mpLineChart.invalidate();

                    //Update min/max values, send them to firebase
                    rootRef.child("users").child(phone).child("lowrate").setValue(min);
                    rootRef.child("users").child(phone).child("maxrate").setValue(max);



                }
                else
                    handler.postDelayed(this, delay);
            }
        }, delay);

    }


}