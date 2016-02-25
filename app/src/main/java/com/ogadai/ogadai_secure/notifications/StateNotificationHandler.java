package com.ogadai.ogadai_secure.notifications;

import android.content.Context;

import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.CheckIfHomeCallback;
import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;

/**
 * Created by alee on 24/02/2016.
 */
public class StateNotificationHandler {
    Context mContext;
    public StateNotificationHandler(Context context) {
        mContext = context;
    }
    public void received(NotifyMessageState message) {
        ShowNotification notify = new ShowNotification(mContext);
        notify.show(message.getState(),
                message.getState() + " has been activated!",
                ShowNotification.STATEID);

        checkIfHome();
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
}
