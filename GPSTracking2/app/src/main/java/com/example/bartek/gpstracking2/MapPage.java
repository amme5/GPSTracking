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

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapPage extends android.app.Activity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    DatabaseReference myRef;
    FirebaseDatabase database;

    private FirebaseUser user;
    TextView textstatus,emerg,boundaryText,emergText,boundaryText2;
    String online = "Online";
    String offline = "Offline";
    boolean first=true;
    double radius=0;
    String boundary="NotActive";
    double x1,y1;
    LatLng center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_map);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this,LoginPage.class));
        }

        user = firebaseAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Locations/"+user.getUid());
        myRef.keepSynced(true);

        textstatus = (TextView)findViewById(R.id.textOffline);
        emerg = (TextView)findViewById(R.id.textView7);
        emergText = (TextView)findViewById(R.id.textView6);
        boundaryText = (TextView)findViewById(R.id.textView10);
        boundaryText2 = (TextView)findViewById(R.id.textView11);

        textVisibility(false);

        myRef.child("listener").setValue(online);
        myRef.child("listener").onDisconnect().setValue(offline);

        initmap();


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child("status").getValue(String.class).equals("Online"))
                    {
                        textVisibility(true);
                        textstatus.setText(online);
                        textstatus.setTextColor(Color.parseColor("#009933"));

                        if(dataSnapshot.child("emergency").getValue(String.class).equals("true")){
                            emerg.setText("In danger");
                            emerg.setTextColor(Color.parseColor("#FF0000"));
                        }
                        else{
                            emerg.setText("Ok");
                            emerg.setTextColor(Color.parseColor("#009933"));
                        }

                        if(!dataSnapshot.child("boundary").getValue(String.class).equals("NotActive")){

                            x1=dataSnapshot.child("BndLat").getValue(Double.class);
                            y1=dataSnapshot.child("BndLng").getValue(Double.class);
                            center=new LatLng(x1,y1);

                            if(dataSnapshot.child("boundary").getValue(String.class).equals("Inside")){
                                boundaryText.setText("Inside");
                                boundaryText.setTextColor(Color.parseColor("#009933"));
                            }else{
                                boundaryText.setText("Outside");
                                boundaryText.setTextColor(Color.parseColor("#FF0000"));
                            }

                            goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
                            drawCircle(center,dataSnapshot.child("radius").getValue(Double.class));

                        }else{
                            boundaryText.setText("Not active");
                            boundaryText.setTextColor(Color.GRAY);
                            goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
                        }

                    }
                    else{
                        textVisibility(false);
                        textstatus.setText(offline);
                        first=true;
                        emerg.setText("");
                        textstatus.setTextColor(Color.parseColor("#FF0000"));
                        mGoogleMap.clear();
                        myRef.child("emergency").setValue("false");
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
        MarkerOptions options = new MarkerOptions().title(user.getUid()).position(ll);
        mGoogleMap.addMarker(options);
       // if(!boundary.equals("NotActive")){
      //      drawCircle();
       // }

        if(first){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
            mGoogleMap.animateCamera(update);
            first=false;
        }else{
            CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
            mGoogleMap.animateCamera(update);
        }

    }

    private Circle drawCircle(LatLng center1, double rad){
        CircleOptions options = new CircleOptions().center(center1).radius(rad*1000).fillColor(0x33FF0000).strokeColor(Color.RED).strokeWidth(3);
        return mGoogleMap.addCircle(options);
    }

    public void textVisibility(boolean var){
        if(var){
            emerg.setVisibility(View.VISIBLE);
            emergText.setVisibility(View.VISIBLE);
            boundaryText.setVisibility(View.VISIBLE);
            boundaryText2.setVisibility(View.VISIBLE);
        }else
        {
            emerg.setVisibility(View.INVISIBLE);
            emergText.setVisibility(View.INVISIBLE);
            boundaryText.setVisibility(View.INVISIBLE);
            boundaryText2.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        myRef.child("listener").setValue("Offline");
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        myRef.child("listener").setValue("Online");
    }
    @Override
    protected void onStop() {
        super.onStop();
        //myRef.child("listener").setValue("Offline");
    }
    @Override
    protected void onResume() {
        super.onResume();
        myRef.child("listener").setValue("Online");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child("listener").setValue("Offline");
    }



}
