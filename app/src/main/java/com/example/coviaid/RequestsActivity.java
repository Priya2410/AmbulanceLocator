package com.example.coviaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity{

    ListView requestListView;
    ArrayList<String> requests= new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    LocationListener locationListener;
    LocationManager locationManager;

    ArrayList<Double> requests_latitude=new ArrayList<Double>();
    ArrayList<Double> requests_longitudes=new ArrayList<Double>();
    ArrayList<String>  userNames=new ArrayList<String>();


    public void updateListView(Location location) {
        if (location != null) {
//            requests.clear();
            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Request");
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            parseQuery.whereNear("Location", parseGeoPoint);
            parseQuery.setLimit(10);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null)
                    {
                        requests.clear();
                        requests_longitudes.clear();
                        requests_latitude.clear();
                        if(objects.size()>0)
                        {
                            for(ParseObject object:objects)
                            {
                                ParseGeoPoint requestLocation=new ParseGeoPoint((ParseGeoPoint) object.get("Location"));
                                if(requestLocation!=null) {
                                    double distanceinKil = parseGeoPoint.distanceInKilometersTo(requestLocation);
                                    double distancerounded = (double) Math.round(distanceinKil * 10) / 10;
                                    requests.add(distancerounded + "     KILOMETERS");
                                    requests_latitude.add(requestLocation.getLatitude());
                                    requests_longitudes.add(requestLocation.getLongitude());
                                    userNames.add(object.getString("Username"));
                                }
                            }
                        }
                        else {
                            requests.add("No active requests nearby");
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateListView(lastKnownLocation);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        requestListView= findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, requests);
        requestListView.setAdapter(arrayAdapter);
//        requests.clear();
//        requests.add("Getting nearby requests");

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (ContextCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    Log.i("info",position+"clicked");
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (requests_latitude.size() > position && requests_longitudes.size() > position && lastKnownLocation != null && userNames.size()>position) {

                        Intent intent=new Intent(getApplicationContext(),HelpersLocation.class);
                        intent.putExtra("requestLatitude",requests_latitude.get(position));
                        intent.putExtra("requestLongitude",requests_longitudes.get(position));
                        intent.putExtra("helperLatitude",lastKnownLocation.getLatitude());
                        intent.putExtra("helperLongitude",lastKnownLocation.getLongitude());
                        intent.putExtra("username",userNames.get(position));
                        startActivity(intent);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
                ParseUser.getCurrentUser().put("helperLocation",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    updateListView(lastKnownLocation);
                }
            }
        }
    }
}