package com.example.alarmapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    int c ;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.alarmBtn);
        TextView textView = findViewById(R.id.textAllAlarm);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c++;
                textView.setText("Alarm will trigger after 5 minutes, Total alarm: "+ String.valueOf(c));
                Intent i = new Intent(MainActivity.this, AlarmJobIntentService.class);
                i.putExtra("Timer", 300000);
                AlarmJobIntentService.enqueueWork(MainActivity.this, i);
                setAlarm();
                setNotification();
            }
        });
    }

    private void setNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notify", "Alarm", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Alarm reminder");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(MainActivity.this, AlarmJobIntentService.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent p = PendingIntent.getService(MainActivity.this, 0, i, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , p);

    }
}