package com.example.tryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    private Context c;
    private ArrayList<AppList> apps;

    public MyAdapter(Context c, ArrayList<AppList> apps) {
        this.c = c;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(c).
                    inflate(R.layout.applist_view, parent, false);
        }
        AppList currentApp = apps.get(position);

        TextView tvName = convertView.findViewById(R.id.app_name);
        TextView tvPackage = convertView.findViewById(R.id.package_name);
        ImageView ivIcon = convertView.findViewById(R.id.icon);

        tvName.setText(currentApp.getName());
        tvPackage.setText(currentApp.getPackageName());
        ivIcon.setImageDrawable(currentApp.getIcon());

        return convertView;
    }
}
