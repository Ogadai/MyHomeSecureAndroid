package com.ogadai.ogadai_secure.messages;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

/**
 * Created by alee on 11/02/2016.
 */
public class MessageBase {
    @SerializedName("Method")
    private String mMethod;

    public String getMethod() { return mMethod; }
    public final void setMethod(String method) { mMethod = method; }

    public static MessageBase FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, MessageBase.class);
    }
}
