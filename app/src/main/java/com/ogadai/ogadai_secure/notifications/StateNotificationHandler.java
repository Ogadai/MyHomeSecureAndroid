package com.ogadai.ogadai_secure.notifications;

import android.content.Context;

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
    }
}
