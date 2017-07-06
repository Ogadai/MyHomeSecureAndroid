package com.ogadai.ogadai_secure.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.ogadai.ogadai_secure.CameraFeed;
import com.ogadai.ogadai_secure.Logger;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.CheckIfHomeCallback;
import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;
import com.ogadai.ogadai_secure.awaystatus.IAwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;

import java.net.HttpURLConnection;
import java.util.Random;

/**
 * Created by alee on 24/02/2016.
 */
public class StateNotificationHandler {
    private Context mContext;

    private static Random mRandGen = new Random();

    private static String mLastState = null;
    private static Bitmap mLastSnapshot = null;
    private static String mLastNode = null;

    private static final String TAG = "StateNotification";

    public StateNotificationHandler(Context context) {
        mContext = context;
    }
    public void received(final NotifyMessageState message) {
        Logger.i(TAG, "state notification received - " + message.getState());
        ShowNotification notify = new ShowNotification(mContext);

        int notificationId = getId(message);
        if (message.isActive()) {
            String messageNode = message.getNode();
            if (messageNode != null && messageNode.length() > 0) {
                messageNode = messageNode.toLowerCase();
                mLastNode = messageNode;
            }
            mLastState = message.getState();

            notify.show(message.getState(), getTitle(message), notificationId, mLastSnapshot, false, useSound(message));

            if (alarmState(message)) {
                checkIfHome();

                if (mLastNode != null) {
                    updateNotificationWithSnapshot(message);
                }
            }
        } else {
            notify.clear(notificationId);
        }
    }

    private void checkIfHome() {
        GeofenceSetup geofence = new GeofenceSetup(mContext);
        geofence.checkIfHome(new CheckIfHomeCallback() {
            @Override
            public void OnUserIsHome() {
                try {
                    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                        @Override
                        protected Void doInBackground(String... urls) {
                            try {
                                AwayStatusUpdate statusUpdate = new AwayStatusUpdate(mContext);
                                statusUpdate.updateStatus(ManageAwayStatus.ENTERED_EVENT);
                            } catch (Exception e) {
                                Logger.e(TAG, "Error setting away status - " + e.toString());
                            }
                            return null;
                        }
                    };
                    task.execute();

                    // Clear the notification if home
                    ShowNotification notify = new ShowNotification(mContext);
                    notify.clear();
                } catch (Exception e) {
                    Logger.e(TAG, "error updating status to entered - " + e.toString());
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
                    Logger.i(TAG, "getting snapshot for notification");
                    HttpURLConnection urlConnection = ServerRequest.setupConnectionWithAuth(mContext, "GET",
                            "camerasnapshot?node=" + mLastNode + "&thumbnail=true&i=" + Integer.toString(index), null);
                    Bitmap snapshot = BitmapFactory.decodeStream(urlConnection.getInputStream());
                    mLastSnapshot = snapshot;

                    CameraFeed.setLastImage(mLastNode, snapshot);

                    if (message.getState() == mLastState) {
                        // Update the notification
                        Logger.i(TAG, "updating notification with snapshot");

                        ShowNotification notify = new ShowNotification(mContext);
                        notify.show(message.getState(), getTitle(message), ShowNotification.STATEID, snapshot, true, useSound(message));
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "error getting notification snapshot - " + e.toString());
                }
                return null;
            }
        };
        task.execute();
    }

    private String getTitle(NotifyMessageState message) {
        return alarmState(message)
                ? message.getState() + " has been activated!"
                : message.getState() + " mode is active";
    }

    private int getId(NotifyMessageState message) { return alarmState(message) ? ShowNotification.STATEID : ShowNotification.AWAYSTATUSID; }
    private boolean useSound(NotifyMessageState message) {
        return message.getState().equalsIgnoreCase("Alarm");
    }
    private boolean alarmState(NotifyMessageState message) {
        return !message.getState().equalsIgnoreCase("Away");
    }
}
