package com.example.tryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN=
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$");
    private static String USERID ;

    Button btn1;
    private EditText etn,etp,ete;
    private EditText edt,edt1;
    private TextView textView;
    RadioGroup radioGroup;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    ProgressDialog progressDialog;
    private CallbackManager mCallbackManager;
    private EditText et_mobile;
    private String type,token;
    private static final String TAG="facelog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            mAuth = FirebaseAuth.getInstance();
            btn1 = findViewById(R.id.rbtn);
            etn = findViewById(R.id.name);
            ete = findViewById(R.id.email);
            etp = findViewById(R.id.password);
            et_mobile = findViewById(R.id.et_mobile);
            radioGroup=findViewById(R.id.radiogroup);
            progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("User Registration");
            progressDialog.setMessage("Registering...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            edt=findViewById(R.id.editText15);
            edt.setEnabled(false);
            edt1=findViewById(R.id.editText20);
            edt1.setEnabled(false);
            edt.setVisibility(View.GONE);
            edt1.setVisibility(View.GONE);
            textView = findViewById(R.id.logintext);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLoginActivity();
                }
            });
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    registeruser();
                }
            });
            mAuth = FirebaseAuth.getInstance();
            // Initialize Facebook Login button
            mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = findViewById(R.id.fbbtn);
            loginButton.setReadPermissions("email", "public_profile");
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                    // ...
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                    // ...
                }
            });

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.Parent){
                        type = "PARENT";
//                    Log.d("mytag","parent");
                    }
                    if(checkedId==R.id.Child){
                        type = "CHILD";
//                    Log.d("mytag","child");
                    }
                }
            });
// ...


        }catch (Exception e)
        {
            Log.e("mytag",e.getMessage());
        }
    }
//     @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser!=null) {
//            updateUI(currentUser);
//            Intent intent=new Intent(this,HomeActivity.class);
//            startActivity(intent);
//        }
//    }

    private void updateUI(FirebaseUser user) {
        Toast.makeText(RegisterActivity.this,"You are logged in",Toast.LENGTH_LONG).show();
        Intent intent=new Intent(RegisterActivity.this,HomeActivity.class);
        startActivity(intent);
        //finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void openLoginActivity(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void registeruser(){
        progressDialog.show();
        final String email,password,name;
        email=ete.getText().toString().trim();
        password=etp.getText().toString().trim();
        name=etn.getText().toString().trim();

        if(name.isEmpty() && email.isEmpty() && password.isEmpty()){
            progressDialog.dismiss();
            etn.setError("Field can't be empty");
            ete.setError("Field can't be empty");
            etp.setError("Field can't be empty");
        }
        else if(name.isEmpty()){
            progressDialog.dismiss();
            etn.setError("Field can't be empty");
        }
        else if(email.isEmpty()){
            progressDialog.dismiss();
            ete.setError("Field can't be empty");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            progressDialog.dismiss();
            ete.setError("Please enter a valid email address");
        }
        else if(password.isEmpty()){
            progressDialog.dismiss();
            etp.setError("Field can't be empty");
        }
        else if(!PASSWORD_PATTERN.matcher(password).matches()){
            progressDialog.dismiss();
            etp.setError("Password too weak");
            edt.setVisibility(View.VISIBLE);
            edt1.setVisibility(View.VISIBLE);
        }
        else if(type == null)
        {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"specify PARENT OR CHILD",Toast.LENGTH_SHORT).show();
        }
        else{
            try {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        progressDialog.dismiss();
                                        getDataAndSaveItToDB();
                                        Toast.makeText(getApplicationContext(), "Registeration Successfull, Please check your Email for verification", Toast.LENGTH_LONG).show();
                                        etn.setText("");
                                        ete.setText("");
                                        etp.setText("");
                                    }catch (Exception e)
                                    {
                                        Log.e("mytag",e.getMessage());
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Registeration failed, Email already exists", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            catch (Exception e){
                Log.e("mytag",e.getMessage());
            }
        }
    }

    String returnId=null;
    private void getDataAndSaveItToDB() {
        String name,email,mobile;

        name = etn.getText().toString().trim();
        mobile = et_mobile.getText().toString().trim();
        email = ete.getText().toString().trim();

        if(type.equals("PARENT"))
        {
            Parent parent = new Parent(FirebaseAuth.getInstance().getUid(),name,mobile,email);
            returnId = new RealTimeDBHandler(RegisterActivity.this).onCreateReference(parent);
        }
        if(type.equals("CHILD"))
        {
            Child child = new Child(FirebaseAuth.getInstance().getUid(),name,mobile,email);
            returnId =  new RealTimeDBHandler(RegisterActivity.this).onCreateReference(child);
        }
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful())
                        {
                            token = task.getResult().getToken();
                            User user = new User(type,returnId,token);
                            new RealTimeDBHandler(RegisterActivity.this).onCreateReference(user);
                        }
                        else
                        {
                            Log.d("mytag","token generation failed");
                        }
                    }
                });

    }

    public static String getUserId()
    {
        return RegisterActivity.USERID;
    }
}
