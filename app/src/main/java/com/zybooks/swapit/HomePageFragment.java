package com.zybooks.swapit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HomePageFragment extends Fragment {

    private View postsView;

    //database objects
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, postRef;

    //user objects
    private String userid;

    //layout objects
    private RecyclerView homepage_recyclerView;

    //TextView profileName;

    public HomePageFragment () {
        //empty container
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        postsView = inflater.inflate(R.layout.activity_homepage, container, false);

        //---------recyclerview items---------------------------------------------------
        homepage_recyclerView = postsView.findViewById(R.id.homepage_recyclerview);
        //homepage_recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        homepage_recyclerView.setLayoutManager(linearLayoutManager);
        //---------recyclerview items---------------------------------------------------

        return postsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------database reference---------------------------
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        //------database reference---------------------------
    }

    @Override
    public void onStart() {
        super.onStart();
        displayPosts();
    }

    //post to display posts
    private void displayPosts() {

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
                        Posts.class,
                        R.layout.single_post_view,
                        PostsViewHolder.class,
                        postRef
                ) {
            @Override
            protected void populateViewHolder(PostsViewHolder postsViewHolder, Posts posts, int i) {
                postsViewHolder.setuName(posts.getuName());
                postsViewHolder.setuDp(posts.getuDp());
                postsViewHolder.setpTitle(posts.getpTitle());
                postsViewHolder.setpImage(posts.getpImage());
                postsViewHolder.setpDescr(posts.getpDescr());
                postsViewHolder.setpId(posts.getpId());
            }
        };
        homepage_recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    //class for RecyclerAdapter
    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View view;

        //TextView profileName_textView, postTime_textView, postItemName_textView, postDescr_textView;
        //ImageView profileImageView, postImageView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            /*
            profileName_textView = view.findViewById(R.id.singlepost_sellername_textview);
            postTime_textView = view.findViewById(R.id.singlepost_time_textView);
            postItemName_textView = view.findViewById(R.id.singlepost_itemname_textview);

            profileImageView = view.findViewById(R.id.singlepost_profilepic_imageview);
            postImageView = view.findViewById(R.id.singlepost_imageView);
            */
        }

        //setuid

        public void setuName(String name) {
            TextView profileName_textView = view.findViewById(R.id.singlepost_sellername_textview);
            profileName_textView.setText(name);
        }

        //setuEmail

        public void setuDp(String profileImg) {
            ImageView profileImageView = view.findViewById(R.id.singlepost_profilepic_imageview);

            try{
                Picasso.get().load(profileImg).into(profileImageView);
            } catch (Exception e){
                Picasso.get().load(R.drawable.ic_profilebutton).into(profileImageView);
            }
            //Picasso.get().load(profileImg).into(profileImageView);
        }

        public void setpTitle(String postTitle) {
            TextView postItemName_textView = view.findViewById(R.id.singlepost_itemname_textview);
            postItemName_textView.setText(postTitle);
        }

        public void setpImage(String postImg) {
            ImageView postImageView = view.findViewById(R.id.singlepost_imageView);
            try{
                Picasso.get().load(postImg).into(postImageView);
            } catch (Exception e){
                Picasso.get().load(R.drawable.ic_profilebutton).into(postImageView);
            }
            //Picasso.get().load(postImg).into(postImageView);
        }

        public void setpDescr (String postDescr) {
            TextView postDescription_textView = view.findViewById(R.id.singlepost_description_textview);
            postDescription_textView.setText(postDescr);
        }

        public void setpId(String dateAndTime) {
            TextView postTime_textView = view.findViewById(R.id.singlepost_time_textView);
            postTime_textView.setText("On " + dateAndTime);
        }
    }

}
