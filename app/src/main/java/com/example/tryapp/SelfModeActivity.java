package com.example.tryapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelfModeActivity extends AppCompatActivity {

    private Button sendSOS, getAppUsage;
    private ProgressBar progressBar;
    private ArrayList<Child> childList;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_self_mode);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            childList = new ArrayList<>();
            progressBar = findViewById(R.id.pb);
            progressBar.setVisibility(View.GONE);
            getAppUsage = findViewById(R.id.button);
            sendSOS = findViewById(R.id.button4);
            sendSOS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    sendSOS();
                }
            });

            getAppUsage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (dialog == null)
                        fetchAppUsageFromChild(FirebaseAuth.getInstance().getUid());
                    else
                        dialog.show();
                }
            });
        } catch (Exception e) {
            Log.d("mytag", e.getMessage());
        }
    }

    public void showChildList(ArrayList<Child> childList) {
        try {
            progressBar.setVisibility(View.GONE);
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dbox_child_list_layout);
            dialog.setTitle("Children List");
            LinearLayout linearLayout = dialog.findViewById(R.id.linear_layout);
            linearLayout.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(5, 5, 5, 5);
            for (Child child : childList) {
                Button button = new Button(this);
                button.setId(childList.indexOf(child));
                button.setText(child.getName());
                button.setGravity(Gravity.CENTER_HORIZONTAL);
                button.setOnClickListener(new OnClickHandler(child));
                linearLayout.addView(button, lp);
            }
            //Button button = new Button(this);
            dialog.show();
        } catch (Exception e) {
            Log.e("mytag", e.getMessage());
        }
    }

    public void updateChildList(final MyCallback myCallback, final ArrayList<String> childIdList) {
        for (final String childId : childIdList) {
            if (childId != null) {
                Log.i("mytag", "" + childId);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/CHILD/" + childId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Child child = dataSnapshot.getValue(Child.class);
                        childList.add(child);
                        Log.i("mytag", child.getName() + " added");
                        myCallback.onCompleteUpdateChildList(childList, childIdList.indexOf(childId) + 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });

            }
        }
    }

    public void fetchAppUsageFromChild(String uid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                final String parentId = user.getLocatorId();
                Log.d("mytag", "" + parentId);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/PARENT/" + parentId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            final Parent parent = dataSnapshot.getValue(Parent.class);
                            updateChildList(new MyCallback() {

                                @Override
                                public void onCompleteUpdateChildList(ArrayList<Child> childList, int count) {
                                    if (parent.getChildID().size() == count) {
                                        showChildList(childList);
                                    }
                                }
                            }, parent.getChildID());

                            Log.e("mytag", "getChildAppUsage");
                        } catch (Exception e) {
                            Log.d("mytag", e.getMessage());
                        }
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
    }

    public void sendSOS() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + FirebaseAuth.getInstance().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String type = user.getType();

                //as we know sos sender will be CHILD
                String childId = user.getLocatorId();

                //but in case type is parent
                if (type.equals("PARENT")) {
                    Toast.makeText(getApplicationContext(), "Parent are not allowed to generate any SOS", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type.equals("CHILD")) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/CHILD/" + childId);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            try {
                                Child child = dataSnapshot.getValue(Child.class);
                                final String parentID = child.getParentID();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/PARENT/" + parentID);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            final String fromId = FirebaseAuth.getInstance().getUid(), fromType = "CHILD", toType = "PARENT";
                                            HashMap<String, Object> parent = (HashMap<String, Object>) dataSnapshot.getValue();
                                            final String toId = parent.get("userID").toString();
                                            final Dialog dialog = new Dialog(SelfModeActivity.this);
                                            dialog.setTitle("SOS");
                                            dialog.setContentView(R.layout.dbox_sos_message);
                                            dialog.show();
                                            progressBar.setVisibility(View.GONE);
                                            dialog.findViewById(R.id.sos_msg_send_button).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    EditText et = dialog.findViewById(R.id.et_sos_msg);
                                                    String msg = et.getText().toString().trim();
                                                    SOS sos = new SOS(fromId, toId, fromType, toType, msg);
                                                    new RealTimeDBHandler(SelfModeActivity.this).onCreateReference(sos);
                                                    dialog.dismiss();
                                                    Log.d("mytag", "i sent a msg successfully to " + toId);
                                                }
                                            });
                                        } catch (Exception e) {
                                            Log.e("mytag", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception e) {
                                Log.e("mytag", e.getMessage());
                                ;
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Neither Parent Nor Child", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("mytag", databaseError.getMessage());
            }

        });

    }

    interface MyCallback {
        void onCompleteUpdateChildList(ArrayList<Child> childList, int count);
    }

    class OnClickHandler implements View.OnClickListener {

        Child child;

        OnClickHandler() {

        }

        OnClickHandler(Child child) {
            this.child = child;
        }

        @Override
        public void onClick(View v) {
            dialog.dismiss();
            if (this.child == null) {
                Toast.makeText(getApplicationContext(), "No child selected", Toast.LENGTH_SHORT).show();
                return;
            }

//            SharedPreferences sp = getApplicationContext().getSharedPreferences("MY_SHARED_PREFS", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sp.edit();
//            Gson gson = new Gson();
//            String childString = gson.toJson(child);
//            editor.putString("active_child",childString);
//            editor.apply();
//            Log.d("mytag",String.valueOf(v.getId()));
            AppUsageActivity.child = child;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/USERS/" + child.getUserID());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String token = user.getToken();
                    requestAppUsageState(token).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                if (e instanceof FirebaseFunctionsException) {
                                    Log.e("mytag", e.getMessage());
                                }
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    Task<Object> requestAppUsageState(String token) {
        try {
//            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                @Override
//                public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                    if(task.isSuccessful())
//                    {
//                        String token = task.getResult().getToken();
//                        Log.d("mytag",token);
//                    }
//                }
//            });
            FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("from_userId", FirebaseAuth.getInstance().getUid());
            data.put("push", true);
            return mfunc.getHttpsCallable("requestAppUsageState")
                    .call(data)
                    .continueWith(new Continuation<HttpsCallableResult, Object>() {
                        @Override
                        public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                            HashMap<String, Object> ob = null;
                            try {

                            } catch (Exception e) {
                                Log.e("mytag", e.getMessage());
                            }
                            return ob;
                        }
                    });
        } catch (Exception e) {
            Log.e("mytag", e.getMessage());
        }
        return null;
    }
}

