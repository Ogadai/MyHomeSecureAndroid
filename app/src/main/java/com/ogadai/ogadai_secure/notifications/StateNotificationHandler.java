package com.ogadai.ogadai_secure.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ogadai.ogadai_secure.CameraFragment;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.CheckIfHomeCallback;
import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Random;

/**
 * Created by alee on 24/02/2016.
 */
public class StateNotificationHandler {
    private Context mContext;

    private static Random mRandGen = new Random();

    public StateNotificationHandler(Context context) {
        mContext = context;
    }
    public void received(final NotifyMessageState message) {
        System.out.println("state notification received - " + message.getState());
        ShowNotification notify = new ShowNotification(mContext);
        final String title = message.getState() + " has been activated!";

        Bitmap snapshot = CameraFragment.getLastImage();
        if (snapshot != null && (new Date().getTime() - CameraFragment.getLastImageDate().getTime() > 2 * 60 * 1000)) {
            snapshot = null;
        }

        notify.show(message.getState(), title, ShowNotification.STATEID, snapshot, false);

        checkIfHome();

        if (snapshot == null) {
            updateNotificationWithSnapshot(message);
        }
    }

    private void checkIfHome() {
        GeofenceSetup geofence = new GeofenceSetup(mContext);
        geofence.checkIfHome(new CheckIfHomeCallback() {
            @Override
            public void OnUserIsHome() {
                AwayStatusUpdate statusUpdate = new AwayStatusUpdate(mContext);
                try {
                    statusUpdate.updateStatus(ManageAwayStatus.ENTERED_EVENT);
                } catch (Exception e) {
                    System.out.println("error updating status to entered - " + e.toString());
                }
            }
        });
    }

    private void updateNotificationWithSnapshot(final NotifyMessageState message) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                int index = mRandGen.nextInt(10000);
                try {
                    // Get a new snapshot image
                    System.out.println("getting snapshot for notification");
                    HttpURLConnection urlConnection = ServerRequest.setupConnectionWithAuth(mContext, "GET", "camerasnapshot?node=garage&i=" + Integer.toString(index), null);
                    Bitmap snapshot = BitmapFactory.decodeStream(urlConnection.getInputStream());

                    CameraFragment.setLastImage(snapshot);

                    // Update the notification
                    System.out.println("updating notification with snapshot");

                    ShowNotification notify = new ShowNotification(mContext);
                    final String title = message.getState() + " has been activated!";
                    notify.show(message.getState(), title, ShowNotification.STATEID, snapshot, true);
                } catch (Exception e) {
                    System.out.println("error getting notification snapshot - " + e.toString());
                }
                return null;
            }
        };
        task.execute();
    }
}
