package com.zybooks.swapit.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.zybooks.swapit.Models.User;
import com.zybooks.swapit.R;

import java.util.ArrayList;
import java.util.HashMap;

public class signUpPage extends AppCompatActivity {

    private EditText ETfullName, ETemail, ETpassword, ETzip;
    private Button signUpButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ArrayList<String> arrayList;
    ProgressBar progressBar;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        ETfullName = (EditText) findViewById(R.id.signup_Name);
        ETemail = (EditText) findViewById(R.id.signup_Email);
        ETpassword = (EditText) findViewById(R.id.signup_Password);
        ETzip = (EditText) findViewById(R.id.signup_Zipcode);
        Button signUpButton = findViewById(R.id.signup_signupbutton);
        progressBar = (ProgressBar) findViewById(R.id.signup_progressBar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = ETfullName.getText().toString().trim();
                final String email = ETemail.getText().toString().trim();
                final String password = ETpassword.getText().toString().trim();
                final String zip = ETzip.getText().toString().trim();

                if(TextUtils.isEmpty(fullName)){
                    ETfullName.setError("Please enter your full name");
                    ETfullName.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    ETemail.setError("Please enter your email");
                    ETemail.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ETemail.setError("Please enter a valid email");
                    ETemail.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    ETpassword.setError("Please enter a password");
                    ETpassword.requestFocus();
                    return;
                }

                if(password.length() < 6){
                    ETpassword.setError("Password minimum: 6 characters");
                    ETpassword.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(zip)){
                    ETzip.setError("Please enter your zip code");
                    ETzip.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                User userObj = new User(fullName,email,password,zip);
                userObj.setName(fullName);
                userObj.setEmail(email);
                userObj.setPassword(password);
                userObj.setZip(zip);

                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(signUpPage.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //------------------------
                            String userid = user.getUid();
                            String useremail = user.getEmail();
                            String userFullName;
                            String userPassword;
                            String userzip;
                            String userimg;

                            HashMap<Object,String>hashMap = new HashMap<>();
                            hashMap.put("uid",userid);
                            hashMap.put("email",email);
                            hashMap.put("name",fullName);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("zip",zip);
                            hashMap.put("password", password);
                            hashMap.put("image","");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(userid).setValue(hashMap);

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(fullName).build();
                            user.updateProfile(profileChangeRequest);

                            SharedPreferences sharedPreferences = getSharedPreferences("ENTRIES",0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("Current user", fullName);
                            editor.putString("User email", email);
                            editor.apply();

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(signUpPage.this, "Successfully signed up!", Toast.LENGTH_LONG).show();

                            finish();
                            Intent intent = new Intent(signUpPage.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else{
                            progressBar.setVisibility(View.GONE);
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(signUpPage.this, "This email is already registered.", Toast.LENGTH_LONG).show();
                            } else{
                                Toast.makeText(signUpPage.this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
            }
        });

    }
}
