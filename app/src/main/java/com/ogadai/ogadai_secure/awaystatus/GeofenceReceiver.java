package com.ogadai.ogadai_secure.awaystatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ogadai.ogadai_secure.R;
import com.ogadai.ogadai_secure.notifications.ShowNotification;

public class GeofenceReceiver extends BroadcastReceiver {
    public GeofenceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("geofence broadcast event received");
        GeofencingEvent geofenceEvent = GeofencingEvent.fromIntent(intent);
        if (!geofenceEvent.hasError()) {
            int transition = geofenceEvent.getGeofenceTransition();
            System.out.println("geofence transition - " + transition);

            // Test that a valid transition was reported
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                postEvent(context, transition == Geofence.GEOFENCE_TRANSITION_ENTER
                        ? ManageAwayStatus.ENTERED_EVENT : ManageAwayStatus.EXITED_EVENT);

                notifyUser(context, transition == Geofence.GEOFENCE_TRANSITION_EXIT);
            }
        } else {
            // Log the error.
            Log.e("geofence", "geofencing intent error - " + geofenceEvent.getErrorCode());
            System.out.println("geofence intent error - " + geofenceEvent.getErrorCode());
        }
    }

    protected void postEvent(Context context, String eventName) {
        IManageAwayStatus manageStatus = new ManageAwayStatus(context);
        manageStatus.setAwayStatus(eventName);
    }

    protected void notifyUser(Context context, boolean exited) {
        ShowNotification showNotification = new ShowNotification(context);
        if (exited) {
            showNotification.show(
                    "Away from home",
                    "Away mode is active",
                    ShowNotification.AWAYSTATUSID,
                    null,
                    false,
                    false,
                    R.drawable.notification_exit
                    );
        } else {
            showNotification.clear(ShowNotification.AWAYSTATUSID);
        }
    }
}
