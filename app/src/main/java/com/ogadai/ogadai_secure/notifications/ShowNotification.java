package com.ogadai.ogadai_secure.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

import com.ogadai.ogadai_secure.MainActivity;
import com.ogadai.ogadai_secure.R;
import com.ogadai.ogadai_secure.SetHomeActivity;
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
        show(title, content, id, null, false);
    }

    public void show(String title, String content, int id, Bitmap largeIcon, boolean update) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        resultIntent.putExtra(MainActivity.EXTRA_SHOWFRAGMENT, "monitor");

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent mainActivityIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.notification)
                        .setAutoCancel(true)
                        .setContentText(content)
                        .setContentIntent(mainActivityIntent);

        if (id == STATEID) {
            Uri notifySound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/alarm");

            // Creates an explicit intent for an Activity in your app
            Intent viewIntent = new Intent(mContext, MainActivity.class);
            viewIntent.putExtra(MainActivity.EXTRA_SHOWFRAGMENT, "camera");
            TaskStackBuilder stackBuilderView = TaskStackBuilder.create(mContext);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilderView.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilderView.addNextIntent(viewIntent);
            PendingIntent viewActivityIntent =
                    stackBuilderView.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent homeIntent = new Intent(mContext, SetHomeActivity.class);
            PendingIntent pHomeIntent = PendingIntent.getActivity(mContext, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder = builder
                    .addAction(R.drawable.ic_action_video, mContext.getString(R.string.notify_action_view), viewActivityIntent)
                    .addAction(R.drawable.notification, mContext.getString(R.string.notify_action_home), pHomeIntent);

            if (!update) {
                builder = builder
                    .setSound(notifySound)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
            };
        }

        if (largeIcon != null) {
            builder.setLargeIcon(cropForNotification(largeIcon));
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        // mId allows you to update the notification later on.
        notificationManager.notify(id, builder.build());
    }

    public void clear() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private Bitmap cropForNotification(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        int size = Math.min(width, height);

        return Bitmap.createBitmap(source,
                (width - size) / 2, (height - size) / 2,
                size, size);
    }
}
