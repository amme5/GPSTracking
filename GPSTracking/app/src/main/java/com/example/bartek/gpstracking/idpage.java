package com.example.bartek.gpstracking;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class idpage extends Activity {

    EditText LatitudeText;
    EditText LongitudeText;
    Button button;

    double latitude, longitude;


    DatabaseReference databaseLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idpage);

        databaseLocation = FirebaseDatabase.getInstance().getReference("locations");

        LatitudeText = (EditText) findViewById(R.id.LatitudeText);
        LongitudeText = (EditText) findViewById(R.id.Longitude);
        button = (Button) findViewById(R.id.button);

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

        //getLocation();


        if (!TextUtils.isEmpty(latitude_fromBox) || !TextUtils.isEmpty(longitude_fromBox)) {

            Location1 location = new Location1(id, latitude_fromBox, longitude_fromBox);
            databaseLocation.child(id).setValue(location);
            Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show();

        }else
        {
            Toast.makeText(this, "Provide location", Toast.LENGTH_LONG).show();
        }
    }


}
