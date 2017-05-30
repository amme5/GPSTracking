package com.example.bartek.gpstracking2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapPage extends android.app.Activity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    private Marker marker;
    private Circle circle;
    DatabaseReference myRef;
    FirebaseDatabase database;
    ValueEventListener vel;

    private FirebaseUser user;
    TextView textstatus,emerg,boundaryText,emergText,boundaryText2;
    FloatingActionButton callBtn, zoomBtn;
    String online = "Online";
    String offline = "Offline";
    String phoneNum;
    boolean first=true;
    double x1,y1;
    LatLng center;
    double lat=0,lng=0;
    private NotificationManager nm;

    boolean notifActv = false;
    boolean notifActv2 = false;
    boolean notifActv3 = false;

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
        callBtn = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        zoomBtn = (FloatingActionButton)findViewById(R.id.floatingActionButton2);

        textVisibility(false);

        myRef.child("listener").setValue(online);
        myRef.child("listener").onDisconnect().setValue(offline);

        initmap();

        myRef.addValueEventListener(vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phoneNum = dataSnapshot.child("phone").getValue(String.class);
                lat=dataSnapshot.child("latitude").getValue(Double.class);
                lng=dataSnapshot.child("longitude").getValue(Double.class);
                if(dataSnapshot.child("status").getValue(String.class).equals("Online"))
                {
                    textVisibility(true);
                    notifActv=false;
                    if(nm!=null) nm.cancel(1);
                    textstatus.setText(online);
                    textstatus.setTextColor(Color.parseColor("#2B8A4E"));

                    if(dataSnapshot.child("emergency").getValue(String.class).equals("true")){
                        emerg.setText("In danger");
                        if(!notifActv3){
                            generateDangerNotification();
                            notifActv3=true;
                        }
                        emerg.setTextColor(Color.parseColor("#FF4444"));
                    }
                    else{
                        emerg.setText("Ok");
                        notifActv3=false;
                        if(nm!=null) nm.cancel(2);
                        emerg.setTextColor(Color.parseColor("#2B8A4E"));
                    }

                    if(!dataSnapshot.child("boundary").getValue(String.class).equals("NotActive")){

                        x1=dataSnapshot.child("BndLat").getValue(Double.class);
                        y1=dataSnapshot.child("BndLng").getValue(Double.class);
                        center=new LatLng(x1,y1);

                        if(dataSnapshot.child("boundary").getValue(String.class).equals("Inside")){
                            boundaryText.setText("Inside");
                            boundaryText.setTextColor(Color.parseColor("#2B8A4E"));
                            notifActv2=false;
                            if(nm!=null) nm.cancel(0);
                        }else{
                            boundaryText.setText("Outside");
                            boundaryText.setTextColor(Color.parseColor("#FF4444"));
                            if(!notifActv2){
                                generateBndNotification();
                                notifActv2=true;
                            }
                        }

                        goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
                        drawCircle(center,dataSnapshot.child("radius").getValue(Double.class));

                    }else{
                        boundaryText.setText("Not active");
                        boundaryText.setTextColor(Color.GRAY);
                        if(nm!=null) nm.cancel(0);
                        notifActv2=false;
                        removeCircle();
                        goToLocation(dataSnapshot.child("latitude").getValue(Double.class),dataSnapshot.child("longitude").getValue(Double.class),16);
                    }

                }
                else{
                    textVisibility(false);
                    textstatus.setText(offline);
                    first=true;
                    emerg.setText("");
                    if(nm!=null) {
                        nm.cancel(2);
                        nm.cancel(0);
                    }
                    if(!notifActv){
                        generateOffNotification();
                        notifActv=true;
                    }
                    textstatus.setTextColor(Color.parseColor("#FF4444"));
                    mGoogleMap.clear();
                    marker=null;
                    circle=null;
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
        LatLng ll= new LatLng(lat,lng);
        if(marker==null){
            MarkerOptions options = new MarkerOptions().title(user.getEmail()).position(ll);
            marker = mGoogleMap.addMarker(options);
        }else{
            marker.setPosition(ll);
        }

        if(first){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
            mGoogleMap.animateCamera(update);
            first=false;
        }else{
            CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
            mGoogleMap.animateCamera(update);
        }

    }
    private void drawCircle(LatLng center1, double rad){
        if(circle==null){
            CircleOptions options = new CircleOptions().center(center1).radius(rad*1000).fillColor(0x33FF0000).strokeColor(Color.RED).strokeWidth(3);
            circle = mGoogleMap.addCircle(options);
        }
    }
    private void removeCircle(){
        if(circle!=null){
            circle.remove();
            circle = null;
        }
    }

    public void textVisibility(boolean var){
        if(var){
            emerg.setVisibility(View.VISIBLE);
            emergText.setVisibility(View.VISIBLE);
            boundaryText.setVisibility(View.VISIBLE);
            boundaryText2.setVisibility(View.VISIBLE);
            callBtn.setVisibility(View.VISIBLE);
            zoomBtn.setVisibility(View.VISIBLE);
        }else
        {
            emerg.setVisibility(View.INVISIBLE);
            emergText.setVisibility(View.INVISIBLE);
            boundaryText.setVisibility(View.INVISIBLE);
            boundaryText2.setVisibility(View.INVISIBLE);
            callBtn.setVisibility(View.INVISIBLE);
            zoomBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void zoomToMyLoc(View view){
        first=true;
        LatLng ll= new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,16);
        mGoogleMap.animateCamera(update);
    }

    public void phone(View view){
        if(phoneNum != null){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNum));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void generateBndNotification(){
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.drawable.notification_icon);
        notification.setTicker("User outside");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("SafetyM");
        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notification.setContentText(user.getEmail()+" - User is outside the boundary");

        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(pendingIntent);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification.build());
    }

    public void generateOffNotification(){
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.drawable.notification_icon);
        notification.setTicker("SafetyM - user offline");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("SafetyM");
        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notification.setContentText(user.getEmail()+" - Location provider is offline");

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, notification.build());
    }
    public void generateDangerNotification(){
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.drawable.notification_icon);
        notification.setTicker("SafetyM - user in danger!");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("SafetyM");
        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notification.setContentText(user.getEmail()+" - User is in danger!");

        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(pendingIntent);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(2, notification.build());
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(nm!=null) nm.cancelAll();
        myRef.removeEventListener(vel);
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
    }
    @Override
    protected void onResume() {
        super.onResume();
        myRef.child("listener").setValue("Online");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.removeEventListener(vel);
        if(nm!=null) nm.cancelAll();
        myRef.child("listener").setValue("Offline");
    }



}