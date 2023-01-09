package com.USE.sleepsmarter;

import static com.USE.sleepsmarter.Login.signed_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Settings extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static String Doctor = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button removegpBtn = findViewById(R.id.updatedpBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stop Sharing Your Data With Your Personal Doctor");
        builder.setMessage("If you want to share your data again, then call your doctor");

        builder.setPositiveButton("Remove Doctor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/").child("users");
                HashMap hasmap = new HashMap();
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String phone = sharedPreferences.getString("phoneval", "");

                //get phone of user (unique) and update doctor
                dbref.child(phone).updateChildren(hasmap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        hasmap.put("Personal_Doctor", " ");
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    String phone = sharedPreferences.getString("phoneval", "");
                    //get phone of user (unique) and update doctor
                    dbref.child(phone).updateChildren(hasmap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(Settings.this, "Successfully removed from your Personal Doctor", Toast.LENGTH_SHORT).show();
                    }
                });
                    }
                });
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog ad = builder.create();

        removegpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.show();
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.settings);


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
                        if (Doctor.equals("me")) {
                            startActivity(new Intent(getApplicationContext(), Patients.class));
                            overridePendingTransition(0,0);
                            return true;
                        }
                        else {
                            Toast.makeText(Settings.this, "Only Doctors can access this", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    case R.id.settings:
                        return true;
                }
                return false;
            }
        });

        //Logout User
        Button logout = findViewById(R.id.Logoutbtn);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("You are about to logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Settings.this, "Logging out...", Toast.LENGTH_SHORT).show();
                                SharedPreferences shpref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = shpref.edit();
                                editor.putBoolean(signed_in, false).apply();

                                startActivity(new Intent(Settings.this, Login.class));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });

        //Remove doctor from being able to see patients data
//        Button removegpBtn = findViewById(R.id.removegpBtn);
//        removegpBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //remove doctor from DB
//                HashMap hasmap = new HashMap();
//                hasmap.put("Personal_Doctor", " ");
//                DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/").child("users");
//
//                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//                String phone = sharedPreferences.getString("phoneval", "");
//                //get phone of user (unique) and update doctor
//                dbref.child(phone).updateChildren(hasmap).addOnSuccessListener(new OnSuccessListener() {
//                    @Override
//                    public void onSuccess(Object o) {
//                        Toast.makeText(Settings.this, "Personal Doctor removed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
    }
}