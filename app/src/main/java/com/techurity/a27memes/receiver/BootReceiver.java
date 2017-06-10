package com.techurity.a27memes.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.techurity.a27memes.MainActivity;

import java.util.Calendar;

/**
 * Created by AJ on 6/10/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            /* Setting the alarm here */

            boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                    new Intent(context, NotificationReceiver.class),
                    PendingIntent.FLAG_NO_CREATE) != null);

            if (!alarmUp) {

                Intent alarmIntent = new Intent(context, NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 60);
                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                long interval = 60 * 1000;

                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        interval, pendingIntent);
            }
        }
    }
}
