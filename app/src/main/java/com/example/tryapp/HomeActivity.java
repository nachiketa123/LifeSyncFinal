package com.example.tryapp;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "mytag";
    private Button button;
    private Button button1;
    private String text2Qr;
    public Button signoutbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            /*
            Bundle bundle = getIntent().getExtras();
            text2Qr = bundle.getString("text");
            */
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "device token"+ task.getResult().getToken());
                    }
                    else{
                        Log.d(TAG, "onComplete: unsuccessfull");
                    }
                }
            });
            button = findViewById(R.id.sm);
            signoutbtn=findViewById(R.id.signout);
            signoutbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
//                        Log.d("mytag","inside onClick");
                        openSelfMode();
                    } catch (Exception e)
                    {
                        Log.d("mytag",e.getMessage());
                    }
                }
            });
            button1 = findViewById(R.id.rm);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openRMS();
                }
            });
        }catch (Exception e)
        {
            Log.e("mytag",e.getMessage());
        }


        //---------------------------------------------------
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + FirebaseAuth.getInstance().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String type = user.getType();

                //as we know sos sender will be CHILD
                String childId = user.getLocatorId();

                //but in case type is child
                if (type.equals("CHILD")) {

                    //start the service to fetch and block the apps which are in blocked list
                    Intent intent = new Intent(getApplicationContext(),BackgroundAppLockService.class);
                    startService(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //---------------------------------------------------
    }

    public void openSelfMode(){
//        Log.d("mytag","inside onOpenSelf");
        Intent intent=new Intent(this,SelfModeActivity.class);
        startActivity(intent);
    }

    public void openRMS(){
        Intent intent=new Intent(this,RemoteAccessMode.class);
        /*Bundle bundle=new Bundle();
        bundle.putString("text",text2Qr);
        intent.putExtras(bundle);
        */
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.settings)
        {
            Log.d("mytag","settings");
        }
        if(id == R.id.share)
        {
            Log.d("mytag","share");
        }
        if(id == R.id.signout)
        {
            Log.d("mytag","signout");
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkIfPermissionAlreadyGiven(Context context) {
        AppOpsManager appOps;
        int mode = 50;
        try {
            appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        }catch (Exception e)
        {
            Log.e("mytag",e.getMessage());
        }
        return mode == MODE_ALLOWED;
    }

    public void showAlertDialog() {
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Permission Required")
                .setMessage("App must use App Usage Stat to work.. click OK and goto LifeSync and enable 'Permit usage access'")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        }).create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!checkIfPermissionAlreadyGiven(getApplicationContext()))
            showAlertDialog();
    }
}
