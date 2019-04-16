package com.example.tryapp;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class BackgroundAppLockService extends Service {
    private static final String TAG = "mytag";

    public BackgroundAppLockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: app lock service started");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
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
                            //Log.d(TAG, "run: " + topPackageName);
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
