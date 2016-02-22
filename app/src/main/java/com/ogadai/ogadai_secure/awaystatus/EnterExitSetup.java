package com.ogadai.ogadai_secure.awaystatus;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
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
    private GeofenceSetup mGeofenceSetup = new GeofenceSetup();

    @Override
    public void setup(Context context) {
        requestNewTokenOnThread(context);
        mGeofenceSetup.setup(context);
    }

    @Override
    public void remove(Context context) {
        mGeofenceSetup.remove(context);
    }

    private void requestNewTokenOnThread(final Context context) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                requestNewToken(context);
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();    }

    private void requestNewToken(final Context context) {
        System.out.println("requesting new token");

        ITokenCache googleToken = new TokenCache(context, TokenCache.GOOGLE_PREFFILE);
        CachedToken cachedToken = googleToken.get();
        if (cachedToken == null) {
            Log.e("geofence", "No cached token available");
        }
        IServerRequest serverRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", cachedToken.getToken());

        try {
            String response = serverRequest.post("https://ogadai-secure.azure-mobile.net/api/SetupToken", null);

            Gson gson = new Gson();
            AwayStatusMessage tokenMessage = gson.fromJson(response, AwayStatusMessage.class);

            ITokenCache awayStatusToken = new TokenCache(context, TokenCache.AWAYSTATUS_PREFFILE);
            awayStatusToken.set(new CachedToken(tokenMessage.getUserName(), tokenMessage.getToken()));

            System.out.println("Updated token for user - " + tokenMessage.getUserName());
        } catch (Exception e) {
            System.out.println("Error posting away status - " + e.toString());

            ShowNotification test = new ShowNotification(context);
            test.show("Error requesting Token", e.toString());
        }
    }
}
