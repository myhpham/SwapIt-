package com.zybooks.swapit.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;
import com.zybooks.swapit.Activities.MainActivity;
import com.zybooks.swapit.Models.Posts;
import com.zybooks.swapit.R;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ViewUserProfileFragment extends Fragment {

    private static final int CHOOSE_IMAGE = 101;
    DatabaseReference databaseReference, postsRef;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference, profilePicRef;

    //path where profile pictures go
    String storagePath = "Users_ProfilePictures/";

    private ImageView profilePicture;
    private TextView TVname, TVemail, TVzip;
    private Button logoutbutton;
    private RecyclerView userprofile_recyclerview;

    FloatingActionButton fab;
    ProgressBar progressBar;
    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image for profile pic
    Uri image_uri;
    String profilePhoto, downloadUri;

    String value, uid, pValue, pName;

    HashMap postMap;

    public ViewUserProfileFragment() {

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_viewuserprofile, container, false);

        TVname = v.findViewById(R.id.userprofile_name);
        TVemail = v.findViewById(R.id.userprofile_email);
        TVzip = v.findViewById(R.id.userprofile_zip);
        profilePicture = v.findViewById(R.id.userprofile_image);
        userprofile_recyclerview = v.findViewById(R.id.userprofile_recyclerview);

        fab = v.findViewById(R.id.fab);
        logoutbutton = v.findViewById(R.id.userprofile_logout);
        progressBar = v.findViewById(R.id.profilepicprogressbar);

        pd = new ProgressDialog(getActivity());

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        postsRef = firebaseDatabase.getReference().child("Posts");
        storageReference = getInstance().getReference();
        profilePicRef = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        userprofile_recyclerview.setLayoutManager(linearLayoutManager);

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String zip = "" + ds.child("zip").getValue();
                    String image = "" + ds.child("image").getValue();

                    TVname.setText(name);
                    TVemail.setText(email);
                    TVzip.setText(zip);

                    try{
                        Picasso.get().load(image).into(profilePicture);
                    } catch (Exception e){
                        Picasso.get().load(R.drawable.ic_profilebutton).into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
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

        displayUserPosts();
    }

    private void displayUserPosts() {
        Query query = postsRef.orderByChild("uid").equalTo(user.getUid());

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();

        FirebaseRecyclerAdapter <Posts, ViewUserProfileFragment.UserPostsViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, ViewUserProfileFragment.UserPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UserPostsViewHolder userPostsViewHolder, int i, @NonNull final Posts posts) {
                userPostsViewHolder.user_post_name.setText(posts.getpTitle());
                userPostsViewHolder.user_post_description.setText(posts.getpDescr());

                try{
                    Picasso.get().load(posts.getpImage()).into(userPostsViewHolder.user_post_image);
                } catch (Exception e){
                    //
                }

                userPostsViewHolder.deletepost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String postid = posts.getpValue();
                        deletePost(postid);
                    }
                });
            }

            @NonNull
            @Override
            public ViewUserProfileFragment.UserPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userprofile_posts_view, parent, false);
                ViewUserProfileFragment.UserPostsViewHolder holder = new UserPostsViewHolder(view);
                return holder;
            }
        };
        userprofile_recyclerview.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void deletePost(String postid) {
        postsRef.child(postid).removeValue();
    }

    public static class UserPostsViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageView user_post_image;
        TextView user_post_name, user_post_description;

        ImageButton deletepost;

        public UserPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            user_post_name = view.findViewById(R.id.userprofile_post_name);
            user_post_description = view.findViewById(R.id.userprofile_post_description);
            user_post_image = view.findViewById(R.id.userprofile_post_imageview);

            deletepost = view.findViewById(R.id.delete_button);
        }
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit profile picture", "Edit name"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){ //edit profile pic
                    pd.setMessage("Updating Profile Picture");
                    profilePhoto = "image";
                    showImagePickDialog();

                } else if (which == 1){ //edit name
                    pd.setMessage("Updating Name");
                    showNameUpdateDialog("name");
                }
            }
        });

        builder.create().show();
    }

    private void showNameUpdateDialog(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap <String, Object> result = new HashMap();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            updatePostDatabase(value);
                            Toast.makeText(getActivity(), "Successfully updated", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                } else{
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();
                }
                //updatePostDatabase(value);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.create().show();
    }

    //trying to update posts
    private void updatePostDatabase(final String value) {
        uid = user.getUid();

        Query query = postsRef.orderByChild("uid").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    pValue = ds.child("pValue").getValue().toString();

                    postMap = new HashMap();
                    postMap.put("uName", value);
                    postsRef.child(pValue).updateChildren(postMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    private void showImagePickDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Upload From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){ //camera

                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    } else{
                        pickFromCamera();
                    }
                }else if (which == 1){ //gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    } else{
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted){
                        //permissions enabled
                        pickFromCamera();
                    } else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
                    }

                }
            } break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 1){
                    //Error here!
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccepted){
                        //permissions enabled
                        pickFromGallery();
                    } else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }

                }
            } break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE){
                image_uri = data.getData();
                uploadProfilePicture(image_uri);

            }
            if(requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                uploadProfilePicture(image_uri);
            }
        }


    }

    private void uploadProfilePicture(Uri uri) {
        pd.setMessage("Updating profile image...");
        pd.show();

        if(uri != null){
            profilePhoto = uri.toString();
            StorageReference filePath = profilePicRef.child(user.getUid()).child("image");
            filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        downloadUri = task.getResult().getDownloadUrl().toString();
                        pd.dismiss();
                        profilePicToDatabase();
                    }
                }
            });
        }
    }


    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            TVemail.setText(user.getEmail());
            TVname.setText(user.getDisplayName());

        } else{
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onStart() {
        checkUserStatus();
        super.onStart();

    }

    private void profilePicToDatabase() {
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    HashMap result = new HashMap();
                    result.put("image", downloadUri);

                    //updates uDp (users)
                    databaseReference.child(user.getUid()).updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                profilePhotoToDatabase(downloadUri);
                            }
                            else {
                                Toast.makeText(getActivity(), "Could not update profile photo", Toast.LENGTH_SHORT).show();
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

    private void profilePhotoToDatabase(final String downloadUri) {
        uid = user.getUid();

        Query query = postsRef.orderByChild("uid").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    pValue = ds.child("pValue").getValue().toString();

                    postMap = new HashMap();
                    postMap.put("uDp", downloadUri);
                    postsRef.child(pValue).updateChildren(postMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }
}