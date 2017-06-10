package com.techurity.a27memes.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.techurity.a27memes.MainActivity;
import com.techurity.a27memes.R;

/**
 * Created by Ajay Srinivas on 6/10/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private NotificationCompat.Builder builder;

    @Override
    public void onReceive(Context context, Intent intent) {

        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_menu_likeus)
                .setContentTitle("27Memes Notification")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText("New Meme Uploaded");

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(27, builder.build());


    }
}
