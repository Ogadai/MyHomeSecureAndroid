package com.ogadai.ogadai_secure;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsFragment.KEY_PREF_ENTEREXIT, false)) {
            Log.i(TAG, "Boot action restoring geofence");
            GeofenceSetup geoFence = new GeofenceSetup(context);
            geoFence.setup();

//            Log.e(TAG, "Boot action restoring daily alarm");
//            AlarmReceiver.setupDailyAlarm(context);
        }
        Log.i(TAG, "Boot action completed");
    }
}
