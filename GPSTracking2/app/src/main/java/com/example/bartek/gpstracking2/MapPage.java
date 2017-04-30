package com.example.bartek.gpstracking2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
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
    TextView tv,textstatus,emerg;
    String online = "Online";
    String offline = "Offline";
    boolean first=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_map);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        passedId = getIntent().getStringExtra("Passed Id");
        tv = (TextView)findViewById(R.id.text1);
        textstatus = (TextView)findViewById(R.id.textOffline);
        tv.setText("User "+passedId+":");
        emerg = (TextView)findViewById(R.id.textView7);

        myRef.child("Locations").child(passedId).child("listener").setValue("Online");

        initmap();


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    UserLocation uInfo = new UserLocation();
                    uInfo.setLatitude(ds.child(passedId).getValue(UserLocation.class).getLatitude());
                    uInfo.setLongitude(ds.child(passedId).getValue(UserLocation.class).getLongitude());

                    if(ds.child(passedId).child("status").getValue(String.class).equals("Online"))
                    {
                        textstatus.setText(online);
                        textstatus.setTextColor(Color.parseColor("#009933"));
                        goToLocation(uInfo.getLatitude(),uInfo.getLongitude(),16);
                        if(ds.child(passedId).child("emergency").getValue(String.class).equals("true")){
                            emerg.setText("In danger");
                            emerg.setTextColor(Color.parseColor("#FF0000"));
                        }
                        else{
                            emerg.setText("Ok");
                            emerg.setTextColor(Color.parseColor("#009933"));
                        }
                    }
                    else{
                        textstatus.setText(offline);
                        emerg.setText("");
                        textstatus.setTextColor(Color.parseColor("#FF0000"));
                        mGoogleMap.clear();
                        myRef.child("Locations").child(passedId).child("emergency").setValue("false");
                    }

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
        mGoogleMap.clear();
        LatLng ll= new LatLng(lat,lng);
        MarkerOptions options = new MarkerOptions().title(passedId).position(ll);
        mGoogleMap.addMarker(options);

        if(first){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
            mGoogleMap.animateCamera(update);
            first=false;
        }else{
            CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
            mGoogleMap.animateCamera(update);
        }

    }

    public void onStartClick(View x){
        myRef.child("Locations").child(passedId).child("listener").setValue("Offline");
        Intent i = new Intent(MapPage.this, LoginPage.class);
        startActivity(i);
    }
    @Override
    public void onBackPressed(){
        myRef.child("Locations").child(passedId).child("listener").setValue("Offline");
        Intent i = new Intent(MapPage.this, LoginPage.class);
        startActivity(i);
    }
    @Override
    protected void onStop() {
        super.onStop();
        myRef.child("Locations").child(passedId).child("listener").setValue("Offline");
    }
    @Override
    protected void onResume() {
        super.onResume();
        myRef.child("Locations").child(passedId).child("listener").setValue("Online");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child("Locations").child(passedId).child("listener").setValue("Offline");
    }



}
