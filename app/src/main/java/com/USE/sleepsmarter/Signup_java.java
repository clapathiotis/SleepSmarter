package com.USE.sleepsmarter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup_java extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String phoneval = "phoneval";

    DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_java);

        final EditText PD = findViewById(R.id.personalDoctorEt);
        final EditText name = findViewById(R.id.fullnameEt);
        final EditText password = findViewById(R.id.passwordEtsignup);
        final EditText phone = findViewById(R.id.phoneEtsignup);
        final Button registerbtn = findViewById(R.id.signupBtn);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nametxt = name.getText().toString();
                final String PDtxt = PD.getText().toString();
                final String passwordtxt = password.getText().toString();
                final String phonetxt = phone.getText().toString();

                //check if cells are filled
                if(nametxt.isEmpty() || passwordtxt.isEmpty()) {
                    Toast.makeText(Signup_java.this, "Please fill the required cells",
                            Toast.LENGTH_SHORT).show();
                }

                else{
                    SharedPreferences shpref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = shpref.edit();
                    editor.putString(phoneval, phonetxt).apply();
                    dbref.child("users").child(phonetxt).child("FullName").setValue(nametxt);
                    dbref.child("users").child(phonetxt).child("Personal Doctor").setValue(PDtxt);
                    dbref.child("users").child(phonetxt).child("Password").setValue(passwordtxt);
                    dbref.child("users").child(phonetxt).child("Phone").setValue(phonetxt);

                    Toast.makeText(Signup_java.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signup_java.this, MySleep.class));
                }
            }


        });
    }
}