package com.example.alarmapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmJobIntentService extends JobIntentService {

    static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, AlarmJobIntentService.class, 123, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        if (isStopped()) return;

        int t = intent.getIntExtra("Timer", 10000);

        SystemClock.sleep(t);

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notify")
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContentTitle("Alarm")
                .setContentText("Alarm reminder")
                .setAutoCancel(true)
                .setContentIntent(p)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(100, builder.build());

        Uri s = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Ringtone r = RingtoneManager.getRingtone(this, s);
        r.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }
}
