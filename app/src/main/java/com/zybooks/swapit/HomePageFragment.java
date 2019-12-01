package com.zybooks.swapit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePageFragment extends Fragment {

    //database objects
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    //user objects
    private String userid;

    RecyclerView homepage_recyclerView;
    FrameLayout homepage_fragmentContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_homepage, container, false);

        homepage_recyclerView = v.findViewById(R.id.homepage_recyclerview);
        homepage_fragmentContainer = v.findViewById(R.id.fragment_container);

        //------database reference---------------------------
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //------database reference---------------------------

        userRef.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do nothing
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
