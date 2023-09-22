package com.example.quickapplauncher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button helloWorldActivityBtn = findViewById(R.id.helloWordBtn);
        helloWorldActivityBtn.setOnClickListener(view -> {
            Intent startIntent = new Intent(getApplicationContext(), HelloWorldActivity.class);
            // Pass info from one screen to another
            startIntent.putExtra("key","Patata");
            startActivity(startIntent);
        });

        // Launch an app outside ours

        Button googleSearchButton = findViewById(R.id.googleSearchButton);

        googleSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String googleUrl = "https://www.google.com";
                Uri webAddress = Uri.parse(googleUrl);

                Intent goToGoogle = new Intent(Intent.ACTION_VIEW, webAddress);

                if(goToGoogle.resolveActivity(getPackageManager()) != null){
                    //If I have receive a positive answer:
                    startActivity(goToGoogle);
                }
            }
        });

    }
}