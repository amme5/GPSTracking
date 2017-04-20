package com.example.bartek.gpstracking2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

    }

    public void onStartClick(View x)
    {
        EditText IdToPass = (EditText)findViewById(R.id.IdText);
        String str = IdToPass.getText().toString();

        if (!TextUtils.isEmpty(str))
        {
            if(x.getId() == R.id.addIdbutton)
            {
                Intent i = new Intent(LoginPage.this, send_loc.class);
                i.putExtra("Passed Id",str);
                startActivity(i);
            }
            if(x.getId() == R.id.mapButton)
            {
                Intent j = new Intent(LoginPage.this, MapPage.class);
                j.putExtra("Passed Id",str);
                startActivity(j);
            }
        }else
        {
            Toast.makeText(this, "Provide ID", Toast.LENGTH_LONG).show();
        }




    }
}
