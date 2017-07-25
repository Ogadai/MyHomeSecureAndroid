package com.ogadai.ogadai_secure.awaystatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ogadai.ogadai_secure.Logger;

/**
 * Created by alee on 17/07/2017.
 */

public class AwayStatusDelayReceiver extends BroadcastReceiver {
    private static final String TAG = "AwayStatusDelayReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.setContext(context);

        ManageAwayStatus.processStatus(context);
    }
}
