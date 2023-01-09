package com.example.projectfinal;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private NotificationManager notificationManager;
    private int pauseCounter = 0;
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private EditText arg1EditText;
    private EditText arg2EditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonAddEntryListener();
        setButtonSaveDataListener();
        setButtonInverValuesListener();
        setButtonClearListListener();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseCounter++;
        updateNotification();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    private void updateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.color_accent))
                .setContentTitle("Pauses")
                .setContentText("Count: " + pauseCounter)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .setSummaryText("Total count so far: " + pauseCounter)
                .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.meme_image));
        builder.setStyle(style);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setButtonClearListListener() {
        Button clearButton = findViewById(R.id.button_clear_list);
        clearButton.setOnClickListener(view -> {
            list.clear();
            adapter.notifyDataSetChanged();

            SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("data");
            editor.apply();
        });
    }

    private void setButtonInverValuesListener() {
        Button launchButton = findViewById(R.id.button_invert_values);
        launchButton.setOnClickListener(view -> launchSecondActivity());
    }

    private void setButtonSaveDataListener() {
        Button saveButton = findViewById(R.id.button_save_data);
        saveButton.setOnClickListener(view -> {
            requestWriteExternalStoragePermission();
            saveDataToFile();
        });
    }

    private void requestWriteExternalStoragePermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

        }

    }


    public void launchSecondActivity() {
        String arg1 = arg1EditText.getText().toString();
        String arg2 = arg2EditText.getText().toString();

        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("arg1", arg1);
        intent.putExtra("arg2", arg2);
        startActivity(intent);
    }

    private void setButtonAddEntryListener() {
        ListView listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        arg1EditText = findViewById(R.id.arg1);
        arg2EditText = findViewById(R.id.arg2);

        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        Set<String> dataSet = sharedPreferences.getStringSet("data", null);
        if (dataSet != null) {
            list.clear();
            list.addAll(dataSet);
            adapter.notifyDataSetChanged();
        }


        Button button = findViewById(R.id.button_add_entry);
        button.setOnClickListener(view -> {
            if (arg1EditText.getText().toString().equals("") || arg2EditText.getText().toString().equals("")) {
                showToast("Please enter both values");
            } else {
                String arg1 = arg1EditText.getText().toString();
                String arg2 = arg2EditText.getText().toString();
                String entry = arg1 + " " + arg2;
                list.add(entry);
                adapter.notifyDataSetChanged();
                arg1EditText.setText("");
                arg2EditText.setText("");
                SharedPreferences sharedPreferences1 = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                Set<String> dataSet1 = new HashSet<>(list);
                editor.putStringSet("data", dataSet1);
                editor.apply();
            }
        });
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String item = adapter.getItem(position);

            showToast(item);
        });
    }

    public void showToast(String item) {
        Toast toast = new Toast(getApplicationContext());
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, findViewById(R.id.toast_root));
        TextView text = layout.findViewById(R.id.text);
        text.setText(item);

        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void saveDataToFile() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, "data.txt");
        try {
            OutputStream os = Files.newOutputStream(file.toPath());
            for (String entry : list) {
                os.write(entry.getBytes());
                os.write("\n".getBytes());
            }
            os.close();
            showToast("Data saved to file successfully!");
        } catch (AccessDeniedException e) {
            showToast("No permission for saving data to file!");
            e.printStackTrace();
        } catch (IOException e) {
            showToast("Error saving data to file!");
            e.printStackTrace();
        }
    }
}