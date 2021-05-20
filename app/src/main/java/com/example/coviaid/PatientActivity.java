package com.example.coviaid;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PatientActivity extends FragmentActivity implements OnMapReadyCallback {

    //Setting up to get user's location
    private GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;
    Button button;
    boolean request_active=false;
    Handler handler=new Handler();
    TextView textView;
    Boolean driver_active=false;

    public void checkForUpdates(){
        ParseQuery<ParseObject> parseQuery=new ParseQuery<ParseObject>("Request");
//        Log.i("info",ParseUser.getCurrentUser().getUsername());
        parseQuery.whereEqualTo("Username",ParseUser.getCurrentUser().getUsername());
        parseQuery.whereExists("helperName");

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if(e==null && objects.size()>0)
                {
                     driver_active=true;
                     ParseQuery<ParseUser> query=ParseUser.getQuery();
                     query.whereEqualTo("username",objects.get(0).getString("helperName"));

                     query.findInBackground(new FindCallback<ParseUser>() {
                         @Override
                         public void done(List<ParseUser> objects, ParseException e) {

                             if(e==null && objects.size()>0)
                             {
                                 Log.i("info","I have entered-new");
                                ParseGeoPoint driverLocation=objects.get(0).getParseGeoPoint("helperLocation");
                                 if (ContextCompat.checkSelfPermission(PatientActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                     Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                            ParseGeoPoint userLocation=new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                                     assert driverLocation != null;
                                     double distanceinKil = driverLocation.distanceInKilometersTo(userLocation);
                                            double distancerounded = (double) Math.round(distanceinKil * 10) / 10;
                                            textView.setText("Your helper is "+ distancerounded +" km away");
                                             LatLng helperLocationLatLng = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
                                             LatLng requestLocationLatLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());

                                             ArrayList<Marker> markers=new ArrayList<Marker>();
                                             markers.add(mMap.addMarker(new MarkerOptions().position(helperLocationLatLng).title("Helper's Location")));
                                             markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title("Patient's Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))));
                                             LatLngBounds.Builder builder=new LatLngBounds.Builder();

                                             for(Marker marker: markers){
                                                 builder.include(marker.getPosition());
                                             }
                                             LatLngBounds bounds=builder.build();
                                             int padding=30;
                                             CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,padding);
                                             mMap.animateCamera(cu);
                                 }
                             }
                         }
                     });
                     button.setVisibility(View.INVISIBLE);
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                    }
                },2000);
            }
        });

    }


//    public void checkForUpdates() {
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
//        query.whereEqualTo("Username", ParseUser.getCurrentUser().getUsername());
//        query.whereExists("helperName");
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> objects, ParseException e) {
//                if (e == null && objects.size() > 0) {
//                    driver_active = true;
//                    ParseQuery<ParseUser> query = ParseUser.getQuery();
//                    query.whereEqualTo("Username", objects.get(0).getString("helperName"));
//                    query.findInBackground(new FindCallback<ParseUser>() {
//                        @SuppressLint("SetTextI18n")
//                        @Override
//                        public void done(List<ParseUser> objects, ParseException e) {
//                            if (e == null && objects.size() > 0) {
//                                ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");
//                                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(PatientActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                    if (lastKnownLocation != null) {
//                                        ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                                        assert driverLocation != null;
//                                        double distanceInKm = driverLocation.distanceInKilometersTo(userLocation);
//                                        if (distanceInKm < 0.01) {
//
//                                            textView.setText("Your helper is here!");
//                                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
//                                            query.whereEqualTo("Username", ParseUser.getCurrentUser().getUsername());
//                                            query.findInBackground(new FindCallback<ParseObject>() {
//                                                @Override
//                                                public void done(List<ParseObject> objects, ParseException e) {
//                                                    if (e == null) {
//                                                        for (ParseObject object : objects) {
//                                                            object.deleteInBackground();
//                                                        }
//                                                    }
//                                                }
//                                            });
//
//                                            handler.postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    textView.setText("");
//                                                    button.setVisibility(View.VISIBLE);
//                                                    button.setText("Call An Uber");
//                                                    request_active = false;
//                                                    driver_active = false;
//
//                                                }
//                                            }, 5000);
//
//                                        } else {
//
//                                            double distanceOneDP = (double) Math.round(distanceInKm * 10) / 10;
//                                            textView.setText("Your driver is " + distanceOneDP + " KILOMETERS");
//                                            LatLng driverLocationLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
//                                            LatLng requestLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
//
//                                            ArrayList<Marker> markers = new ArrayList<>();
//
//                                            mMap.clear();
//
//                                            markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLng).title("Driver Location")));
//                                            markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
//
//                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                                            for (Marker marker : markers) {
//                                                builder.include(marker.getPosition());
//                                            }
//                                            LatLngBounds bounds = builder.build();
//
//
//                                            int padding = 60; // offset from edges of the map in pixels
//                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//
//                                            mMap.animateCamera(cu);
//
//
//                                            button.setVisibility(View.INVISIBLE);
//
//                                            handler.postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
//
//                                                    checkForUpdates();
//
//                                                }
//                                            }, 2000);
//
//                                        }
//
//                                    }
//
//                                }
//
//                            }
//
//                        }
//                    });
////
//
//
//
//                }
//
//
//
//            }
//        });
//
//
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(lastKnownLocation!=null){
                        updateMap(lastKnownLocation);
                    }
                }
            }
        }
    }

    public void logOut(View view){
        ParseUser.logOut();
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }


    public void callAmb(View view) {
        Log.i("info", "ambulance was called");
        if (request_active) {
            ParseQuery<ParseObject> parseQuery=new ParseQuery<ParseObject>("Request");
            //Condition for the query
            parseQuery.whereEqualTo("Username",ParseUser.getCurrentUser().getUsername());

            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            for(ParseObject object:objects){
                                object.deleteInBackground();
                            }
                            button.setText("Call Ambulance");
                            request_active=false;
                        }
                    }
                }
            });
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    ParseObject request = new ParseObject("Request");
                    request.put("Username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    request.put("Location", parseGeoPoint);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                button.setText("Cancel Ambulance");
                                request_active=true;
                                checkForUpdates();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "Could not find location. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void updateMap(Location location) {
        //To get user's latitude and longitude
        if(!driver_active) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            // To zoom to the location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            // When clicked this will be shown
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Patient's Location"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        textView=findViewById(R.id.alertUser);

        button= findViewById(R.id.button2);
        ParseQuery<ParseObject> parseQuery=new ParseQuery<ParseObject>("Request");
        //Condition for the query
        parseQuery.whereEqualTo("Username",ParseUser.getCurrentUser().getUsername());

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        button.setText("Cancel Ambulance");
                        request_active=true;
                       checkForUpdates();
                    }
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
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
                    updateMap(lastKnownLocation);
                }
            }
        }
    }
}