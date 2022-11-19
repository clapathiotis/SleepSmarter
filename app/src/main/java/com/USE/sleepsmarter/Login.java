package com.USE.sleepsmarter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sleepsmarter-6f213-default-rtdb.firebaseio.com/");
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String signed_in = "signed_in" ;
    private boolean logged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText phone = findViewById(R.id.phoneEtlogin);
        final EditText password = findViewById(R.id.passwordEtlogin);
        final Button loginbtn = findViewById(R.id.loginBtn);
        final TextView noaccount = findViewById(R.id.noAccountTy);
        SharedPreferences shpref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        logged = shpref.getBoolean(signed_in, false);
        if (logged) {
            Log.d("logged", "val" + logged);
            startActivity(new Intent(Login.this, MainActivity.class));

        }


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String phonetxt = phone.getText().toString();
                final String passwordtxt = password.getText().toString();

                if (phonetxt.isEmpty() || passwordtxt.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your name and password,",
                            Toast.LENGTH_SHORT).show();
                }
                else {

                    dbref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(phonetxt)) {
                                final String getpassword = snapshot.child(phonetxt).child("Password").getValue(String.class);
                                if(getpassword.equals(passwordtxt)) {
                                    Toast.makeText(Login.this, "Successful Login", Toast.LENGTH_SHORT).show();
                                    SharedPreferences shpref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = shpref.edit();
                                    editor.putBoolean(signed_in, true).apply();
                                    boolean val = shpref.getBoolean(signed_in, true);
                                    Log.d("logged1", "Value " + val );
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                }
                                else {
                                    Toast.makeText(Login.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(Login.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        noaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Signup_java.class));
            }
        });

    }


}