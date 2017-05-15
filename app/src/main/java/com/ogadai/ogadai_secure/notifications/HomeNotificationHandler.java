package com.ogadai.ogadai_secure.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.microsoft.windowsazure.notifications.NotificationsHandler;
import com.ogadai.ogadai_secure.MainActivity;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.awaystatus.HubLocationMessage;

import java.io.IOException;

/**
 * Created by alee on 24/02/2016.
 */
public class HomeNotificationHandler extends NotificationsHandler {

    private static Runnable mFailCallback;
    public static void setFailCallback(Runnable callback) { mFailCallback = callback; }

    @Override
    public void onRegistered(Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        final Context fContext = context;
        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                boolean successful = false;
                try {
                    String hubId = getHubId(fContext);
                    if (hubId != null) {
                        String[] tags = new String[]{hubId};
                        System.out.println("registering notifications handler");
//                        MainActivity.getClient().getPush().register(gcmRegistrationId, tags);
                        successful = true;
                    }
                }
                catch(Exception e) {
                    // handle error
                    System.out.println("Error registering notifications handler - " + e.getMessage());
                }

                if (!successful && mFailCallback != null) mFailCallback.run();

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
//                    MainActivity.getClient().getPush().unregisterAll(gcmRegistrationId);
                }
                catch(Exception e) {
                    // handle error
                    System.out.println("Error unregistering notifications handler - " + e.getMessage());
                    if (mFailCallback != null) mFailCallback.run();
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

    private String getHubId(Context context) {
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

}

