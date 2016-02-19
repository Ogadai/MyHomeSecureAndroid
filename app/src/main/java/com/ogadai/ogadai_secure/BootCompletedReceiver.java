package com.ogadai.ogadai_secure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofenceSetup geoFence = new GeofenceSetup();
        geoFence.setup(context);

        System.out.println("Boot action completed");
    }
}
