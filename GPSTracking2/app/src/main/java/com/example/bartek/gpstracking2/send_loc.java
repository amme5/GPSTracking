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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    TextView readyText;
    TextView textView4,textView3,insideOut;
    ToggleButton emergencyButton;
    ImageView imageMap;
    EditText boundaryEditText;
    Button delete,setBoundary;
    String bound;

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
    double x1,y1,x2,y2,radius1,radius2;
    boolean first=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.send_loc);

        readyText = (TextView) findViewById(R.id.readyText);
        emergencyButton = (ToggleButton) findViewById(R.id.emergencyButton);
        insideOut = (TextView) findViewById(R.id.textView9);
        imageMap = (ImageView) findViewById(R.id.imageView);
        imageMap.setImageResource(R.drawable.popup2);

        id=getIntent().getStringExtra("Passed Id");
        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations");
        myRefStatus=database.getReference();
        textView4 = (TextView) findViewById(R.id.textView4);
        textView3 = (TextView) findViewById(R.id.textView3);
        delete = (Button) findViewById(R.id.button4);
        delete.setVisibility(View.INVISIBLE);
        setBoundary = (Button) findViewById(R.id.button2);
        setBoundary.setVisibility(View.VISIBLE);
        insideOut.setVisibility(View.INVISIBLE);

        myRef.child(id).child("boundary").setValue("NotActive");
        first=true;


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

                    if(!ds.child(id).child("boundary").getValue(String.class).equals("NotActive")){
                        if(first)
                        {
                            x1=ds.child(id).child("latitude").getValue(Double.class);
                            y1=ds.child(id).child("longitude").getValue(Double.class);
                            x2=x1;
                            y2=y1;
                            first=false;
                            radius1=Math.PI*Double.parseDouble(bound)*Double.parseDouble(bound);
                            Log.d("rad", "rad1: "+radius1);
                        }else
                        {
                            x2=ds.child(id).child("latitude").getValue(Double.class);
                            y2=ds.child(id).child("longitude").getValue(Double.class);
                        }
                        radius2=haversine(x1,y1,x2,y2);
                        Log.d("rad", "rad2: "+radius2);
                        if(radius1<radius2){
                            myRef.child(id).child("boundary").setValue("Outside");
                            insideOut.setVisibility(View.VISIBLE);
                            insideOut.setText("Outside");
                            insideOut.setTextColor(Color.parseColor("#FF0000"));
                        }else
                        {
                            myRef.child(id).child("boundary").setValue("Inside");
                            insideOut.setVisibility(View.VISIBLE);
                            insideOut.setText("Inside");
                            insideOut.setTextColor(Color.parseColor("#009933"));
                        }
                    }else{
                        insideOut.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setBoundary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(send_loc.this);

                View mview = getLayoutInflater().inflate(R.layout.popup,null);

                boundaryEditText = (EditText) mview.findViewById(R.id.editTextpop);

                builder.setView(mview)
                        .setMessage("Set boundary")
                        .setPositiveButton("Activate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                bound = boundaryEditText.getText().toString();
                                if (!TextUtils.isEmpty(bound)){
                                    first=true;
                                    myRef.child(id).child("radius").setValue(Double.parseDouble(boundaryEditText.getText().toString()));
                                    myRef.child(id).child("boundary").setValue("Inside");
                                    delete.setVisibility(View.VISIBLE);
                                    setBoundary.setVisibility(View.INVISIBLE);

                                }else{
                                    Toast.makeText(send_loc.this, "Provide number", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setCancelable(false);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });



    }


    public void deleteClick(View x){
        myRef.child(id).child("boundary").setValue("NotActive");
        first=true;
        setBoundary.setVisibility(View.VISIBLE);
        delete.setVisibility(View.INVISIBLE);
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
        //myRef.child(id).child("boundary").setValue("NotActive");
        //first=true;
    }

    @Override
    public void onBackPressed(){
        myRef.child(id).child("boundary").setValue("NotActive");
        first=true;
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
        myRef.child(id).child("boundary").setValue("NotActive");
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
        readyText.setText("Connected");
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

    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;

        return Math.PI*(d*d);
    }

}
