package com.example.tryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class RemoteAccessMode extends AppCompatActivity {

    Button button;
    Button button1;
    EditText editText;
    ImageView image;
    String text2Qr;
    String type,myId;
    DatabaseReference ref;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_access_mode);
        button1=findViewById(R.id.gqrbtn);
        button=findViewById(R.id.sqrbtn);
        progressBar = findViewById(R.id.progressBar);
        button.setVisibility(View.GONE);
        button1.setVisibility(View.GONE);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ref = FirebaseDatabase.getInstance().getReference("/USERS/"+FirebaseAuth.getInstance().getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    type = user.getType();
                    myId = user.getLocatorId();

                    if(type.equals("PARENT"))
                    {
                        progressBar.setVisibility(View.GONE);
                        button1.setVisibility(View.VISIBLE);
                    }
                    if(type.equals("CHILD"))
                    {
                        progressBar.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"some error occurred",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e)
        {
            Log.e("mytag",e.getMessage());
        }
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{generateqractivity();}
                catch (Exception e)
                {
                    Log.e("mytag",""+e);
                }
            }
        });


        final Activity activity=this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator=new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });
    }


    public void generateqractivity(){

        declareMeParent();

        Intent intent=new Intent(this,GenerateQRActivity.class);
        startActivity(intent);
    }

    private void declareMeParent() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null) {
            if(result.getContents()==null){
                Toast.makeText(this,"You Cancelled the Scanning",Toast.LENGTH_LONG).show();
            }
            else{
                String id = result.getContents();
                Toast.makeText(this,"Connection established",Toast.LENGTH_LONG).show();
                try{

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("/USERS/"+id);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Log.d("mytag","looks fine");
                            User user = dataSnapshot.getValue(User.class);
                            String id = user.getLocatorId();
//                            Log.d("mytag",""+id);
                            new RealTimeDBHandler(RemoteAccessMode.this).updateValueToChild("/CHILD/"+myId,id);
                            new RealTimeDBHandler(RemoteAccessMode.this).updateValueToParent("/PARENT/"+id,myId,FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }catch (Exception e)
                {
                    Log.e("mytag",e.getMessage());
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
