package com.example.bartek.gpstracking2;

import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class send_loc extends android.app.Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback{

    TextView readyText;
    TextView textView4,insideOut;
    ToggleButton emergencyButton;
    EditText boundaryEditText;
    //Button delete,setBoundary;
    String boundRad;
    FloatingActionButton zoom, setBoundary;
    private SeekBar sb;
    ImageView insidebg;

    String danger = "Other user informed, to cancel press button";
    String safe = "In case of emergency press button";
    String danger2 = "Listener if offline, you are on your own";
    String online = "Online";
    String offline = "Offline";
    boolean reset=true;


    DatabaseReference myRef,myRefStatus;
    FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    //String id;
    //GPS-------
    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
   // private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
  //  private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LocationManager locationManager;
    //----------
    //Maps----
    GoogleMap mGoogleMap;
    double radius;
    LatLng center;
    boolean firstMap=true;
    double lat=0,lng=0;
    //-------

    double x1,y1,x2,y2,radius1Area,radius2Area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.send_loc);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        readyText = (TextView) findViewById(R.id.readyText);
        emergencyButton = (ToggleButton) findViewById(R.id.emergencyButton);
        insideOut = (TextView) findViewById(R.id.textView9);

        firebaseAuth= FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this,LoginPage.class));
        }

        user = firebaseAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations/"+user.getUid());
        myRef.keepSynced(true);

        textView4 = (TextView) findViewById(R.id.textView4);
        setBoundary = (FloatingActionButton)findViewById(R.id.boundSetFloat);
        zoom = (FloatingActionButton)findViewById(R.id.zoomFloat);
        insidebg = (ImageView) findViewById(R.id.imageView3);
        insidebg.setVisibility(View.INVISIBLE);

        sb = (SeekBar)findViewById(R.id.seekBar);
        sb.setProgress(100);
        sb.setVisibility(View.INVISIBLE);

        insidebg.setVisibility(View.INVISIBLE);
        setBoundary.setVisibility(View.VISIBLE);
        insideOut.setVisibility(View.INVISIBLE);


        myRef.child("boundary").setValue("NotActive");
        listenerOfflineButton();

        firstMap=true;
        initmap();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        checkLocation();
        myRef.child("status").onDisconnect().setValue("Offline");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    radius = dataSnapshot.child("radius").getValue(Double.class);
                    lat=dataSnapshot.child("latitude").getValue(Double.class);
                    lng=dataSnapshot.child("longitude").getValue(Double.class);

                    if(dataSnapshot.child("listener").getValue(String.class).equals("Online"))
                    {
                        if(reset){
                            resetButton();
                            reset=false;
                        }
                    }
                    else{
                        reset=true;
                        listenerOfflineButton();
                    }

                    if(!dataSnapshot.child("boundary").getValue(String.class).equals("NotActive")){

                        x1=dataSnapshot.child("BndLat").getValue(Double.class);
                        y1=dataSnapshot.child("BndLng").getValue(Double.class);
                        radius1Area=Math.PI*radius*radius;
                        center=new LatLng(x1,y1);

                        x2=dataSnapshot.child("latitude").getValue(Double.class);
                        y2=dataSnapshot.child("longitude").getValue(Double.class);

                        radius2Area=haversine(x1,y1,x2,y2);
                        if(radius1Area<radius2Area){
                            myRef.child("boundary").setValue("Outside");
                            //insideOut.setVisibility(View.VISIBLE);
                            insideOut.setText("Outside");
                            insideOut.setTextColor(Color.parseColor("#FF4444"));
                        }else
                        {
                            myRef.child("boundary").setValue("Inside");
                            //insideOut.setVisibility(View.VISIBLE);
                            insideOut.setText("Inside");
                            insideOut.setTextColor(Color.parseColor("#2B8A4E"));
                        }
                        goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
                        drawCircle(center,radius);
                    }else{
                        //insideOut.setVisibility(View.INVISIBLE);
                        goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
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
                        .setMessage("Set boundary [km]")
                        .setPositiveButton("Activate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boundRad = boundaryEditText.getText().toString();
                                if (!TextUtils.isEmpty(boundRad) && Double.parseDouble(boundRad)<5){
                                    myRef.child("radius").setValue(Double.parseDouble(boundaryEditText.getText().toString()));
                                    myRef.child("BndLat").setValue(lat);
                                    myRef.child("BndLng").setValue(lng);
                                    myRef.child("boundary").setValue("Inside");
                                    sb.setProgress(100);
                                    insideOut.setVisibility(View.VISIBLE);
                                    insidebg.setImageResource(R.drawable.insideoutnormal);
                                    insidebg.setVisibility(View.VISIBLE);
                                    sb.setVisibility(View.VISIBLE);
                                    setBoundary.setVisibility(View.INVISIBLE);
                                }else{
                                    Toast.makeText(send_loc.this, "Provide number less than 5km", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setCancelable(false);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        //----------------------------------------------------------
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sb.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                insidebg.setImageResource(R.drawable.insideout);
                insidebg.setVisibility(View.VISIBLE);
                insideOut.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(sb.getProgress()<5){
                    myRef.child("boundary").setValue("NotActive");
                    setBoundary.setVisibility(View.VISIBLE);
                    sb.setVisibility(View.INVISIBLE);
                    insidebg.setVisibility(View.INVISIBLE);
                    insidebg.setImageResource(R.drawable.insideoutnormal);
                }else{
                    insidebg.setImageResource(R.drawable.insideoutnormal);
                    //insidebg.setVisibility(View.INVISIBLE);
                    insideOut.setVisibility(View.VISIBLE);
                    sb.setProgress(100);
                }

            }
        });

        //----------------------------------------------------------

    }

    public void zoomToMyLoc(View view){
        firstMap=true;
        LatLng ll= new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,16);
        mGoogleMap.animateCamera(update);
    }
    public void resetButton(){
        emergencyButton.setEnabled(true);
        emergencyButton.setChecked(false);
        emergencyButton.setBackgroundColor(Color.parseColor("#FF4444"));
        textView4.setText(safe);
        myRef.child("emergency").setValue("false");
    }
    public void listenerOfflineButton(){
        emergencyButton.setEnabled(false);
        emergencyButton.setChecked(false);
        emergencyButton.setBackgroundColor(Color.parseColor("#FF808080"));
        textView4.setText(danger2);
        myRef.child("emergency").setValue("false");
    }

    public void emergency(View view){

            if(emergencyButton.isChecked()){
                textView4.setText(danger);
                emergencyButton.setBackgroundColor(Color.parseColor("#2B8A4E"));
                myRef.child("emergency").setValue("true");
            }
            else{
                textView4.setText(safe);
                emergencyButton.setBackgroundColor(Color.parseColor("#FF4444"));
                myRef.child("emergency").setValue("false");
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
        setUserStatus(true);
        firstMap=true;
        //Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
    }

 /*   @Override
    public void onBackPressed(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        first=true;
        myRef.child(user.getUid()).child("boundary").setValue("NotActive");
        setUserStatus(false);
        firstMap=true;
        finish();
        startActivity(new Intent(getApplicationContext(),ActivityPage.class));
       // Intent i = new Intent(send_loc.this, LoginPage.class);
       // startActivity(i);
    }*/
    @Override
    protected void onStop() {
        super.onStop();
        /*if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }*/
        firstMap=true;
        //myRef.child("boundary").setValue("NotActive");
        //setUserStatus(false);
        //boundary="NotActive";
        //deleteClick();
        //Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus(true);
        firstMap=true;
        //Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        myRef.goOffline();
        //setUserStatus(false);
        //first=true;
        //Toast.makeText(this, "Destruction", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        myRef.goOffline();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        setUserStatus(false);
        finish();
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
    }
    public void setUserStatus(boolean stat){
        if(stat){
            myRef.child("status").setValue(online);
        }else{
            myRef.child("status").setValue(offline);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myRef.child("latitude").setValue(location.getLatitude());
        myRef.child("longitude").setValue(location.getLongitude());
        setUserStatus(true);
        lat=location.getLatitude();
        lng=location.getLongitude();
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

    private void initmap() {

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment2);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private void goToLocation(double lat, double lng, float zoom) {
        mGoogleMap.clear();
        LatLng ll= new LatLng(lat,lng);
        MarkerOptions options = new MarkerOptions().title(user.getEmail()).position(ll);
        mGoogleMap.addMarker(options);
        //if(!boundary.equals("NotActive")){
           // drawCircle();
       // }

        if(firstMap){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
            mGoogleMap.animateCamera(update);
            firstMap=false;
        }else{
            CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
            mGoogleMap.animateCamera(update);
        }

    }
    private Circle drawCircle(LatLng center1, double rad){
        CircleOptions options = new CircleOptions().center(center1).radius(rad*1000).fillColor(0x33FF0000).strokeColor(Color.RED).strokeWidth(3);
        return mGoogleMap.addCircle(options);
    }
}
