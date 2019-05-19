package com.example.tryapp;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppList implements Parcelable {
    private Drawable icon;
    private String name;
    private String packageName;
    private float time;
    private static final String TAG = "mytag";

    public AppList(Drawable icon, String name, String packageName) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
        ;
    }

    public AppList(String name, String packageName, String time) {
        this.name = name;
        this.packageName = packageName;
        this.time = Float.parseFloat(time);
    }

    public AppList() {

    }

    protected AppList(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        time = in.readFloat();
    }

    public static final Creator<AppList> CREATOR = new Creator<AppList>() {
        @Override
        public AppList createFromParcel(Parcel in) {
            return new AppList(in);
        }

        @Override
        public AppList[] newArray(int size) {
            return new AppList[size];
        }
    };

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeFloat(time);
    }

    public Map<String, Object> getAllAppDetails(ArrayList<AppList> appList) {
        Map appDetailMap = new HashMap();
        try {
            ArrayList<String> appNames = new ArrayList<>();
            ArrayList<String> appPackageNames = new ArrayList<>();
            ArrayList<String> appTimeUsages = new ArrayList<>();
            ArrayList<String> appIcons = new ArrayList<>();
//            Gson gson = new Gson();
            for (AppList app : appList) {
                appNames.add(app.getName());
                appPackageNames.add(app.getPackageName());
                appTimeUsages.add(String.valueOf(app.getTime()));
//                appIcons.add(gson.toJson(app.getIcon()));
            }
            appDetailMap.put("appNames", appNames);
            appDetailMap.put("appPackageNames", appPackageNames);
            appDetailMap.put("appTimeUsages", appTimeUsages);
//            appDetailMap.put("appIcons", appIcons);
        } catch (Exception e) {
            Log.e("mytag", "in getAllAppDetails " + e.getMessage());
        }
        return appDetailMap;
    }

    public ArrayList<AppList> convertAppDetailsToAppList(ArrayList<String> appNames, ArrayList<String> appPackageNames, ArrayList<String> appTimeUsages) {
        ArrayList<AppList> appList = new ArrayList<>();
        try {
            for (int i = 0; i < appNames.size(); ++i) {
                AppList app = new AppList(appNames.get(i), appPackageNames.get(i), appTimeUsages.get(i));
                appList.add(app);
            }
        } catch (Exception e) {
            Log.e("mytag", "in convertAppDetailsToAppList " + e.getMessage());
        }
        return appList;
    }


//    public Bitmap convertDrawableToBitmap(Drawable drawable)
//    {
//        return getBitmapFromDrawable(drawable);
//    }
//
//    public Drawable convertBitmapToDrawable(Bitmap bitmap)
//    {
//        Drawable drawable = new BitmapDrawable(bitmap);
//        return drawable;
//    }
//
//    @NonNull
//    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
//        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        final Canvas canvas = new Canvas(bmp);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bmp;
//    }
}
