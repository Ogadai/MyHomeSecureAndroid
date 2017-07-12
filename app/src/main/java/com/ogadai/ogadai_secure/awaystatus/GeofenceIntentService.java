package com.ogadai.ogadai_secure.awaystatus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ogadai.ogadai_secure.Logger;

import java.util.List;

/**
 * Created by alee on 19/06/2017.
 */

public class GeofenceIntentService extends IntentService {

    private static final String TAG = "GeofenceIntentService";

    public GeofenceIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        Logger.setContext(this);
        GeofencingEvent geofenceEvent = GeofencingEvent.fromIntent(intent);
        if (!geofenceEvent.hasError()) {
            int transition = geofenceEvent.getGeofenceTransition();

            // Test that a valid transition was reported
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                String awayStatus = transition == Geofence.GEOFENCE_TRANSITION_ENTER
                        ? ManageAwayStatus.ENTERED_EVENT : ManageAwayStatus.EXITED_EVENT;
                Logger.i(TAG, "geofence transition - " + awayStatus);
                postEvent(this.getBaseContext(), awayStatus);
            }
        } else {
            Logger.e(TAG, "geofencing intent error - " + geofenceEvent.getErrorCode());
        }
    }

    protected void postEvent(Context context, String eventName) {
        IManageAwayStatus manageStatus = new ManageAwayStatus(context);
        manageStatus.setAwayStatus(eventName);
    }
}
