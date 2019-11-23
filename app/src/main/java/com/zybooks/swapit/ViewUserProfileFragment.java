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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
<<<<<<< Updated upstream
import java.util.UUID;
=======
import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;
>>>>>>> Stashed changes

public class ViewUserProfileFragment extends Fragment {

    //************************************
    private static final int CHOOSE_IMAGE = 101;
    public String profileImageURL;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser user;

    private FirebaseAuth firebaseAuth;
    private ImageView profilePicture;
    private TextView TVname;
<<<<<<< Updated upstream
    private Button logoutbutton, saveChangesbutton;
=======
    private TextView TVemail;
    private Button logoutbutton;
    private Button saveChangesbutton;
>>>>>>> Stashed changes

    Uri uriProfileImage;
    ProgressBar progressBar;
    //************************************

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_viewuserprofile, container, false);

        //************************************
        logoutbutton = v.findViewById(R.id.userprofile_logout);
        saveChangesbutton = v.findViewById(R.id.userprofile_savephoto);
        TVname = v.findViewById(R.id.userprofile_name);
<<<<<<< Updated upstream
=======
        TVemail = v.findViewById(R.id.userprofile_email);

>>>>>>> Stashed changes
        profilePicture = v.findViewById(R.id.userprofile_image);
        progressBar = v.findViewById(R.id.profilepicprogressbar);


        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        firebaseAuth.getCurrentUser();

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
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });


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

    //displays user info
    private void loadUserInformation() {
        //TO DO HERE!

        databaseReference = FirebaseDatabase.getInstance().getReference().child("profilepics/");

        FirebaseUser user = firebaseAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ENTRIES", getActivity().MODE_PRIVATE);
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


    private void chooseProfilePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Photo"), CHOOSE_IMAGE);
    }


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
