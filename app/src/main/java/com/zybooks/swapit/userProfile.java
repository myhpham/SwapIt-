package com.zybooks.swapit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zybooks.swapit.ui.home.HomeFragment;

import java.io.IOException;

public class userProfile extends AppCompatActivity{
    private static final int CHOOSE_IMAGE = 101;
    public String profileImageURL;
    DatabaseReference databaseReference;

    private FirebaseAuth firebaseAuth;
    private ImageView profilePicture;
    private TextView TVname;
    private Button logoutbutton;
    private Button saveChangesbutton;
    private BottomNavigationView bottomNavView;
    Fragment active;

    Uri uriProfileImage;
    ProgressBar progressBar;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewuserprofile);

        logoutbutton = findViewById(R.id.userprofile_logout);
        saveChangesbutton = findViewById(R.id.userprofile_savephoto);
        TVname = findViewById(R.id.userprofile_name);
        profilePicture = findViewById(R.id.userprofile_image);
        progressBar = findViewById(R.id.profilepicprogressbar);
        bottomNavView = findViewById(R.id.nav_view);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();



        loadUserInformation();

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseProfilePicture();

            }
        });

        logoutbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                firebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(userProfile.this, MainActivity.class));
            }
        });


        saveChangesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewUserProfileFragment()).commit();
            active = new ViewUserProfileFragment();
        }

    }

    protected void onStart() {
        super.onStart();
        //user is not logged in
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }


    private void loadUserInformation() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences("ENTRIES", MODE_PRIVATE);
        String n = sharedPreferences.getString("Current user", "");

        if(user!=null){
            if(user.getPhotoUrl() != null){
                Glide.with(this).load(user.getPhotoUrl().toString()).load(profilePicture);
            }
            if(user.getDisplayName() != null){
                TVname.setText(user.getDisplayName());
            } else{
                TVname.setText(n);
            }
        }
    }


    //*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                profilePicture.setImageBitmap(bitmap);

                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //*
    private void uploadImageToFirebaseStorage() {
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

        if(uriProfileImage != null){
            progressBar.setVisibility(View.VISIBLE);
            storageReference.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    profileImageURL = taskSnapshot.getDownloadUrl().toString();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(userProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //*
    private void chooseProfilePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Photo"), CHOOSE_IMAGE);
    }


    //*
    private void saveUserInformation(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user.getDisplayName().isEmpty()){
            Toast.makeText(this,"Display name is empty.",Toast.LENGTH_LONG).show();
            return;
        }

        if(user!=null && profileImageURL != null){
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(user.getDisplayName()).setPhotoUri(Uri.parse(profileImageURL)).build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(userProfile.this, "Profile updated", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}