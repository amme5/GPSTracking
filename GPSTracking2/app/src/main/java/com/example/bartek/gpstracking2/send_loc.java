package com.example.bartek.gpstracking2;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class send_loc extends android.app.Activity {

    EditText LatitudeText;
    EditText LongitudeText;
    Button button;
    DatabaseReference myRef;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.send_loc);

        LatitudeText = (EditText) findViewById(R.id.LatitudeText);
        LongitudeText = (EditText) findViewById(R.id.Longitude);
        button = (Button) findViewById(R.id.button);

        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference("Locations");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passedId = getIntent().getStringExtra("Passed Id");
                addLocation(passedId);
            }
        });


    }

    private void addLocation(String id) {
        String latitude_fromBox = LatitudeText.getText().toString().trim();
        String longitude_fromBox = LongitudeText.getText().toString().trim();
        double latitude=Double.parseDouble(latitude_fromBox);
        double longitude=Double.parseDouble(longitude_fromBox);

        //getLocation();


        if (!TextUtils.isEmpty(latitude_fromBox) && !TextUtils.isEmpty(longitude_fromBox)) {

            //UserLocation location = new UserLocation(latitude,id,longitude);
            UserLocation location = new UserLocation();
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            myRef.child(id).child("latitude").setValue(location.getLatitude());
            myRef.child(id).child("longitude").setValue(location.getLongitude());

            Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show();

        }else
        {
            Toast.makeText(this, "Provide location", Toast.LENGTH_LONG).show();
        }
    }


}
