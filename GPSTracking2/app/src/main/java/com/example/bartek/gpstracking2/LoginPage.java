package com.example.bartek.gpstracking2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPage extends AppCompatActivity {

    private EditText txtEmail;
    private  EditText txtPassword;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        txtEmail = (EditText) findViewById(R.id.email);
        txtPassword = (EditText) findViewById(R.id.password);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){
            finish();
            startActivity(new Intent(getApplicationContext(),ActivityPage.class));
        }

    }

    public void login(View v){
        if (!TextUtils.isEmpty(txtEmail.getText().toString()) && !TextUtils.isEmpty(txtPassword.getText().toString()) ){
            final ProgressDialog progressDialog = ProgressDialog.show(LoginPage.this,"Please wait","Processing",true);
            firebaseAuth.signInWithEmailAndPassword(txtEmail.getText().toString(),txtPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(LoginPage.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(),ActivityPage.class));
                                //Intent i = new Intent(LoginPage.this, ActivityPage.class);
                                //i.putExtra("Passed Id",txtEmail.getText().toString());
                                //startActivity(i);

                            }else{
                                Toast.makeText(LoginPage.this, task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else
        {
            Toast.makeText(this, "Provide ID", Toast.LENGTH_LONG).show();
        }
    }

    public void register(View v){
        if (!TextUtils.isEmpty(txtEmail.getText().toString()) && !TextUtils.isEmpty(txtPassword.getText().toString()) ){
            final ProgressDialog progressDialog = ProgressDialog.show(LoginPage.this,"Please wait","Processing",true);
            firebaseAuth.createUserWithEmailAndPassword(txtEmail.getText().toString(),txtPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(LoginPage.this,"Register Successful",Toast.LENGTH_SHORT).show();
                                fillBase();
                                finish();
                                startActivity(new Intent(getApplicationContext(),ActivityPage.class));
                            }else{
                                Toast.makeText(LoginPage.this, task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else
        {
            Toast.makeText(this, "Provide ID", Toast.LENGTH_LONG).show();
        }
    }

    public void fillBase(){
        database = FirebaseDatabase.getInstance("https://gpstracking2-d6443.firebaseio.com/");
        myRef = database.getReference();
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("boundary").setValue("NotActive");
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("BndLat").setValue(0);
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("BndLng").setValue(0);
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("emergency").setValue("false");
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("latitude").setValue(0);
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("listener").setValue("Offline");
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("longitude").setValue(0);
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("radius").setValue(1);
        myRef.child("Locations").child(firebaseAuth.getCurrentUser().getUid()).child("status").setValue("Offline");
    }

    public void onStartClick(View x)
    {
        EditText IdToPass = (EditText)findViewById(R.id.email);
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
