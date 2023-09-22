package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // We indicated that the add button in java is an object of the view with that id.
        Button addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(view -> {
            EditText firstNumber = findViewById(R.id.firstNumber);
            EditText secondNumber = findViewById(R.id.secondNumber);
            TextView result = findViewById(R.id.result);

            int num1 = Integer.parseInt(firstNumber.getText().toString());
            int num2 = Integer.parseInt(secondNumber.getText().toString());

            int additionResult = num1 + num2;

            result.setText(additionResult + "");

        });
    }
}