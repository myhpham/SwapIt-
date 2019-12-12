package com.zybooks.swapit.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zybooks.swapit.Models.Posts;
import com.zybooks.swapit.R;
import com.zybooks.swapit.Activities.ViewSellerProfile;
import com.zybooks.swapit.Activities.chatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class HomePageFragment extends Fragment {

    private final String TAG = "HomePageFragment";

    private View postsView;
    static Context context;

    //database objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef, postRef;


    //user objects
    String userZip;

    //layout objects
    private RecyclerView homepage_recyclerView;

    public HomePageFragment () {
        //empty container
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        postsView = inflater.inflate(R.layout.activity_homepage, container, false);

        //---------recyclerview items---------------------------------------------------
        homepage_recyclerView = postsView.findViewById(R.id.homepage_recyclerview);
        //homepage_recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        //linearLayoutManager.setStackFromEnd(true);
        homepage_recyclerView.setLayoutManager(linearLayoutManager);
        //---------recyclerview items---------------------------------------------------

        return postsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------database reference---------------------------
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        user = firebaseAuth.getCurrentUser();
        //------database reference---------------------------
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserZip();
        //displayPosts();
    }

    private void getUserZip() {
        String userid = user.getUid();

        userRef.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userZip = dataSnapshot.child("zip").getValue().toString();
                    displayPosts(userZip);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    private void displayPosts(String zip) {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        String currentDate = dateFormat.format(date.getTime());

        //set query to filter posts by zip code
        Query query = postRef.orderByChild("pZip").equalTo(zip);

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post_view, parent, false);
                PostsViewHolder holder = new PostsViewHolder(view);
                return holder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, final int position, @NonNull final Posts posts) {
                postsViewHolder.profileName_textView.setText(posts.getuName());
                postsViewHolder.postItemName_textView.setText(posts.getpTitle());
                postsViewHolder.postItemZip_textView.setText(posts.getpZip());
                postsViewHolder.postDescription_textView.setText(posts.getpDescr());
                postsViewHolder.postTime_textView.setText(posts.getpId());
                posts.setUid(posts.getUid());


                //profile pic
                try{
                    Picasso.get().load(posts.getuDp()).into(postsViewHolder.profileImageView);
                } catch (Exception e){
                    Picasso.get().load(R.drawable.ic_profilebutton).into(postsViewHolder.profileImageView);
                }

                //post image view
                try{
                    Picasso.get().load(posts.getpImage()).into(postsViewHolder.postImageView);
                } catch (Exception e){
                    Picasso.get().load(R.drawable.ic_profilebutton).into(postsViewHolder.postImageView);
                }

                //message button
                postsViewHolder.messageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String seller = posts.getUid();

                        Intent intent = new Intent(context, chatActivity.class);
                        intent.putExtra("SELLER_ID", seller);
                        startActivity(intent);
                    }
                });

                //seller profile button
                postsViewHolder.profileName_textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String seller = posts.getUid();

                        Intent intent = new Intent(context, ViewSellerProfile.class);
                        intent.putExtra("SELLER_ID", seller);
                        startActivity(intent);
                    }
                });

                //share post button
                postsViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable)postsViewHolder.postImageView.getDrawable();
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        sharePost(posts.getpTitle(), posts.getpDescr(), bitmap);
                    }
                });

            }
        };
        homepage_recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void sharePost(String getpTitle, String getpDescr, Bitmap bitmap) {
        //concatenate
        String shareBody = "Check out this " + "\"" + getpTitle + "\"" + " out on the SwapIt app!";


        //save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.putExtra(Intent.EXTRA_TEXT, shareBody);
        i.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        i.setType("image/png");
        context.startActivity(Intent.createChooser(i, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdirs(); // create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.zybooks.swapit.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView profileName_textView, postItemName_textView, postItemZip_textView, postDescription_textView, postTime_textView;
        ImageView profileImageView, postImageView;
        ImageButton messageButton, shareButton;

        public PostsViewHolder(@NonNull final View itemView) {
            super(itemView);
            view = itemView;

            profileName_textView = view.findViewById(R.id.singlepost_sellername_textview);
            postItemName_textView = view.findViewById(R.id.singlepost_itemname_textview);
            postItemZip_textView = view.findViewById(R.id.singlepost_itemzip_textview);
            postDescription_textView = view.findViewById(R.id.singlepost_description_textview);
            postTime_textView = view.findViewById(R.id.singlepost_time_textView);

            postImageView = view.findViewById(R.id.singlepost_imageView);
            profileImageView = view.findViewById(R.id.singlepost_profilepic_imageview);

            messageButton = view.findViewById(R.id.singlepost_message_button);
            shareButton = view.findViewById(R.id.singlepost_share_button);
        }
    }
}
