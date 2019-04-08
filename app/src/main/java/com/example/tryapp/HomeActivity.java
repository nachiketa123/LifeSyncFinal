package com.example.tryapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
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
}
