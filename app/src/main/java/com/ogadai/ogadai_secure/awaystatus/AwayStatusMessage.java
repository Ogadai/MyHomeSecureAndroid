package com.ogadai.ogadai_secure.awaystatus;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 22/02/2016.
 */
public class AwayStatusMessage {
    @SerializedName("userName")
    private String mUserName;

    @SerializedName("token")
    private String mToken;

    @SerializedName("action")
    private String mAction;

    public AwayStatusMessage(String action) {
        setAction(action);
    }

    public AwayStatusMessage(String userName, String token, String action) {
        setUserName(userName);
        setToken(token);
        setAction(action);
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        mAction = action;
    }
}
