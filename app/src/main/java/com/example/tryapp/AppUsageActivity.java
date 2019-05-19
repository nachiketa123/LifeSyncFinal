package com.example.tryapp;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class AppUsageActivity extends AppCompatActivity {
    ArrayList<AppList> appList;
    private static final String TAG = "mytag";
    public static ArrayList<String> blockAppList;
    public static Child child;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appusage);
        try {
            appList = getIntent().getParcelableArrayListExtra("appList");

        }catch (Exception e)
        {
            Log.e("mytag",e.getMessage());
        }

//        if (!checkIfPermissionAlreadyGiven(AppUsageActivity.this)) {
//            showAlertDialog();
//        }
//        List<ResolveInfo> pkgAppsList = getPackageList();
//
//        final ArrayList<AppList> appList = getAppList(pkgAppsList);

        MyAdapter myAdapter = new MyAdapter(this, appList);
        ListView myListView = findViewById(R.id.myListView);
        myListView.setAdapter(myAdapter);


//        attachUsageTimeToAppList(appList);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

//                Log.d("mytag",String.valueOf(position));
                final Dialog dialog = new Dialog(AppUsageActivity.this);
                dialog.setTitle(appList.get(position).getName());
                dialog.setContentView(R.layout.dbox_layout);
                TextView tvTime = dialog.findViewById(R.id.tvtime);
                float time = (float) appList.get(position).getTime();
                int hrs = (int) (time / 3600);
                time -= (hrs * 3600);
                int min = (int) (time / 60);
                time -= (min * 60);
                int sec = (int) time;
                tvTime.setText(hrs + "hours" + "\n" + min + getString(R.string.minutes) + "\n" + +sec + getString(R.string.seconds));
                dialog.show();
                dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                final Button lock = dialog.findViewById(R.id.lock);
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lock.getText().equals("LOCK")) {
                            lock.setText("UNLOCK");
                            String packageName = appList.get(position).getPackageName();
                            fetchBlockedAppListFromDBAndAddApp(packageName);
//                            if(blockAppList != null)
//                                Log.d(TAG, ""+blockAppList.toString());
//                            else
//                                Log.d(TAG, "null");
                        }
                        else {
                            lock.setText("LOCK");
                            String packageName = appList.get(position).getPackageName();
                            fetchBlockedAppListFromDBAndRemoveApp(packageName);
                        }
                    }
                });


            }
        });


    }

    public boolean checkIfPermissionAlreadyGiven(Context context) {
        AppOpsManager appOps;
        int mode = 50;
        try {
            appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        } catch (Exception e) {
            Log.e("mytag", e.getMessage());
        }
        return mode == MODE_ALLOWED;
    }

    public List<ResolveInfo> getPackageList(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0); //extracting all the packages
        return pkgAppsList;

    }

    public ArrayList<AppList> getAppList(List<ResolveInfo> pkgAppsList,Context context) {
        final ArrayList<AppList> appList = new ArrayList<AppList>();
        for (ResolveInfo info : pkgAppsList) {

            try {
                ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
                //...
                //get package name, icon and label from applicationInfo object
                AppList app = new AppList(applicationInfo.loadIcon(context.getPackageManager())
                        , applicationInfo.loadLabel(context.getPackageManager()).toString()
                        , applicationInfo.packageName);
                appList.add(app);

               // saveIconToFirebase(applicationInfo.loadIcon(context.getPackageManager()),app.getPackageName());
            } catch (Exception e) {
                Log.e("mytag", e.getMessage());
            }
        }
        return appList;
    }


    private void saveIconToFirebase(Drawable loadIcon,String packageName) {
       try {
           StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/icons");
           mStorageRef = mStorageRef.child("/"+packageName+".png");
//           Log.d("mytag",""+mStorageRef.getPath());

           byte[] image = null;
           if (checkIfApdaptiveDrawable(loadIcon)) {
//               Log.d("mytag", "adaptive");
               Bitmap bitmap = Bitmap.createBitmap(loadIcon.getIntrinsicWidth(), loadIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
               bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
               image = byteArrayOutputStream.toByteArray();
           } else {
               Bitmap bitmap = ((BitmapDrawable) loadIcon).getBitmap();
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
               bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
               image = byteArrayOutputStream.toByteArray();
           }
           mStorageRef.putBytes(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   Log.d("mytag", "icon saved");
               }
           });
       }catch (Exception e)
       {
          try {
              StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/icons");
              mStorageRef = mStorageRef.child("/" + packageName + ".png");
              byte[] image = null;
              Log.e("mytag", "in saving icons to firebase " + e.getMessage());
              Bitmap bitmap = ((BitmapDrawable) loadIcon).getBitmap();
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
              image = byteArrayOutputStream.toByteArray();
              mStorageRef.putBytes(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                      Log.d("mytag", "icon saved");
                  }
              });
          }catch (Exception ex)
          {
              Toast.makeText(AppUsageActivity.this,"App Icons are not available",Toast.LENGTH_SHORT).show();
          }
       }
    }

    @TargetApi(26)
    private boolean checkIfApdaptiveDrawable(Drawable loadIcon) {
        return loadIcon instanceof AdaptiveIconDrawable;
    }


    public ArrayList<AppList> attachUsageTimeToAppList(ArrayList<AppList> appList,Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        //setting app usage time duration for the apps in the applist using calendar(since when? since last month or since last year or what?)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        final long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start, end);

        for (AppList app : appList) {
            try {
                float time = (float) stats.get(app.getPackageName()).getTotalTimeInForeground() / 1000f;
                app.setTime(time);
            } catch (NullPointerException npe) {
                //  Log.e("mytag",app.getPackageName() + "\t" + npe.getMessage());
            }
        }
        return appList;
    }

    public void showAlertDialog() {
        new AlertDialog.Builder(AppUsageActivity.this)
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


    public void fetchBlockedAppListFromDBAndAddApp(final String packageName) {
        // Log.d(TAG, "fetchBlockedAppListFromDB: called");
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String type = user.getType();
                    if (type.equals("PARENT")) {
//                        String json = sp.getString("active_child","");
//                        Gson gson = new Gson();
//                        Child child = gson.fromJson(json,Child.class);
                        String locatorId = child.getChildID();
                        Log.d(TAG, "locatorId:"+locatorId);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/CHILD/" + locatorId);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    Child child = dataSnapshot.getValue(Child.class);
                                    blockAppList = child.getBlockedAppList();
                                    if(blockAppList == null)
                                    {
                                        blockAppList = new ArrayList<>();
                                    }
                                    if(!blockAppList.contains(packageName))
                                        blockAppList.add(packageName);
                                    new RealTimeDBHandler(getApplicationContext()).updateToBlockedAppListOfChild(child,blockAppList);
                                } catch (Exception e) {
                                    Log.e(TAG, "AppList-fetchAppListFromDB:" + e.getMessage());
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void fetchBlockedAppListFromDBAndRemoveApp(final String packageName) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String type = user.getType();
                    if (type.equals("PARENT")) {
//                        String json = sp.getString("active_child","");
//                        Gson gson = new Gson();
//                        Child child = gson.fromJson(json,Child.class);
                        String locatorId = child.getChildID();
                        Log.d(TAG, "locatorId:"+locatorId);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/CHILD/" + locatorId);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    Child child = dataSnapshot.getValue(Child.class);
                                    blockAppList = child.getBlockedAppList();
                                    if(blockAppList !=null )
                                        if(blockAppList.contains(packageName))
                                            blockAppList.remove(packageName);
                                    new RealTimeDBHandler(getApplicationContext()).updateToBlockedAppListOfChild(child,blockAppList);
                                } catch (Exception e) {
                                    Log.e(TAG, "AppList-fetchAppListFromDB:" + e.getMessage());
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
