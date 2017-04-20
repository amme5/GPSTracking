package com.example.bartek.gpstracking;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.R.attr.x;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(googleServicesAvailable()){
            Toast.makeText(this,"API is correct",Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main);
        }else{
            //todo add window for no google maps
        }
    }

    public boolean googleServicesAvailable()
    {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable,0);
            dialog.show();
        }else{
            Toast.makeText(this,"Cant connect to play services",Toast.LENGTH_LONG).show();
        }
        return false;

    }

    public void onStartClick(View x)
    {
        EditText IdToPass = (EditText)findViewById(R.id.IdText);
        String str = IdToPass.getText().toString();

        if(x.getId() == R.id.addIdbutton)
        {
            Intent i = new Intent(MainActivity.this, idpage.class);
            i.putExtra("Passed Id",str);
            startActivity(i);
        }
        if(x.getId() == R.id.mapButton)
        {
            Intent j = new Intent(MainActivity.this, map.class);
            j.putExtra("Passed Id",str);
            startActivity(j);
        }


    }


}
