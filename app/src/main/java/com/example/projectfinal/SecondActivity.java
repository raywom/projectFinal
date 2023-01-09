package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView arg1TextView = findViewById(R.id.arg1_text_view);
        TextView arg2TextView = findViewById(R.id.arg2_text_view);

        Intent intent = getIntent();
        String arg1 = intent.getStringExtra("arg1");
        String arg2 = intent.getStringExtra("arg2");

        String invertedArg1 = new StringBuilder(arg1).reverse().toString();
        String invertedArg2 = new StringBuilder(arg2).reverse().toString();

        arg1TextView.setText(invertedArg1);
        arg2TextView.setText(invertedArg2);
    }
}