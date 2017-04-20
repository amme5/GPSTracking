package com.example.bartek.gpstracking;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class map extends Activity implements OnMapReadyCallback {

    TextView mValueView;
    String passedId;
    GoogleMap mGoogleMap;
    DatabaseReference databaseLocation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        databaseLocation = FirebaseDatabase.getInstance().getReference("locations");
        mValueView = (TextView) findViewById(R.id.textView4);

        passedId = getIntent().getStringExtra("Passed Id");
        TextView tv = (TextView)findViewById(R.id.LocatingFor);
        tv.setText(passedId);
        initMap();


        databaseLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot locationSnapshot: dataSnapshot.getChildren()){
                    Location1 location = locationSnapshot.getValue(Location1.class);
                    if(location.mygetLocationID().equals(passedId))
                    {
                        mValueView.setText(location.mygetLatitude());
                        goToLocation(location.mygetLatitude(), location.mygetLongitude(), 16);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

    }

    private void goToLocation(String lat, String lng, float zoom) {
        LatLng ll= new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mGoogleMap.moveCamera(update);
    }

}



