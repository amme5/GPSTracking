package com.example.bartek.gpstracking2;

import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//gps
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.location.Location;
import android.location.LocationManager;
import android.widget.ToggleButton;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class send_loc extends android.app.Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    TextView LatitudeText;
    TextView LongitudeText;
    TextView textView4,textView3;
    ToggleButton emergencyButton;
    String danger = "Other user informed, to cancel press button";
    String safe = "In case of emergency press button";
    String danger2 = "Listener if offline, you are on your own";
    String online = "Online";
    String offline = "Offline";
    boolean listenerStatus,reset=true;


    DatabaseReference myRef,myRefStatus;
    FirebaseDatabase database;
    String id;
    //GPS-------
    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LocationManager locationManager;
    //----------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.send_loc);

        LatitudeText = (TextView) findViewById(R.id.viewlatitude);
        LongitudeText = (TextView) findViewById(R.id.viewlongitude);
        emergencyButton = (ToggleButton) findViewById(R.id.emergencyButton);

        id=getIntent().getStringExtra("Passed Id");
        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations");
        myRefStatus=database.getReference();
        textView4 = (TextView)findViewById(R.id.textView4);
        textView3 = (TextView)findViewById(R.id.textView3);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        checkLocation();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        myRefStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    if(ds.child(id).child("listener").getValue(String.class).equals("Online"))
                    {
                        listenerStatus=true;
                        textView3.setText(online);
                        textView3.setTextColor(Color.parseColor("#009933"));

                        if(reset){
                            resetButton();
                            reset=false;
                        }
                    }
                    else{
                        listenerStatus=false;
                        textView3.setText(offline);
                        textView3.setTextColor(Color.parseColor("#FF0000"));

                        reset=true;
                        listenerOfflineButton();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void resetButton(){
        emergencyButton.setEnabled(true);
        emergencyButton.setChecked(false);
        emergencyButton.setBackgroundColor(Color.parseColor("#FF0000"));
        textView4.setText(safe);
        myRef.child(id).child("emergency").setValue("false");
    }
    public void listenerOfflineButton(){
        emergencyButton.setEnabled(false);
        emergencyButton.setChecked(false);
        emergencyButton.setBackgroundColor(Color.parseColor("#FF808080"));
        textView4.setText(danger2);
        myRef.child(id).child("emergency").setValue("false");
    }

    public void emergency(View view){

            if(emergencyButton.isChecked()){
                textView4.setText(danger);
                emergencyButton.setBackgroundColor(Color.parseColor("#009933"));
                myRef.child(id).child("emergency").setValue("true");
            }
            else{
                textView4.setText(safe);
                emergencyButton.setBackgroundColor(Color.parseColor("#FF0000"));
                myRef.child(id).child("emergency").setValue("false");
            }

    }


    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    public void onStartClick(View x){
        myRef.child(id).child("status").setValue("Offline");
        Intent i = new Intent(send_loc.this, LoginPage.class);
        startActivity(i);
    }
    @Override
    public void onBackPressed(){
        myRef.child(id).child("status").setValue("Offline");
        Intent i = new Intent(send_loc.this, LoginPage.class);
        startActivity(i);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        myRef.child(id).child("status").setValue("Offline");
    }
    @Override
    protected void onResume() {
        super.onResume();
        myRef.child(id).child("status").setValue("Online");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child(id).child("status").setValue("Offline");
    }

    protected void startLocationUpdates() {

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        myRef.child(id).child("latitude").setValue(location.getLatitude());
        myRef.child(id).child("longitude").setValue(location.getLongitude());
        myRef.child(id).child("status").setValue("Online");
        LatitudeText.setText(Double.toString(location.getLatitude()));
        LongitudeText.setText(Double.toString(location.getLongitude()));
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}
