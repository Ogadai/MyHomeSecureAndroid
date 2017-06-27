package com.ogadai.ogadai_secure.awaystatus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.ogadai.ogadai_secure.AlarmReceiver;
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

    private static final String TAG = "EnterExitSetup";

    public EnterExitSetup(Context context) {
        mContext = context;
        mGeofenceSetup = new GeofenceSetup(context);
    }

    @Override
    public void setup(Runnable failCallback) {
        requestNewTokenOnThread(failCallback);
        getLocationOnThread(failCallback);
    }

    @Override
    public void remove() {
        mGeofenceSetup.remove();

//        Log.e(TAG, "Removing daily alarm");
//        AlarmReceiver.cancelAlarm(mContext);
    }

    private void requestNewTokenOnThread(final Runnable failCallback) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                requestNewToken(failCallback);
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }

    private void requestNewToken(Runnable failCallback) {
        Log.i(TAG, "requesting new token");

        try {
            AwayStatusMessage tokenMessage = ServerRequest.post(mContext, "setuptoken", AwayStatusMessage.class);

            ITokenCache awayStatusToken = new TokenCache(mContext, TokenCache.AWAYSTATUS_PREFFILE);
            awayStatusToken.set(new CachedToken(tokenMessage.getUserName(), tokenMessage.getToken()));

            Log.i(TAG, "Updated token for user - " + tokenMessage.getUserName());
        } catch (Exception e) {
            Log.e(TAG, "Error posting away status - " + e.toString());
            failCallback.run();

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error requesting Token", e.toString());
        }
    }

    private void getLocationOnThread(final Runnable failCallback) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                getLocation(failCallback);
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }

    private void getLocation(Runnable failCallback) {
        try {
            HubLocationMessage location = ServerRequest.get(mContext, "hublocation", HubLocationMessage.class);

            mGeofenceSetup.setLocation(location.getLatitude(), location.getLongitude(), location.getRadius());
            mGeofenceSetup.setup();

//            Log.i(TAG, "Setting up daily alarm");
//            AlarmReceiver.setupDailyAlarm(mContext);
        } catch (Exception e) {
            Log.e(TAG, "Error getting hub location - " + e.toString());
            failCallback.run();

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error getting hub location", e.toString());
        }
    }

}
