package com.ogadai.ogadai_secure.awaystatus;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ogadai.ogadai_secure.IServerRequest;
import com.ogadai.ogadai_secure.ServerRequest;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

import org.glassfish.tyrus.client.auth.AuthenticationException;

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
    public void updateStatus(String action) throws IOException, AuthenticationException {
        System.out.println("updating status transition - " + action);

        ITokenCache tokenCache = new TokenCache(mContext, TokenCache.AWAYSTATUS_PREFFILE);
        CachedToken cachedToken = tokenCache.get();
        if (cachedToken == null) {
            Log.e("geofence", "No awaystatus token available");
            return;
        }

        AwayStatusMessage awayStatus = new AwayStatusMessage(cachedToken.getUser(), cachedToken.getToken(), action);
        ServerRequest.post(null, "awaystatus", awayStatus);
    }
}
