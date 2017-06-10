package com.techurity.a27memes.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.techurity.a27memes.MainActivity;
import com.techurity.a27memes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ajay Srinivas on 6/10/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private NotificationCompat.Builder builder;
    String main_message;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle parameters = new Bundle();
        parameters.putString("limit", "1");
        final Context mContext = context;

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/429615974063759/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject mainObj = response.getJSONObject();
                try {
                    JSONArray jsonArray = mainObj.getJSONArray("data");
                    JSONObject postObj = jsonArray.getJSONObject(0);
                    main_message = postObj.getString("message");
                    if (!main_message.equals("stop")){
                        makeNotification(mContext, main_message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        request.executeAsync();

    }

    private void makeNotification(Context context, String main_message) {

        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_menu_likeus)
                .setContentTitle("27Memes")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(main_message);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(27, builder.build());

        Log.d("Time Now", ""+ SystemClock.elapsedRealtime());

    }


}
