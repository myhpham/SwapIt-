package com.zybooks.swapit;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class homePage extends AppCompatActivity {

    TextView homepage_textView;
    GridView homepage_gridView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        homepage_gridView = findViewById(R.id.homepage_gridview);
    }
}
