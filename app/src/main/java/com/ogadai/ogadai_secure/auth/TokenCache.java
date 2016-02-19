package com.ogadai.ogadai_secure.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alee on 19/02/2016.
 */
public class TokenCache implements ITokenCache {
    private Context mContext;

    private static final String SHAREDPREFFILE = "temp";
    private static final String USERIDPREF = "uid";
    private static final String TOKENPREF = "tkn";

    public TokenCache(Context context) {
        mContext = context;
    }

    @Override
    public void set(CachedToken cachedToken)
    {
        SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, cachedToken.getUser());
        editor.putString(TOKENPREF, cachedToken.getToken());
        editor.commit();
    }

    @Override
    public CachedToken get()
    {
        SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, "undefined");
        if (userId == "undefined")
            return null;
        String token = prefs.getString(TOKENPREF, "undefined");
        if (token == "undefined")
            return null;


        return new CachedToken(userId, token);
    }
}
