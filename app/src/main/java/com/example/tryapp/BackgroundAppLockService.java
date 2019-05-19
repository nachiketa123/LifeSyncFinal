package com.example.tryapp;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class BackgroundAppLockService extends Service {
    private static final String TAG = "mytag";
    private static ArrayList<String> blockedAppList;
    private Thread blockAppListFetcherThread;
    public BackgroundAppLockService() {
        blockAppListFetcherThread = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    String userId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/"+userId);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            String locatorId = user.getLocatorId();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/CHILD/"+locatorId);
                            //Log.d(TAG, "fetching blocked list from: "+locatorId);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Child child = dataSnapshot.getValue(Child.class);
                                    blockedAppList = child.getBlockedAppList();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    try{
                        Thread.sleep(1000);
                    }catch (Exception e)
                    {
                        Log.e(TAG, "onDataChange: "+e.getMessage() );
                    }
                }
            }
        } );

        blockAppListFetcherThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: app lock service started");
        final AppUsageActivity activity = new AppUsageActivity();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {

                    //getting blockedAppList
                   // activity.fetchBlockedAppListFromDB();

                    //blocking running apps which are present in blockedAppList
                    String topPackageName;
                    UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                    long time = System.currentTimeMillis();
                    // We get usage stats for the last 10 seconds
                    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
                    // Sort the stats by the last time used
                    if (stats != null) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                        for (UsageStats usageStats : stats) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (!mySortedMap.isEmpty()) {
                            topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                            // Log.d(TAG, "run: "+topPackageName);
                            if (blockedAppList != null) {
                                if (blockedAppList.contains(topPackageName)) {
                                    Intent intent = new Intent(getApplicationContext(), BlockScreenActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    //Log.d(TAG, "run: " + topPackageName);
                                }
                            }

                        }
                    }
                }
            }
        };
        Thread applockthread = new Thread(runnable);
        applockthread.start();
        return Service.START_STICKY;
    }
}
