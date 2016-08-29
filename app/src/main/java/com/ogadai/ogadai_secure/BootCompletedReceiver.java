package com.ogadai.ogadai_secure;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsFragment.KEY_PREF_ENTEREXIT, false)) {
            System.out.println("Boot action restoring geofence");
            GeofenceSetup geoFence = new GeofenceSetup(context);
            geoFence.setup();

            System.out.println("Boot action restoring daily alarm");
            AlarmReceiver.setupDailyAlarm(context);
        }
        System.out.println("Boot action completed");
    }
}
