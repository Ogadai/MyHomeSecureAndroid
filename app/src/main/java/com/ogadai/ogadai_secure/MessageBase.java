package com.ogadai.ogadai_secure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 11/02/2016.
 */
public class MessageBase {
    @SerializedName("Method")
    private String mMethod;

    public String getMethod() { return mMethod; }
    public final void setMethod(String method) { mMethod = method; }
}
