package com.ogadai.ogadai_secure.awaystatus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GeofenceSetup implements GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private GeofencingClient mGeofencingClient;

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
        getGeogencingClient();
        addGeofence();
    }

    public void remove() {
        System.out.println("Uninstalling geofence");
        getGeogencingClient();
        removeGeofence();
    }

    public void checkIfHome(final CheckIfHomeCallback callback) {
        System.out.println("Checking if user is home");
        doGoogleApiClientSetup(new Runnable() {
            @Override
            public void run() {
                checkIfUserHome(callback);
            }
        });
    }

    private void getGeogencingClient() {
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
    }

    private void doGoogleApiClientSetup(final Runnable callback) {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        System.out.println("GoogleApiClient connected");

                        callback.run();
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

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    private void addGeofence() {
        System.out.println("Adding geofence");

        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("Setting up geofence result - successful");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Setting up geofence result - failed: " + e.getMessage());
                        }
                    });

//            LocationServices.GeofencingApi.addGeofences(
//                    mGoogleApiClient,
//                    getGeofencingRequest(),
//                    getGeofencePendingIntent()
//            ).setResultCallback(new ResultCallback<Status>() {
//                @Override
//                public void onResult(Status status) {
//                    System.out.println("Setting up geofence result - " + (status.isSuccess() ? "successful" : status.getStatusMessage()));
//                    mGoogleApiClient.disconnect();
//                }
//            });
        } catch(SecurityException sec) {
            System.out.println("Security exception setting up geofence - " + sec.toString());
        } catch(Exception e) {
            System.out.println("Error setting up geofence - " + e.toString());
        }

    }

    private void removeGeofence() {
        System.out.println("Removing geofence");

        try {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("Remove geofence result - successful");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Remove geofence result - failed: " + e.getMessage());
                        }
                    });

//            LocationServices.GeofencingApi.removeGeofences(
//                    mGoogleApiClient,
//                    getGeofencePendingIntent()
//            ).setResultCallback(new ResultCallback<Status>() {
//                @Override
//                public void onResult(Status status) {
//                    System.out.println("Remove geofence result - " + (status.isSuccess() ? "successful" : status.getStatusMessage()));
//                    mGoogleApiClient.disconnect();
//                }
//            });
        } catch(Exception e) {
            System.out.println("Error removing geofence - " + e.toString());
        }

    }

    private Geofence getGeoFence() {
        SharedPreferences prefs = mContext.getSharedPreferences(LOCATION_PREFFILE, Context.MODE_PRIVATE);
        double latitude = prefs.getFloat(LATITUDEPREF, 52);
        double longitude = prefs.getFloat(LONGITUDEPREF, -2.5f);
        float radius = prefs.getFloat(RADIUSPREF, 500);

        return new Geofence.Builder()
                .setRequestId(mKey)
                .setCircularRegion(latitude, longitude, radius)
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

    private void checkIfUserHome(final CheckIfHomeCallback callback) {
        SharedPreferences prefs = mContext.getSharedPreferences(LOCATION_PREFFILE, Context.MODE_PRIVATE);
        final float radius = prefs.getFloat(RADIUSPREF, 500);
        final Location homeLocation = new Location("Google");
        homeLocation.setLatitude(prefs.getFloat(LATITUDEPREF, 52));
        homeLocation.setLongitude(prefs.getFloat(LONGITUDEPREF, -2.5f));

        try {
            final LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    System.out.println("Location update - lat: " + location.getLatitude() + ", lng: " + location.getLongitude());

                    float distance = location.distanceTo(homeLocation);
                    if (distance < radius) {
                        System.out.println("Stopping location updates - user is home");
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                        disconnectGoogleApiClient();

                        // Count this as home
                        callback.OnUserIsHome();
                    }
                }
            };

            System.out.println("Requesting location updates");
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, listener);

            // Stop updates after 30 seconds if not home
            ManageAwayStatus.getScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Stopping location updates - user not home");
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, listener);
                    disconnectGoogleApiClient();
                }
            }, 30, TimeUnit.SECONDS);

        } catch (SecurityException sec) {
            disconnectGoogleApiClient();
        }
    }
}
