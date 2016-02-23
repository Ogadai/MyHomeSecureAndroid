package com.ogadai.ogadai_secure;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ogadai.ogadai_secure.awaystatus.EnterExitSetup;
import com.ogadai.ogadai_secure.awaystatus.GeofenceSetup;
import com.ogadai.ogadai_secure.awaystatus.IEnterExitSetup;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_ENTEREXIT = "pref_enterexit";
    private static final int FINE_LOCATION_REQUEST = 7784;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
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
            boolean enterExitEnabled = sharedPreferences.getBoolean(key, false);
            System.out.println("Enter/Exit events " + (enterExitEnabled ? "enabled" : "disabled"));

            if (enterExitEnabled) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
            } else {
                IEnterExitSetup enterExitSetup = new EnterExitSetup(getActivity());
                enterExitSetup.remove();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST:
                if (PackageManager.PERMISSION_GRANTED == getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    IEnterExitSetup enterExitSetup = new EnterExitSetup(getActivity());
                    enterExitSetup.setup();
                } else {
                    SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
                    preferences.edit().remove(KEY_PREF_ENTEREXIT).commit();
                }
                break;
        }
    }
}
