package com.zybooks.swapit.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.zybooks.swapit.Fragments.ViewMessagesFragment;
import com.zybooks.swapit.R;

public class ViewMessages extends AppCompatActivity {

    Fragment active;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openmessages);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewMessagesFragment()).commit();
            active = new ViewMessagesFragment();
        }
    }
}
