package com.zybooks.swapit;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class PostItemFragment extends Fragment {

    private EditText postTitle;
    private EditText postDesc;
    private ImageView postImage;
    private Button postButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDb, postsDb;
    private StorageReference PostItemStorageRef;
    ActionBar actionBar;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //user info
    private String name, email, uid, dp, mCurrentPhotoPath;

    //post info
    private String saveCurrentDate, saveCurrentTime, postDateAndTime, downloadUri;
    private String itemName, itemDescription;

    Uri image_uri = null;
    ProgressDialog pd;

    private final String TAG = "PostItemFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_postitem, container, false);


        postTitle = v.findViewById(R.id.postItem_title);
        postDesc = v.findViewById(R.id.postItem_description);
        postImage = v.findViewById(R.id.postItem_image);
        postButton = v.findViewById(R.id.postItem_button);

        /*
        getActivity().setActionBar(null);
        getActivity().getActionBar().setTitle("Add post");
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);*/

        //-----Firebase Storage Reference-----------
        //PostItemStorageRef = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        PostItemStorageRef = FirebaseStorage.getInstance().getReference();
        postsDb = FirebaseDatabase.getInstance().getReference().child("Posts");
        //-----Firebase Storage Reference-----------

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        userDb = FirebaseDatabase.getInstance().getReference("Users");  //get USERS database reference
        Query query = userDb.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //storePostInformation();

        postImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                storePostInformation();
            }
        });

        return v;
    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("CHOOSE IMAGE FROM");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){ //camera clicked
                    pickFromCamera();
                }
                if(which==1){ //gallery clicked
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    //take image with camera
    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void storePostInformation() {
        //now are global variables
        itemName = postTitle.getText().toString().trim();
        itemDescription = postDesc.getText().toString().trim();

        if(TextUtils.isEmpty(itemName)){
            Toast.makeText(getActivity(), "Please enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(itemDescription)){
            Toast.makeText(getActivity(), "Please enter description", Toast.LENGTH_SHORT).show();
            return;
        }
        if(image_uri == null){ // posting without image
            uploadData(itemName, itemDescription, null);
        }
        else{
            uploadData(itemName, itemDescription, image_uri);
        }
    }

    private void uploadData(final String title, final String description, Uri imageUri) {
        Log.d(TAG, "Attempting to publish post...");

        pd.setMessage("Publishing post...");
        pd.show();

        //---------------Post Date and Time------------------------------------

        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("DD-MM-YYYY");
        saveCurrentDate = currentDate.format(date.getTime());

        Calendar time = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:MM:SS");
        saveCurrentTime = currentTime.format(time.getTime());

        //global string variable for date and time
        postDateAndTime = saveCurrentDate + " " + saveCurrentTime;

        //---------------Post Date and Time------------------------------------

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "" + timestamp;

        if(imageUri != null){
            //mCurrentPhotoPath = imageUri.getLastPathSegment();
            mCurrentPhotoPath = imageUri.toString();
            StorageReference filePath = PostItemStorageRef.child("Posts").child(mCurrentPhotoPath + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        downloadUri = task.getResult().getDownloadUrl().toString();
                        pd.dismiss();
                        saveToDatabase();
                    }
                    else {
                        Toast.makeText(getActivity(), "Could not upload post.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&storageAccepted){
                        pickFromCamera();
                    } else{
                        Toast.makeText(getActivity(), "Camera & Storage permissions needed", Toast.LENGTH_SHORT).show();
                    }
                } else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(getActivity(), "Storage permission needed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    /*
    public boolean onSupportNavigateUp(){
        getActivity().onBackPressed();
        return super.onSupportNavigateUp;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                postImage.setImageURI(image_uri);
            } else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                postImage.setImageURI(image_uri);
            }
        }
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            email = user.getEmail();
            uid = user.getUid();
        } else{
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    private void saveToDatabase(){
        userDb.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    name = dataSnapshot.child("name").getValue().toString();
                    email = dataSnapshot.child("email").getValue().toString();
                    //dp = dataSnapshot.child("image").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", uid);
                    postsMap.put("uName", name);
                    postsMap.put("uEmail", email);
                    postsMap.put("uDp", dp);
                    postsMap.put("pId", postDateAndTime);
                    postsMap.put("pTitle", itemName);
                    postsMap.put("pDescr", itemDescription);
                    postsMap.put("pImage", downloadUri);

                    postsDb.child(uid+ " " + itemName + " " + postDateAndTime).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Post uploaded", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), "Could not upload post", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do nothing
            }
        });
    }
}