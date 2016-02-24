package com.ogadai.ogadai_secure.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

import com.ogadai.ogadai_secure.MainActivity;
import com.ogadai.ogadai_secure.R;
import com.ogadai.ogadai_secure.StateItem;

/**
 * Created by alee on 19/02/2016.
 */
public class ShowNotification {
    public static final int ERRORID = 8923;
    public static final int STATEID = 8924;

    private Context mContext;

    public ShowNotification(Context context) {
        mContext = context;
    }

    public void show(String title, String content) {
        show(title, content, ERRORID);
    }

    public void show(String title, String content, int id) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setContentText(content);

        if (id == STATEID) {
            Uri notifySound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/alarm");

            builder = builder
                    .setSound(notifySound)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        notificationManager.notify(id, builder.build());
    }
}
