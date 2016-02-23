package com.ogadai.ogadai_secure.awaystatus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class GeofenceSetup implements GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    private final String mKey = "ogadai-secure-home-geofence";

    public static final String LOCATION_PREFFILE = "hub_loc";
    private static final String LATITUDEPREF = "latitude";
    private static final String LONGITUDEPREF = "longitude";
    private static final String RADIUSPREF = "radius";

    public GeofenceSetup(Context context) {
        mContext = context;
    }

    public void setLocation(double latitude, double longitude, float radius) {
        SharedPreferences prefs = mContext.getSharedPreferences(LOCATION_PREFFILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(LATITUDEPREF, (float) latitude);
        editor.putFloat(LONGITUDEPREF, (float) longitude);
        editor.putFloat(RADIUSPREF, radius);
        editor.commit();
    }

    public void setup() {
        System.out.println("Setting up geofence");
        doGoogleApiClientSetup(true);
    }

    public void remove() {
        System.out.println("Uninstalling geofence");
        doGoogleApiClientSetup(false);
    }

    public void doGoogleApiClientSetup(final boolean settingUp) {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        System.out.println("GoogleApiClient connected");

                        if (settingUp) {
                            addGeofence();
                        } else {
                            removeGeofence();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }

    private void addGeofence() {
        System.out.println("Adding geofence");

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    System.out.println("Setting up geofence result - " + (status.isSuccess() ? "successful" : status.getStatusMessage()));
                    mGoogleApiClient.disconnect();
                }
            });
        } catch(Exception e) {
            System.out.println("Error setting up geofence - " + e.toString());
            mGoogleApiClient.disconnect();
        }
    }

    private void removeGeofence() {
        System.out.println("Removing geofence");

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    System.out.println("Remove geofence result - " + (status.isSuccess() ? "successful" : status.getStatusMessage()));
                    mGoogleApiClient.disconnect();
                }
            });
        } catch(Exception e) {
            System.out.println("Error removing geofence - " + e.toString());
            mGoogleApiClient.disconnect();
        }

    }

    private Geofence getGeoFence() {
        SharedPreferences prefs = mContext.getSharedPreferences(LOCATION_PREFFILE, Context.MODE_PRIVATE);
        double latitide = prefs.getFloat(LATITUDEPREF, 52);
        double longitude = prefs.getFloat(LONGITUDEPREF, -2.5f);
        float radius = prefs.getFloat(RADIUSPREF, 500);

        return new Geofence.Builder()
                .setRequestId(mKey)
                .setCircularRegion(latitide, longitude, radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(getGeoFence());
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("GoogleApiClient connection failed - " + connectionResult.getErrorMessage());
    }
}
