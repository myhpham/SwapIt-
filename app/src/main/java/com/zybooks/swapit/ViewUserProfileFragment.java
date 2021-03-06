package com.zybooks.swapit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ViewUserProfileFragment extends Fragment {

    //************************************
    private static final int CHOOSE_IMAGE = 101;
    private String profileImageURL;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser user;

    private FirebaseAuth firebaseAuth;
    private ImageView profilePicture;
    private TextView TVname;
    private TextView TVemail;
    private Button logoutbutton;
    private Button saveChangesbutton;

    private Uri uriProfileImage;
    private ProgressBar progressBar;
    //************************************

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_viewuserprofile, container, false);

        //************************************
        //layout items---------------------------
        logoutbutton = v.findViewById(R.id.userprofile_logout);
        saveChangesbutton = v.findViewById(R.id.userprofile_savephoto);
        TVname = v.findViewById(R.id.userprofile_name);
        TVemail = v.findViewById(R.id.userprofile_email);
        profilePicture = v.findViewById(R.id.userprofile_image);
        progressBar = v.findViewById(R.id.profilepicprogressbar);
        //layout items---------------------------

        //firebase items---------------------------
        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
        //firebase items---------------------------

        //get data of current user from firebase------------------
        firebaseAuth.getCurrentUser();
        //get data of current user from firebase------------------

        loadUserInformation();              //call function to load user's information

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //function to choose profile picture
                chooseProfilePicture();

            }
        });

        //logout functionality
        logoutbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                firebaseAuth.getInstance().signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });

        //button that calls saves changes (to user profile onto database)
        saveChangesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
        //************************************

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //userProfile u = new userProfile();
        //u.loadUserInformation();
    }

    //************************************

    //*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriProfileImage);
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
                getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

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
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //loads user information
    private void loadUserInformation() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ENTRIES", getActivity().MODE_PRIVATE);
        String n = sharedPreferences.getString("Current user", "");
        String e = sharedPreferences.getString("User email","");

        if(user!=null){
            if(user.getPhotoUrl() != null){
                Glide.with(this).load(user.getPhotoUrl().toString()).load(profilePicture);
            }
            if(user.getDisplayName() != null){
                TVname.setText(user.getDisplayName());
                TVemail.setText(user.getEmail());
            } else{
                TVname.setText(n);
                TVemail.setText(e);
                profilePicture.setImageURI(Uri.parse(profileImageURL));
            }
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
            Toast.makeText(getActivity(),"Display name is empty.",Toast.LENGTH_LONG).show();
            return;
        }

        if(user!=null && profileImageURL != null){
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(user.getDisplayName()).setPhotoUri(Uri.parse(profileImageURL)).build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //************************************
}
