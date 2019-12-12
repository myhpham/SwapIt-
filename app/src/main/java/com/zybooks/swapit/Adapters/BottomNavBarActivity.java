package com.zybooks.swapit.Adapters;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zybooks.swapit.Fragments.HomePageFragment;
import com.zybooks.swapit.Fragments.PostItemFragment;
import com.zybooks.swapit.Fragments.ViewMessagesFragment;
import com.zybooks.swapit.Fragments.ViewUserProfileFragment;
import com.zybooks.swapit.R;

public class BottomNavBarActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;
    Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav_bar);

        bottomNavView = findViewById(R.id.nav_view);
        bottomNavView.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomePageFragment()).commit();
            active = new HomePageFragment();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_profile:
                            selectedFragment = new ViewUserProfileFragment();
                            break;
                        case R.id.navigation_home:
                            selectedFragment = new HomePageFragment();
                            break;
                        case R.id.navigation_addpost:
                            selectedFragment = new PostItemFragment();
                            break;
                        case R.id.navigation_messages:
                            selectedFragment = new ViewMessagesFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

                    return false;
                }
            };
}
