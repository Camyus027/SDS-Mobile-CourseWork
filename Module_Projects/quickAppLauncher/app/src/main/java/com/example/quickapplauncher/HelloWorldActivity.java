package com.example.quickapplauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class HelloWorldActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        if(getIntent().hasExtra("key")){
            TextView tv = findViewById(R.id.helloWorldText);
            tv.setText(getIntent().getExtras().getString("key"));
        }

    }
}