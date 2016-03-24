package com.ogadai.ogadai_secure.messages;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

/**
 * Created by alee on 24/03/2016.
 */
public class ConnectionStatusMessage extends MessageBase {
    @SerializedName("Connected")
    private boolean mConnected;

    public boolean getConnected() { return mConnected; }
    public final void setConnected(boolean connected) { mConnected = connected; }

    public static ConnectionStatusMessage FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, ConnectionStatusMessage.class);
    }
}

