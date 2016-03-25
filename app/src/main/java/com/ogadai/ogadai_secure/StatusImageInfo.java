package com.ogadai.ogadai_secure;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.ogadai.ogadai_secure.messages.UpdateStatesMessage;

import java.io.IOException;
import java.util.Date;

/**
 * Created by alee on 25/03/2016.
 */
public class StatusImageInfo {
    @SerializedName("state")
    private String mState;
    @SerializedName("fileName")
    private String mFileName;
    @SerializedName("active")
    private boolean mActive;
    @SerializedName("updated")
    private Date mUpdated;
    @SerializedName("zIndex")
    private int mZIndex;

    public String getState() { return mState; }
    public final void setState(String state) { mState = state; }
    public String getFileName() { return mFileName; }
    public final void setFileName(String fileName) { mFileName = fileName; }
    public boolean getActive() { return mActive; }
    public final void setActive(boolean active) { mActive = active; }
    public Date getUpdated() { return mUpdated; }
    public final void setUpdated(Date updated) { mUpdated = updated; }
    public int getZIndex() { return mZIndex; }
    public final void setZIndex(int zIndex) { mZIndex = zIndex; }
}

