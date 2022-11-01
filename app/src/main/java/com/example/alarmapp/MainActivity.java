package com.example.alarmapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private int c ;
    private Button btnSetAlarm, btnChangeRingtone, btnSnooze;
    private static Ringtone r;
    private TextView textViewTotalAlarm;
    private PreferenceManager preferenceManager;
    private AlarmManager alarmManager;

    private Handler mainHandler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        CountDownLatch countDownLatch = new CountDownLatch(30000);

        btnSetAlarm = findViewById(R.id.alarmBtn);
        btnSnooze = findViewById(R.id.snooze);
        btnChangeRingtone = findViewById(R.id.changeRingtone);
        textViewTotalAlarm = findViewById(R.id.textAllAlarm);

        btnChangeRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean permission;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permission = Settings.System.canWrite(MainActivity.this);
                } else {
                    permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                }

                if (permission) {
                    final Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(MainActivity.this,RingtoneManager.TYPE_ALL);
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    startActivityForResult(intent, 999);
                }  else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        MainActivity.this.startActivityForResult(intent, 11);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS}, 11);
                    }
                }
            }
        });

        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c++;
                textViewTotalAlarm.setText("Alarm will trigger after 5 minutes, Total alarm: "+ String.valueOf(c));
//                Thread thread1 = new startThread();
//                thread1.start();

                Intent i = new Intent(getApplicationContext(), AlarmJobIntentService.class);
                i.putExtra("Timer", 5000);
                AlarmJobIntentService.enqueueWork(getApplicationContext(), i);
                setAlarm();

                setNotification();



            }
        });

        btnSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                r = AlarmJobIntentService.r;
                if (r != null && r.isPlaying()) {
                    r.stop();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Handler threadHandler = new Handler(Looper.getMainLooper());
                            threadHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(MainActivity.this, AlarmJobIntentService.class);
                                    Toast.makeText(MainActivity.this, "toast", Toast.LENGTH_SHORT).show();
                                    setAlarm();
                                    AlarmJobIntentService.enqueueWork(MainActivity.this, i);
                                    r.play();
                                    setNotification();
                                }
                            }, 300000);
                        }
                    }).start();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 999 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            AlarmJobIntentService.s = uri;
            preferenceManager.putString("uriString", uri.toString());
            RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uri);
        }
        if (requestCode == 11 && Settings.System.canWrite(this) && data != null){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            AlarmJobIntentService.s = uri;
            preferenceManager.putString("uriString", uri.toString());
            RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uri);
        }
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
        Intent i = new Intent(MainActivity.this, AlarmJobIntentService.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent p = PendingIntent.getService(MainActivity.this, 0, i, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , p);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            AudioManager audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
        return super.onKeyDown(keyCode, event);
    }
}