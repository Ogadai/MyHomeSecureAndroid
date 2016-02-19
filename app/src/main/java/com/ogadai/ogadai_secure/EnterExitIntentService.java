package com.ogadai.ogadai_secure;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class EnterExitIntentService extends IntentService {

    public EnterExitIntentService() {
        super("EnterExitIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e("geofence", "geofencing error message - " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            postEvent(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ? "entered" : "exited");

        } else {
            // Log the error.
            Log.e("geofence", "geofencing invalid type" + geofenceTransition);
        }
    }

    protected void postEvent(String eventName) {
        IAwayStatusUpdate  statusUpdate = new AwayStatusUpdate(this);
        statusUpdate.updateStatus(eventName);
    }
}
