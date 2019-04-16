package com.example.tryapp;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        new RealTimeDBHandler(this).updateTokenToUser("/USERS/", s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        Log.d("mytag", "onMessageError " + e.getMessage());
        super.onSendError(s, e);
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d("mytag", "onMessageSent");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("mytag", "onMessageReceived");
        // Toast.makeText(getApplicationContext(),"Message Received",Toast.LENGTH_SHORT).show();
       if(Looper.myLooper() == null)
           Looper.prepare();
        if (remoteMessage.getData().get("msg_type").equals("incomming_appDetails")) {
            try {
                Map<String, String> appDetailsMap = remoteMessage.getData();
                Gson gson = new Gson();
                ArrayList<String> appNames = gson.fromJson(appDetailsMap.get("appNames"), ArrayList.class);
                ArrayList<String> appPackageNames = gson.fromJson(appDetailsMap.get("appPackageNames"), ArrayList.class);
                ArrayList<String> appTimeUsages = gson.fromJson(appDetailsMap.get("appTimeUsages"), ArrayList.class);
                ArrayList<AppList> appList = new AppList().mergeAppDetailsToAppList(appNames, appPackageNames, appTimeUsages);
                Intent intent = new Intent(getApplicationContext(), AppUsageActivity.class);
                intent.putParcelableArrayListExtra("appList", appList);
                startActivity(intent);

            } catch (Exception e) {
                Log.e("mytag", e.getMessage());
            }

        } else if (remoteMessage.getData().get("msg_type").equals("appUsageRequest")) {
            try {
                AppUsageActivity aua = new AppUsageActivity();
                if (!aua.checkIfPermissionAlreadyGiven(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "App Usage Permission Required", Toast.LENGTH_SHORT).show();
                    sendMessageToParentThatChildMustEnableAppUsagePermission(remoteMessage.getData().get("from_userId"));
                } else {
                    List<ResolveInfo> pkgAppList = aua.getPackageList(getApplicationContext());
                    ArrayList<AppList> appList = aua.getAppList(pkgAppList, getApplicationContext());
                    appList = aua.attachUsageTimeToAppList(appList, getApplicationContext());
//                    Intent intent = new Intent(getApplicationContext(),AppUsageActivity.class);
//                    intent.putParcelableArrayListExtra("appList",appList);
//                    startActivity(intent);

                    sendAppUsageStateToParent(remoteMessage.getData().get("from_userId"), appList);
//                    .addOnCompleteListener(new OnCompleteListener<Object>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Object> task) {
//                            if (!task.isSuccessful()) {
//                                Exception e = task.getException();
//                                if (e instanceof FirebaseFunctionsException) {
//                                    Log.e("mytag", "in SendAppUsageStateToParent "+e.getMessage());
//                                }
//                            }
//                        }
//                    });

                }
            } catch (Exception e) {
                Log.e("mytag", e.getLocalizedMessage());
            }
        } else {
            String channel_id = remoteMessage.getData().get("msgId");
            createNotificationChannel(channel_id);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, remoteMessage.getData().get("msgId"))
                    .setSmallIcon(R.mipmap.lifesync_logo)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(remoteMessage.getData().get("msgId").hashCode(), builder.build());
        }
    }

    private void sendMessageToParentThatChildMustEnableAppUsagePermission(String toUserId) {

    }

    private void createNotificationChannel(String channel_id) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CN";
            String description = "CD";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    Task<Object> task;

    Task<Object> sendAppUsageStateToParent(String toId, final ArrayList<AppList> appList) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + toId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String token = user.getToken();
                try {
                    FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
                    Map<String, Object> data = new AppList().getAllAppDetails(appList);
                    data.put("token", token);
//                    Gson gson = new Gson();

//                    Log.d("mytag",""+appList.toString());
                    //data.put("appList",appList);
                    data.put("from_userId", "ID :"+FirebaseAuth.getInstance().getUid());
                    data.put("push", true);
//                    Log.v("mytag","ID :"+FirebaseAuth.getInstance().getUid());
                    task = mfunc.getHttpsCallable("sendAppUsageState")
                            .call(data)
                            .continueWith(new Continuation<HttpsCallableResult, Object>() {
                                @Override
                                public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                    HashMap<String, Object> ob = null;
                                    try {
                                        Log.d("mytag", "in response sendappstats");
                                    } catch (Exception e) {
                                        Log.e("mytag", e.getMessage());
                                    }
                                    return ob;
                                }
                            });
                    if (task == null) {
                        Log.d("mytag", "task is null");
                    } else {
                        task.addOnCompleteListener(new OnCompleteListener<Object>() {
                            @Override
                            public void onComplete(@NonNull Task task) {
//                                Log.d("mytag", "task is complete");
                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    Log.e("mytag", "in SendAppUsageStateToParent " + e.getMessage());
                                    if (e instanceof FirebaseFunctionsException) {
                                        Log.e("mytag", "in SendAppUsageStateToParent " + e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("mytag", e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return task;
    }
}



