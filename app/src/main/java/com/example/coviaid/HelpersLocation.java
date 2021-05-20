package com.example.coviaid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.example.coviaid.databinding.ActivityHelpersLocationBinding;
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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class HelpersLocation extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    private ActivityHelpersLocationBinding binding;
    Button acceptRequest;
    Intent intent;

    public void acceptRequestMethod(View view) {

        ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Request");
        parseQuery.whereEqualTo("Username", intent.getStringExtra("username"));
//        parseQuery.whereDoesNotExist("helperName");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects) {
                            object.put("helperName", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?saddr=" + intent.getDoubleExtra("helperLatitude", 0) + "," + intent.getDoubleExtra("helperLongitude", 0) + "&daddr=" + intent.getDoubleExtra("requestLatitude", 0) + "," + intent.getDoubleExtra("requestLongitude", 0)));
                                        startActivity(directionsIntent);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpersLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        acceptRequest = findViewById(R.id.acceptRequest);
        intent=getIntent();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        constraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LatLng helperLocation = new LatLng(intent.getDoubleExtra("helperLatitude",0),intent.getDoubleExtra("helperLongitude",0));
                LatLng requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude",0),intent.getDoubleExtra("requestLongitude",0));

                ArrayList<Marker> markers=new ArrayList<Marker>();
                    markers.add(mMap.addMarker(new MarkerOptions().position(helperLocation).title("Helper's Location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Patient's Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))));
                LatLngBounds.Builder builder=new LatLngBounds.Builder();

                for(Marker marker: markers){
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds=builder.build();
                int padding=30;
                CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,padding);
                mMap.animateCamera(cu);

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}