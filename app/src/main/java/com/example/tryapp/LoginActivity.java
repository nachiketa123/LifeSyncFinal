package com.example.tryapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN=
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$");

    private FirebaseAuth mAuth;
    Button lgbtn,rstbtn;
    private EditText etemail,etpassword;
    public static String email;
    TextView textView;
    ProgressDialog progressDialog;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etemail=findViewById(R.id.ete);
        etpassword=findViewById(R.id.etp);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("User Login");
        progressDialog.setMessage("Logging in...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        lgbtn=findViewById(R.id.lgbtn);

        //---------------------------------------------------

        Intent intent = new Intent(this,BackgroundAppLockService.class);
        startService(intent);

        //---------------------------------------------------


        lgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressDialog.show();
                if(checkInternetConnection())
                    openHome();
                else
                {
                    Toast.makeText(getApplicationContext(),"Check Your Internet Connectionn and then try again..",Toast.LENGTH_SHORT).show();
                }
            }
        });
        textView=findViewById(R.id.txtvw);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        rstbtn=findViewById(R.id.resetbtn);
        rstbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPass_Activity.class));
            }
        });
        mAuth=FirebaseAuth.getInstance();
    }

    private boolean checkInternetConnection() {
        boolean connected ;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {

            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginActivity.super.onBackPressed();
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void openHome(){
        //Toast.makeText(LoginActivity.this,"Login Successfull",Toast.LENGTH_SHORT).show();
        final String password;
        email=etemail.getText().toString().trim();
        password=etpassword.getText().toString().trim();

        if(email.isEmpty() && password.isEmpty()){
            progressDialog.dismiss();
            etemail.setError("Field can't be empty");
            etpassword.setError("Field can't be empty");
        }
        else if(email.isEmpty()){
            progressDialog.dismiss();
            etemail.setError("Field can't be empty");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            progressDialog.dismiss();
            etemail.setError("Please enter a valid email address");
        }
        else if(password.isEmpty()){
            progressDialog.dismiss();
            etpassword.setError("Field can't be empty");
        }
        else{
                    mAuth.signInWithEmailAndPassword(LoginActivity.email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           try{
                               if(mAuth.getCurrentUser().isEmailVerified() && task.isSuccessful()){
                                   progressDialog.dismiss();
                                   Toast.makeText(getApplicationContext(),"Authentication Success",Toast.LENGTH_LONG).show();
                                   startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                               }
//                            else if(mAuth.getCurrentUser().getEmail().matches(etemail.getText().toString())){
//                                Toast.makeText(LoginActivity.this,"Email does not exists",Toast.LENGTH_LONG).show();
//                            }
                               else {
                                   Toast.makeText(getApplicationContext(),"Authentication failed",Toast.LENGTH_SHORT).show();
                                   Toast.makeText(getApplicationContext(), "    Either email does not exists\n                       or                            \nYou have not verified your email",Toast.LENGTH_LONG).show();
                                  // Toast.makeText(getApplicationContext(),"Email or password is incorrect",Toast.LENGTH_LONG).show();
                               }
                           }catch (Exception e){
                               progressDialog.dismiss();
                               Log.e("mytag",e.getMessage());
                           }
                        }
                    });
        }
        }
}
