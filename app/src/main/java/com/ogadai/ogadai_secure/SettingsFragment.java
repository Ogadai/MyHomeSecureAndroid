package com.ogadai.ogadai_secure;


import android.Manifest;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.ogadai.ogadai_secure.awaystatus.EnterExitSetup;
import com.ogadai.ogadai_secure.awaystatus.IEnterExitSetup;
import com.ogadai.ogadai_secure.notifications.HomeNotificationHandler;
import com.ogadai.ogadai_secure.notifications.HubMessagingService;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String NOTIFICATION_SENDER_ID = "724129164049";

    public static final String KEY_PREF_ENTEREXIT = "pref_enterexit";
    public static final String KEY_PREF_NOTIFICATIONS = "pref_notifications";

    private static final int FINE_LOCATION_REQUEST = 7784;

    private MainActivity mActivity;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mActivity = (MainActivity)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_ENTEREXIT)) {
            setEnterExitEnabled(sharedPreferences.getBoolean(key, false));
        } else if (key.equals(KEY_PREF_NOTIFICATIONS)) {
            setNotificationsEnabled(sharedPreferences.getBoolean(key, false));
        }
    }

    private void setEnterExitEnabled(boolean enterExitEnabled) {
        System.out.println("Enter/Exit events " + (enterExitEnabled ? "enabled" : "disabled"));

        if (enterExitEnabled) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
        } else {
            IEnterExitSetup enterExitSetup = new EnterExitSetup(mActivity);
            enterExitSetup.remove();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Runnable onFail = new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
                preferences.edit().putBoolean(KEY_PREF_ENTEREXIT, false).commit();

                mActivity.createAndShowDialogFromTask("Failed to setup location tracking", "Location");
            }
        };

        switch (requestCode) {
            case FINE_LOCATION_REQUEST:
                if (PackageManager.PERMISSION_GRANTED == mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    IEnterExitSetup enterExitSetup = new EnterExitSetup(mActivity);
                    enterExitSetup.setup(onFail);
                } else {
                    onFail.run();
                }
                break;
        }
    }

    private void setNotificationsEnabled(final boolean notificationsEnabled) {
        HubMessagingService.setFailCallback(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
                preferences.edit().putBoolean(KEY_PREF_NOTIFICATIONS, !notificationsEnabled).commit();

                String message = "Failed to " + (notificationsEnabled ? "register for" : "unregister from") + " notifications";
                mActivity.createAndShowDialogFromTask(message, "Notifications");
            }
        });

        if (notificationsEnabled) {
            HubMessagingService.register(mActivity);
//            NotificationsManager.handleNotifications(mActivity,
//                    NOTIFICATION_SENDER_ID,
//                    HomeNotificationHandler.class);
        } else {
            HubMessagingService.unregister(mActivity);
//            NotificationsManager.stopHandlingNotifications(mActivity);
        }
    }
}
