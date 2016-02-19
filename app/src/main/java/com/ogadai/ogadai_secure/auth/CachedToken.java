package com.ogadai.ogadai_secure.auth;

/**
 * Created by alee on 19/02/2016.
 */
public class CachedToken {
    private String mUser;
    private String mToken;

    public CachedToken(String user, String token) {
        setUser(user);
        setToken(token);
    }

    public String getUser() { return mUser; }
    public void setUser(String user) { mUser = user; }

    public String getToken() { return mToken; }
    public void setToken(String mToken) { this.mToken = mToken; }
}
