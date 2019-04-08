package com.example.tryapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPass_Activity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText et1;
    ProgressDialog progressDialog;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass_);
            et1=findViewById(R.id.verifyem);
            b=findViewById(R.id.button5);
            progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Sending Email...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        progressDialog.show();
                        mAuth= FirebaseAuth.getInstance();
                        String emailv=et1.getText().toString().trim();
                        if(emailv.isEmpty()){
                            progressDialog.dismiss();
                            et1.setError("This field is required");
                        }
                        else if(!Patterns.EMAIL_ADDRESS.matcher(emailv).matches()){
                            progressDialog.dismiss();
                            et1.setError("Please enter a valid email address");
                        }

                        mAuth.sendPasswordResetEmail(emailv).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(ResetPass_Activity.this,"Password Reset email sent",Toast.LENGTH_LONG).show();
                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(ResetPass_Activity.this,"Error while sending email\n\nEmail not yet registered",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }catch (Exception e){
                        Log.e("mytag",e.getMessage());
                    }
                }
            });
    }
}
