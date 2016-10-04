package com.ogadai.ogadai_secure.notifications;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

/**
 * Created by alee on 24/02/2016.
 */
public class NotifyMessageState extends NotifyMessageBase {
    public final static String MESSAGE = "StateNotification";

    @SerializedName("State")
    private String mState;

    @SerializedName("Active")
    private boolean mActive;

    @SerializedName("Node")
    private String mNode;

    @SerializedName("Rule")
    private String mRule;

    public NotifyMessageState(String state) {
        super(MESSAGE);
        setState(state);
    }

    public String getState() { return mState; }
    public void setState(String state) { mState = state; }

    public String getNode() { return mNode; }
    public void setNode(String node) { mNode = node; }

    public String getRule() { return mRule; }
    public void setRule(String rule) { mRule = rule; }

    public static NotifyMessageState FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, NotifyMessageState.class);
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }
}
