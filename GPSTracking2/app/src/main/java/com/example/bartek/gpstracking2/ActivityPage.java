package com.example.bartek.gpstracking2;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityPage extends Activity {

    private FirebaseAuth firebaseAuth;
    private static long back_pressed;

    boolean isLoc=false,isMap=false;
    private ImageView imageMap,imageLoc, loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        FirebaseDatabase database;
        DatabaseReference myRef;
        isNetworkConnectionAvailable();
        firebaseAuth= FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this,LoginPage.class));
        }else{

        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations/"+firebaseAuth.getCurrentUser().getUid());

        loader = (ImageView) findViewById(R.id.loader);
        Animation blink = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink);
        loader.startAnimation(blink);
        loader.setVisibility(View.VISIBLE);
        imageMap = (ImageView) findViewById(R.id.imageMap);
        imageLoc = (ImageView) findViewById(R.id.imageLoc);
        imageLoc.setVisibility(View.INVISIBLE);
        imageMap.setVisibility(View.INVISIBLE);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue(String.class).equals("Online")){
                    isLoc=true;
                    imageLoc.setImageResource(R.drawable.locoff);
                }else{
                    isLoc=false;
                    imageLoc.setImageResource(R.drawable.locbutton);
                }
                if(dataSnapshot.child("listener").getValue(String.class).equals("Online")){
                    isMap=true;
                    imageMap.setImageResource(R.drawable.mapoff);
                }else{
                    isMap=false;
                    imageMap.setImageResource(R.drawable.mapbutton);
                }
                imageLoc.setVisibility(View.VISIBLE);
                imageMap.setVisibility(View.VISIBLE);
                loader.clearAnimation();
                loader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }
    }
    public void logout(View view){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginPage.class));
    }
    public void onLocClick(View x){
        if(!isLoc){
            Intent i = new Intent(ActivityPage.this, send_loc.class);
            startActivity(i);
        }
    }
    public void onMapClick(View x){
        if(!isMap){
            Intent i = new Intent(ActivityPage.this, MapPage.class);
            startActivity(i);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isNetworkConnectionAvailable();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            finish();
            System.exit(0);
        }
        else{
            Toast.makeText(getBaseContext(),"Press once again to exit", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }
    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(myIntent);
            }
        });
        builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isNetworkConnectionAvailable();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if(isConnected) {
            Log.d("Network", "Connected");
            return true;
        }
        else{
            checkNetworkConnection();
            Log.d("Network","Not Connected");
            return false;
        }
    }
}
