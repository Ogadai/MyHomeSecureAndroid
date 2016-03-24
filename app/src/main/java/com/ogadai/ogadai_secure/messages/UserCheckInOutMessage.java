package com.ogadai.ogadai_secure.messages;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.ogadai.ogadai_secure.StateItem;

import java.io.IOException;
import java.util.List;

/**
 * Created by alee on 24/03/2016.
 */
public class UserCheckInOutMessage extends MessageBase {
    @SerializedName("UserName")
    private String mUserName;

    @SerializedName("CurrentUser")
    private boolean mCurrentUser;

    @SerializedName("Away")
    private boolean mAway;

    public String getUserName() { return mUserName; }
    public final void setUserName(String userName) { mUserName = userName; }

    public boolean getCurrentUser() { return mCurrentUser; }
    public final void setCurrentUser(boolean currentUser) { mCurrentUser = currentUser; }

    public boolean getAway() { return mAway; }
    public final void setAway(boolean away) { mAway = away; }

    public static UserCheckInOutMessage FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, UserCheckInOutMessage.class);
    }
}
