package com.example.bartek.gpstracking2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends Activity {

    private EditText txtEmail;
    private  EditText txtPassword;
    private FirebaseAuth firebaseAuth;

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
        finish();
        startActivity(new Intent(getApplicationContext(),SignInPage.class));
    }

}
