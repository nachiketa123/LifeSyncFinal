package com.example.tryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    private Context c;
    private static final String TAG = "mytag";
    private ArrayList<AppList> apps;
    private Handler handler;

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
        //ivIcon.setImageDrawable(currentApp.getIcon());
//        IconDownloader iconDownloader = new IconDownloader(ivIcon);
//        iconDownloader.execute(currentApp.getPackageName());
        return convertView;
    }

    class IconDownloader extends AsyncTask<String,Void,byte[]>
    {
        private ImageView imageView;

        public IconDownloader(ImageView imageView) {
            this.imageView = imageView;
        }

        final long MAX_ONE_MB = 1024 * 1024;
        byte []  byteData = null;
        @Override
        protected byte[] doInBackground(String... strings) {
            final String packageName = strings[0];
            StorageReference ref = FirebaseStorage.getInstance().getReference();
            ref = ref.child("/icons/" + packageName + ".png");
            ref.getBytes(MAX_ONE_MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                        byteData = bytes;
                        Log.d(TAG, "icon for "+packageName+" downloaded");
                }
            });
            while(byteData == null)
            {

            }
            return byteData;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "onPostExecute: icon set");
        }
    }


//    class ThreadForSettingIcon extends Thread {
//
//        private ImageView imageView;
//        private String packageName;
//        final long MAX_ONE_MB = 1024 * 1024;
//        public boolean found;
//        byte[] byteData;
//
//
//        ThreadForSettingIcon() {
//
//        }

//        ThreadForSettingIcon(ImageView imageView, String packageName) {
//            this.imageView = imageView;
//            this.packageName = packageName;
//            this.byteData = null;
//        }
//
//        @Override
//        public synchronized void run() {
//            if (Looper.myLooper() == null)
//                Looper.prepare();
//            Log.d(TAG, Thread.currentThread().getName() + " thread started");
//            StorageReference ref = FirebaseStorage.getInstance().getReference();
//            ref = ref.child("/icons/" + packageName + ".png");
//            this.found = false;
////            while(true) {
//            ref.getBytes(MAX_ONE_MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                @Override
//                public void onSuccess(byte[] bytes) {
////                        if(byteData == null) {
////                            Log.d("mytag", "icon for " + packageName + " found");
////                            byteData = bytes;
////                            Bitmap bitmap = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
////                            handler = new MyHandlerToUpdateIcon(imageView, bitmap, packageName);
////                            handler.sendEmptyMessage(0);
////                            Log.d(TAG, Thread.currentThread().getName() + " thread end");
////                        }
////                        else
////                        {
////                            Log.d("mytag",  packageName +"bytes not null");
////                        }
//                }
//            });
//        }
//
//    }


//    class MyHandlerToUpdateIcon extends Handler
//    {
//        private ImageView imageView;
//        private Bitmap bitmap;
//        private String packageName;
//        public MyHandlerToUpdateIcon() {
//            super();
//        }
//
//        public MyHandlerToUpdateIcon(ImageView imageView,Bitmap bitmap,String packageName)
//        {
//            this.imageView = imageView;
//            this.bitmap = bitmap;
//            this.packageName = packageName;
//        }
//
//        @Override
//        public synchronized void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Log.d(TAG, "in handleMessage ");
//            if(msg.what == 0)
//            {
//                imageView.setImageBitmap(bitmap);
//                Log.d(TAG, packageName +" image icon set ");
//            }
//        }
//    }
}
