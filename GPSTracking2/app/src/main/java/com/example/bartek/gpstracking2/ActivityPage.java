package com.example.bartek.gpstracking2;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
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
    private ImageView imageMap,imageLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        FirebaseDatabase database;
        DatabaseReference myRef;

        firebaseAuth= FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this,LoginPage.class));
        }else{

        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations/"+firebaseAuth.getCurrentUser().getUid());

        imageMap = (ImageView) findViewById(R.id.imageMap);
        imageLoc = (ImageView) findViewById(R.id.imageLoc);

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.menu,menu);

        return true;
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
}
