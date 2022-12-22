package com.USE.sleepsmarter;

import static com.USE.sleepsmarter.Settings.Doctor;
import static com.USE.sleepsmarter.Settings.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
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
//    String[][] dummydata = {{"60", "65", "80", "75"}, {"10:00", "10:01", "10:02", "10:03"}};
//    String[] dummydata = {"40", "44", "32", "88"};
//    String[] timedummydata = {"10:00", "10:01", "10:02","10:03"};
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
//
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

        //Fetch users data
        ArrayList<Integer> x_axis = new ArrayList<>();
        ArrayList<Integer> y_axis = new ArrayList<>();
        String phone = sharedPreferences.getString(patient_num, "");
        rootRef.child("users").child(phone).child("HeartrateList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    // Fetching only y_axis for heartrate graph
                    y_axis.add(Integer.parseInt((String) childDataSnapshot.child("heartrate").getValue()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rootRef.child("users").child(phone).child("TimestampList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    // Fetching only x_axis timestamps for all graphs
                    x_axis.add(Integer.parseInt((String) childDataSnapshot.child("timestamp").getValue()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ArrayList<Integer> test_axis = new ArrayList<>();

        //TESTING FETCHING FROM ARDUINO -- DELETE LATER
//        rootRef.child("users").child("12").child("TimeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot childDataSnapshot : snapshot.getChildren()) {
//                    String key = childDataSnapshot.getKey();
//                    Log.d("test:::::::  ", key);
//                    test_axis.add( Integer.parseInt( (String) childDataSnapshot.getValue()));
////                    y_axis.add(Integer.parseInt((String) childDataSnapshot.child("heartrate").getValue()));
////                    Log.d("here ", "key: " + key + "test: " + test_axis   );
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        // DELETE ENDS HERE


        Handler handler = new Handler();
        int delay = 1000;

        TextView maxrate, lowrate;
        maxrate = findViewById(R.id.tvMaxToUpdate);
        lowrate = findViewById(R.id.tvLowToUpdate);

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!x_axis.isEmpty() && !y_axis.isEmpty())//checking if the data is loaded or not
                {
                    //Graph is being generated here
                   ArrayList<Entry> data = new ArrayList<>();

                   //Transform data into Entries so we can use MPAndroid for graphing
                   for (int j=0; j<y_axis.size(); j++) {
                       data.add(new Entry((float) x_axis.get(j), (float) y_axis.get(j)));
                   }

                    LineChart mpLineChart;
                    mpLineChart = findViewById(R.id.line_chart);
                    LineDataSet lineDataSetHeartrate = new LineDataSet(data, "Heartrate Data");
                    lineDataSetHeartrate.setLineWidth(5);
                    lineDataSetHeartrate.setCircleRadius(3);
                    lineDataSetHeartrate.setCircleHoleRadius(4);
                    lineDataSetHeartrate.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                    //use them to update text below graph and at patients tab
                    String max =  String.valueOf(lineDataSetHeartrate.getYMax());
                    String min = String.valueOf(lineDataSetHeartrate.getYMin());
                    maxrate.setText(max);
                    lowrate.setText(min);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(lineDataSetHeartrate);

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

        //add dummy data -- DELETE LATER
//        int i = 0;
//        while (i < dummydata.length) {
//            HeartrateData heartrateData = new HeartrateData(dummydata[i]);
//            arrayList.add(heartrateData);
//            i++;
//        }
//        rootRef.child("users").child(phone).child("HeartrateList").setValue(arrayList);
//        //Now for timestamps
//        i = 0;
//        while (i < timedummydata.length) {
//            TimeStamps timeStamps = new TimeStamps(timedummydata[i]);
//            timeList.add(timeStamps);
//            i++;
//        }
//        rootRef.child("users").child(phone).child("TimestampList").setValue(timeList);
//        // DELETE TILL HERE


    }


}