package com.ogadai.ogadai_secure.notifications;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

/**
 * Created by alee on 24/02/2016.
 */
public class NotifyMessageBase {
    @SerializedName("Message")
    private String mMessage;

    public NotifyMessageBase(String message) {
        setMessage(message);
    }

    public String getMessage() { return mMessage; }
    public void setMessage(String message) { mMessage = message; }

    public static NotifyMessageBase FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, NotifyMessageBase.class);
    }
}
