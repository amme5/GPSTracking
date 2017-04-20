package com.example.bartek.gpstracking2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class MapPage extends android.app.Activity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    String passedId;
    DatabaseReference myRef;
    FirebaseDatabase database;
    TextView tv;
    //UserLocation location;
    String latitude, longnitude, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_map);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        passedId = getIntent().getStringExtra("Passed Id");
        tv = (TextView)findViewById(R.id.textView);
        tv.setText(passedId);


        initmap();
        //goToLocation(55,55,10);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    UserLocation uInfo = new UserLocation();
                    uInfo.setLatitude(ds.child(passedId).getValue(UserLocation.class).getLatitude()); //set the name
                    uInfo.setLongitude(ds.child(passedId).getValue(UserLocation.class).getLongitude()); //set the email

                    toster(uInfo.getLatitude());
                    goToLocation(uInfo.getLatitude(),uInfo.getLongitude(),16);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initmap() {

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private void goToLocation(double lat, double lng, float zoom) {
        LatLng ll= new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mGoogleMap.animateCamera(update);

    }
    private void toster(double lat){
        Toast.makeText(this, "lat:"+lat, Toast.LENGTH_LONG).show();
    }
}
