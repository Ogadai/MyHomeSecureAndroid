package com.ogadai.ogadai_secure.awaystatus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.ogadai.ogadai_secure.IAwayStatusUpdate;
import com.ogadai.ogadai_secure.IServerRequest;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.ShowNotification;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

import java.io.IOException;

/**
 * Created by alee on 19/02/2016.
 */
public class AwayStatusUpdate implements IAwayStatusUpdate {
    private Context mContext;
    public AwayStatusUpdate(Context context) {
        mContext = context;
    }

    @Override
    public void updateStatus(String action) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... urls) {
                updateStatusOnThread(urls[0]);
                return null;
            }
        };
        task.execute(action);
    }

    private void updateStatusOnThread(String action) {
        ITokenCache tokenCache = new TokenCache(mContext);
        CachedToken cachedToken = tokenCache.get();
        if (cachedToken == null) {
            Log.e("geofence", "No cached token available");
            return;
        }

        IServerRequest serverRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", cachedToken.getToken());

        AwayStatusMessage awayStatus = new AwayStatusMessage(action);
        Gson gson = new Gson();
        String message = gson.toJson(awayStatus);

        try {
            serverRequest.post("https://ogadai-secure.azure-mobile.net/api/AwayStatus", message);
        } catch (IOException e) {
            System.out.println("Error posting away status - " + e.toString());

            ShowNotification test = new ShowNotification(mContext);
            test.show("Error when " + action, e.toString());
        }
    }

    private class AwayStatusMessage {
        @SerializedName("Action")
        private String mAction;

        public AwayStatusMessage(String action) {
            setAction(action);
        }

        public String getAction() { return mAction; }
        public void setAction(String action) { mAction = action; }
    }
}
