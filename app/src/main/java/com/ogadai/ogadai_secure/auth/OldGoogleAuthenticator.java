package com.ogadai.ogadai_secure.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.ogadai.ogadai_secure.MainActivity;
import com.ogadai.ogadai_secure.ServerRequest;

import java.net.MalformedURLException;

/**
 * Created by alee on 04/02/2016.
 */
public class OldGoogleAuthenticator implements IGoogleAuthenticator {

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;
    private ITokenCache mTokenCache;

    private IAuthenticateClient mAuthenticateClient;

    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();

    public OldGoogleAuthenticator(Context context) {
        mTokenCache = new TokenCache(context, TokenCache.GOOGLE_PREFFILE);
    }

    public void authenticate(Context context, boolean update, IAuthenticateClient authenticateClient) {
        // Mobile Service URL and key
        try {
            mClient = new MobileServiceClient(
                    ServerRequest.ROOTPATH,
                    context);

            mAuthenticateClient = authenticateClient;

            doAuthenticate(update);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Authenticates with the desired login provider. Also caches the token.
     *
     * If a local token cache is detected, the token cache is used instead of an actual
     * login unless bRefresh is set to true forcing a refresh.
     *
     * @param bRefreshCache
     *            Indicates whether to force a token refresh.
     */
    private void doAuthenticate(boolean bRefreshCache) {

        bAuthenticating = true;

        if (bRefreshCache || !loadUserTokenCache(mClient))
        {
            // New login using the provider and update the token cache.
            mClient.login(MobileServiceAuthenticationProvider.Google,
                    new UserAuthenticationCallback() {
                        @Override
                        public void onCompleted(MobileServiceUser user,
                                                Exception exception, ServiceFilterResponse response) {

                            synchronized (mAuthenticationLock) {
                                if (exception == null) {
                                    if (user != null) {
                                        cacheUserToken(user);
                                        mAuthenticateClient.authenticated();
                                    }
                                } else {
                                    mAuthenticateClient.showError(exception, "Login Error");
                                }
                                bAuthenticating = false;
                                mAuthenticationLock.notifyAll();
                            }
                        }
                    });
        }
        else
        {
            // Other threads may be blocked waiting to be notified when
            // authentication is complete.
            synchronized(mAuthenticationLock)
            {
                bAuthenticating = false;
                mAuthenticationLock.notifyAll();
            }
            mAuthenticateClient.authenticated();
        }
    }

    private void cacheUserToken(MobileServiceUser user)
    {
        mTokenCache.set(new CachedToken(user.getUserId(), user.getAuthenticationToken()));
    }

    private boolean loadUserTokenCache(MobileServiceClient client)
    {
        CachedToken cachedToken= mTokenCache.get();
        if (cachedToken == null)
            return false;

        MobileServiceUser user = new MobileServiceUser(cachedToken.getUser());
        user.setAuthenticationToken(cachedToken.getToken());
        client.setCurrentUser(user);

        return true;
    }

}
