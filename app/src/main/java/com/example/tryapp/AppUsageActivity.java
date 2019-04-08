package com.example.tryapp;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class AppUsageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appusage);

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        if(!checkIfPermissionAlreadyGiven(AppUsageActivity.this))
        {
            new AlertDialog.Builder(AppUsageActivity.this)
                    .setTitle("Permission Required")
                    .setMessage("App must use App Usage Stat to work.. click OK and goto TestApp2 and enable 'Permit usage access'")
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

        List<ResolveInfo> pkgAppsList = this.getPackageManager().queryIntentActivities( mainIntent, 0); //extracting all the packages
        final ArrayList<AppList> appList =new ArrayList<AppList>();

        for(ResolveInfo info : pkgAppsList) {

            try {
                ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
                //...
                //get package name, icon and label from applicationInfo object
                AppList app = new AppList(applicationInfo.loadIcon(getPackageManager())
                        , applicationInfo.loadLabel(getPackageManager()).toString()
                        , applicationInfo.packageName);
                appList.add(app);
            }catch (Exception e)
            {
                Log.e("mytag",e.getMessage());
            }
        }

        MyAdapter myAdapter = new MyAdapter(this,appList);
        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        ListView myListView = findViewById(R.id.myListView);
        myListView.setAdapter(myAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Log.d("mytag",String.valueOf(position));
                final Dialog dialog = new Dialog(AppUsageActivity.this);
                dialog.setTitle(appList.get(position).getName());
                dialog.setContentView(R.layout.dbox_layout);
                TextView tvTime = dialog.findViewById(R.id.tvtime);
                float time = (float)appList.get(position).getTime();
                int hrs = (int) (time/3600);
                time -= (hrs*3600);
                int min = (int) (time/60);
                time -= (min*60);
                int sec = (int) time;
                tvTime.setText(hrs + "hours" +"\n"+ min +  getString(R.string.minutes) +"\n"+ + sec + getString(R.string.seconds));
                dialog.show();
                dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        final long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start,end);

        for(AppList app : appList)
        {
            try {
                float time = (float)stats.get(app.getPackageName()).getTotalTimeInForeground()/1000f;
                app.setTime(time);
            }catch (NullPointerException npe)
            {
                //  Log.e("mytag",app.getPackageName() + "\t" + npe.getMessage());
            }
        }

    }

    private boolean checkIfPermissionAlreadyGiven(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;

    }

}
