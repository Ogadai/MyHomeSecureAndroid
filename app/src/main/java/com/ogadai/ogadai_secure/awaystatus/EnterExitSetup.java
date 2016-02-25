package com.ogadai.ogadai_secure.awaystatus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.ogadai.ogadai_secure.IServerRequest;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.notifications.ShowNotification;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

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

        try {
            AwayStatusMessage tokenMessage = ServerRequest.post(mContext, "setuptoken", AwayStatusMessage.class);

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
        try {
            HubLocationMessage location = ServerRequest.get(mContext, "hublocation", HubLocationMessage.class);

            mGeofenceSetup.setLocation(location.getLatitude(), location.getLongitude(), location.getRadius());
            mGeofenceSetup.setup();
        } catch (Exception e) {
            System.out.println("Error getting hub location - " + e.toString());

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error getting hub location", e.toString());
        }
    }

}
