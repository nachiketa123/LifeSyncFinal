package com.example.tryapp;

import android.graphics.drawable.Drawable;

public class AppList {
    private Drawable icon;
    private String name;
    private String packageName;
    private float time;

    public AppList(Drawable icon, String name,String packageName) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;;
    }

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
}
