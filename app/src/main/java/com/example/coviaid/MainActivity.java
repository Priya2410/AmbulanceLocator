package com.example.coviaid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MainActivity extends AppCompatActivity {

        //Method to redirect to the map
        public void redirect() {
            if (ParseUser.getCurrentUser().get("TypeOfUser").equals("patient")) {
                Intent intent = new Intent(getApplicationContext(), PatientActivity.class);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(getApplicationContext(), RequestsActivity.class);
                startActivity(intent);
            }
        }


        // To check if the user - patient or helper
        public void getStarted(View view) {
            // To know if the user is a patient or is a helper
            @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switch_val = findViewById(R.id.switch1);
            Log.i("info", String.valueOf((switch_val.isChecked())));
            String val = "patient";
            if (switch_val.isChecked()) {
                val = "helper";
            }
            // Just logging the value to know the user's state
            ParseUser.getCurrentUser().put("TypeOfUser", val);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    redirect();
                }
            });
            Log.i("Patient or Driver:", (String) ParseUser.getCurrentUser().get("TypeOfUser"));
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            ParseUser.logOut();
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            Log.i("Info", "Anonymous login successful");
                        } else {
                            Log.i("Info", "Anonymous login failed");
                        }
                    }
                });

            } else {
                if (ParseUser.getCurrentUser().get("TypeOfUser") != null) {
                    Log.i("Info", "Redirecting as " + ParseUser.getCurrentUser().get("TypeOfUser"));
                    redirect();
                }
            }
            ParseAnalytics.trackAppOpenedInBackground(getIntent());
        }

    }