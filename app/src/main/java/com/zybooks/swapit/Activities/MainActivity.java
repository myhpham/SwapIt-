package com.zybooks.swapit.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.zybooks.swapit.Adapters.BottomNavBarActivity;
import com.zybooks.swapit.Models.ModelUser;
import com.zybooks.swapit.R;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    private Button login, signup;
    private EditText ETusername, ETpassword;
    private FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    DatabaseReference dbRef;
    List<ModelUser> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        ETusername = (EditText) findViewById(R.id.main_username);
        ETpassword = (EditText) findViewById(R.id.main_password);
        login = (Button) findViewById(R.id.main_loginButton);
        signup = (Button) findViewById(R.id.main_signupbutton);
        progressBar = (ProgressBar) findViewById(R.id.main_progressbar);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = ETusername.getText().toString().trim();
                final String password = ETpassword.getText().toString().trim();


                if(TextUtils.isEmpty(username)){
                    Toast.makeText(MainActivity.this, "Please enter your email.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(MainActivity.this, "Please enter a password.", Toast.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(MainActivity.this, BottomNavBarActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "The user does not exist. Please check if you entered the correct email/password.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent it = new Intent(MainActivity.this, signUpPage.class);
                startActivity(it);
            }
        });

    }

    // if user is already logged in, goes straight to user profile
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, BottomNavBarActivity.class));
        }
    }
}