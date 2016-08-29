package com.ogadai.ogadai_secure;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsFragment.KEY_PREF_ENTEREXIT, false)) {
            System.out.println("Timer action restoring geofence");
            GeofenceSetup geoFence = new GeofenceSetup(context);
            geoFence.setup();
        }
        System.out.println("Timer action completed");
    }

    public static void setupDailyAlarm(Context context) {
        PendingIntent pendingIntent = getIntent(context);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * 60 * 24;

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        PendingIntent pendingIntent = getIntent(context);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        manager.cancel(pendingIntent);
    }

    private static PendingIntent getIntent(Context context) {
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
    }
}
