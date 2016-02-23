package com.ogadai.ogadai_secure.awaystatus;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.ogadai.ogadai_secure.HistoryItem;
import com.ogadai.ogadai_secure.IServerRequest;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.ShowNotification;
import com.ogadai.ogadai_secure.UpdateStatesMessage;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

import org.glassfish.tyrus.client.auth.AuthenticationException;

/**
 * Created by alee on 22/02/2016.
 */
public class EnterExitSetup implements IEnterExitSetup {
    private Context mContext;
    private GeofenceSetup mGeofenceSetup;

    public EnterExitSetup(Context context) {
        mContext = context;
        mGeofenceSetup = new GeofenceSetup(context);
    }

    @Override
    public void setup() {
        requestNewTokenOnThread();
        getLocationOnThread();
    }

    @Override
    public void remove() {
        mGeofenceSetup.remove();
    }

    private void requestNewTokenOnThread() {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                requestNewToken();
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }

    private void requestNewToken() {
        System.out.println("requesting new token");

        ITokenCache googleToken = new TokenCache(mContext, TokenCache.GOOGLE_PREFFILE);
        CachedToken cachedToken = googleToken.get();
        if (cachedToken == null) {
            Log.e("geofence", "No cached token available");
        }
        IServerRequest serverRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", cachedToken.getToken());

        try {
            String response = serverRequest.post("https://ogadai-secure.azure-mobile.net/api/SetupToken", null);

            Gson gson = new Gson();
            AwayStatusMessage tokenMessage = gson.fromJson(response, AwayStatusMessage.class);

            ITokenCache awayStatusToken = new TokenCache(mContext, TokenCache.AWAYSTATUS_PREFFILE);
            awayStatusToken.set(new CachedToken(tokenMessage.getUserName(), tokenMessage.getToken()));

            System.out.println("Updated token for user - " + tokenMessage.getUserName());
        } catch (Exception e) {
            System.out.println("Error posting away status - " + e.toString());

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error requesting Token", e.toString());
        }
    }

    private void getLocationOnThread() {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                getLocation();
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }

    private void getLocation() {
        ITokenCache googleToken = new TokenCache(mContext, TokenCache.GOOGLE_PREFFILE);
        CachedToken cachedToken = googleToken.get();
        if (cachedToken == null) {
            Log.e("geofence", "No cached token available");
        }
        IServerRequest serverRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", cachedToken.getToken());

        try {
            String response = serverRequest.get("https://ogadai-secure.azure-mobile.net/api/hublocation");

            Gson gson = new Gson();
            HubLocationMessage location = gson.fromJson(response, HubLocationMessage.class);

            mGeofenceSetup.setLocation(location.getLatitude(), location.getLongitude(), location.getRadius());
            mGeofenceSetup.setup();
        } catch (Exception e) {
            System.out.println("Error getting hub location - " + e.toString());

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error getting hub location", e.toString());
        }
    }

    private class HubLocationMessage
    {
        @SerializedName("latitude")
        private double mLatitude;

        @SerializedName("longitude")
        private double mLongitude;

        @SerializedName("radius")
        private float mRadius;

        public HubLocationMessage(double latitude, double longitude, float radius) {
            setLatitude(latitude);
            setLongitude(longitude);
            setRadius(radius);
        }

        public double getLatitude() {
            return mLatitude;
        }

        public void setLatitude(double latitude) {
            mLatitude = latitude;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public void setLongitude(double longitude) {
            mLongitude = longitude;
        }

        public float getRadius() {
            return mRadius;
        }

        public void setRadius(float radius) {
            mRadius = radius;
        }
    }
}
