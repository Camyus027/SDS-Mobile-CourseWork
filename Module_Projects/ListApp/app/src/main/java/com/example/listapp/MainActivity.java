package com.example.listapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;
    String[] descriptions;
    String[] prices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        listView = findViewById(R.id.listView);
        items = res.getStringArray(R.array.items);
        descriptions = res.getStringArray(R.array.descriptions);
        prices = res.getStringArray(R.array.prices);

        ItemAdapter itemAdapter = new ItemAdapter(this, items, descriptions, prices);

        listView.setAdapter(itemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showDetailsActivity = new Intent(getApplicationContext(), showItemDetails.class);
                showDetailsActivity.putExtra("com.example.listapp.ITEM_INDEX", i);
                startActivity(showDetailsActivity);
            }
        });

    }
}