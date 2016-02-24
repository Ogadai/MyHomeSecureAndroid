package com.ogadai.ogadai_secure.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.microsoft.windowsazure.notifications.NotificationsHandler;
import com.ogadai.ogadai_secure.MainActivity;

import java.io.IOException;

/**
 * Created by alee on 24/02/2016.
 */
public class HomeNotificationHandler extends NotificationsHandler {

    @Override
    public void onRegistered(Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {
                    String[] tags = new String[] { "my-test-tag" };
                    System.out.println("registering notifications handler");
                    MainActivity.getClient().getPush().register(gcmRegistrationId, tags);
                    return null;
                }
                catch(Exception e) {
                    // handle error
                    System.out.println("Error registering notifications handler - " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onUnregistered(Context context, final String gcmRegistrationId) {
        super.onUnregistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {
                    System.out.println("unregistering notifications handler");
                    MainActivity.getClient().getPush().unregisterAll(gcmRegistrationId);
                    return null;
                }
                catch(Exception e) {
                    // handle error
                    System.out.println("Error unregistering notifications handler - " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        String nhMessage = bundle.getString("message");
        System.out.println("notification - " + nhMessage);

        try {
            NotifyMessageBase message = NotifyMessageBase.FromJSON(nhMessage);

            if (message.getMessage().compareTo(NotifyMessageState.MESSAGE) == 0) {
                StateNotificationHandler handler = new StateNotificationHandler(context);
                handler.received(NotifyMessageState.FromJSON(nhMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

