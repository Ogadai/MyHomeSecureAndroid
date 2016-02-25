package com.ogadai.ogadai_secure.awaystatus;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 25/02/2016.
 */
public class HubLocationMessage {
    @SerializedName("hubId")
    private String mHubId;

    @SerializedName("latitude")
    private double mLatitude;

    @SerializedName("longitude")
    private double mLongitude;

    @SerializedName("radius")
    private float mRadius;

    public HubLocationMessage(String hubId, double latitude, double longitude, float radius) {
        setHubId(hubId);
        setLatitude(latitude);
        setLongitude(longitude);
        setRadius(radius);
    }

    public String getHubId() { return mHubId; }
    public void setHubId(String hubId) { mHubId = hubId; }

    public double getLatitude() {
        return mLatitude;
    }
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public float getRadius() {
        return mRadius;
    }
    public void setRadius(float radius) {
        mRadius = radius;
    }
}
