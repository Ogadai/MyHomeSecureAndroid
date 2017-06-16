package com.ogadai.ogadai_secure.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.awaystatus.HubLocationMessage;

import java.io.IOException;

/**
 * Created by alee on 16/06/2017.
 */

public class HubMessagingService extends FirebaseMessagingService {
    private static final String TAG = "HubMessagingService";

    private static Runnable mFailCallback;
    public static void setFailCallback(Runnable callback) { mFailCallback = callback; }

    private static String mHubId;

    public static void register(final Context context) {
//        String token = FirebaseInstanceId.getInstance().getToken();
//        System.out.println("Current token: " + token);

        runWithHubId(context, new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging.getInstance().subscribeToTopic(mHubId);
                System.out.println("Registered for hub - " + mHubId);
            }
        });
    }

    public static void unregister(final Context context) {
        runWithHubId(context, new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging.getInstance().subscribeToTopic(mHubId);
                System.out.println("Unregistered for hub - " + mHubId);
            }
        });
    }

    private static void runWithHubId(final Context context, final Runnable callback) {
        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                boolean successful = false;
                try {
                    mHubId = getHubId(context);
                    callback.run();
                    successful = true;
                }
                catch(Exception e) {
                    // handle error
                    System.out.println("Notifications handler error - " + e.getMessage());
                }

                if (!successful && mFailCallback != null) mFailCallback.run();
                return null;
            }
        }.execute();
    }

    private static String getHubId(Context context) {
        try {
            HubLocationMessage location = ServerRequest.get(context, "hublocation", HubLocationMessage.class);
            return location.getHubId();
        } catch (Exception e) {
            System.out.println("Error getting hub id - " + e.toString());

            ShowNotification test = new ShowNotification(context);
            test.show("Error getting hub location", e.toString());
            return null;
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String nhMessage = remoteMessage.getData().get("message");
            System.out.println("notification - " + nhMessage);

            try {
                NotifyMessageBase message = NotifyMessageBase.FromJSON(nhMessage);

                if (message.getMessage().compareTo(NotifyMessageState.MESSAGE) == 0) {
                    StateNotificationHandler handler = new StateNotificationHandler(this.getApplicationContext());
                    handler.received(NotifyMessageState.FromJSON(nhMessage));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
