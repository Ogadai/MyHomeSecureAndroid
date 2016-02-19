package com.ogadai.ogadai_secure.awaystatus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.ogadai.ogadai_secure.EnterExitIntentService;

/**
 * Created by alee on 16/02/2016.
 */
public class GeofenceSetup implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private boolean mAddedGeogence = false;

    private final String mKey = "ogadai-secure-home-geofence";

    public void setup(Context context) {
        System.out.println("Setting up geofence");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mContext = context;
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

    private Geofence getGeoFence() {
        return new Geofence.Builder()
                .setRequestId(mKey)
                .setCircularRegion(51.477996, -2.604144, 300)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(getGeoFence());
        GeofencingRequest request = builder.build();
        return request;
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, EnterExitIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("GoogleApiClient connected");
        if (!mAddedGeogence) {
            mAddedGeogence = true;
            addGeofence();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
