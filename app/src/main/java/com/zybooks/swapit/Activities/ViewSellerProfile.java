package com.zybooks.swapit.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zybooks.swapit.Models.Posts;
import com.zybooks.swapit.R;

public class ViewSellerProfile extends AppCompatActivity {

    static final String TAG = "ViewSellerProfile";

    //layout items
    RecyclerView seller_recyclerView;
    TextView sellername_textview;
    ImageView sellerimage_imageview;
    ImageButton message_seller_button;

    //firebase items
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, postRef;

    FirebaseRecyclerAdapter <Posts, SellerViewHolder> firebaseRecyclerAdapter;

    //intent extra
    String sellerid;

    //seller info items
    String sellerProfilePic, sellerName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewseller);

        seller_recyclerView = findViewById(R.id.sellerprofile_recyclerview);
        sellerimage_imageview = findViewById(R.id.sellerprofile_image);
        sellername_textview = findViewById(R.id.sellerprofile_name);
        message_seller_button = findViewById(R.id.messageseller_button);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewSellerProfile.this);
        seller_recyclerView.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        Intent intent = getIntent();
        sellerid = intent.getStringExtra("SELLER_ID");

        message_seller_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ViewSellerProfile.this, chatActivity.class);
                it.putExtra("SELLER_ID", sellerid);
                startActivity(it);
            }
        });

        getSellerInformation(sellerid);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = postRef.orderByChild("uid").equalTo(sellerid);
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, SellerViewHolder>(options) {
            @NonNull
            @Override
            public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userpage_view, parent, false);
                SellerViewHolder holder = new SellerViewHolder(view);
                return holder;
            }

            @Override
            protected void onBindViewHolder(@NonNull SellerViewHolder sellerViewHolder, int i, @NonNull Posts posts) {
                sellerViewHolder.seller_post_name.setText(posts.getpTitle());
                sellerViewHolder.seller_post_description.setText(posts.getpDescr());

                try{
                    Picasso.get().load(posts.getpImage()).into(sellerViewHolder.seller_post_image);
                } catch (Exception e){
                    //Picasso.get().load(R.drawable.ic_profilebutton).into(postsViewHolder.profileImageView);
                }
            }
        };
        seller_recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void getSellerInformation(final String sellerid) {

        userRef.child(sellerid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    sellerProfilePic = dataSnapshot.child("image").getValue().toString();
                    sellerName = dataSnapshot.child("name").getValue().toString();

                    sellername_textview.setText(sellerName);

                    try {
                        Picasso.get().load(sellerProfilePic).into(sellerimage_imageview);
                    } catch (Exception e) {
                        //
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });

    }

    public static class SellerViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageView seller_post_image;
        TextView seller_post_name, seller_post_description;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            seller_post_name = view.findViewById(R.id.userprofile_post_name);
            seller_post_description = view.findViewById(R.id.userprofile_post_description);
            seller_post_image = view.findViewById(R.id.user_post_image);
        }
    }
}
