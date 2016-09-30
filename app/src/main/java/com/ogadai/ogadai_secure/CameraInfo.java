package com.ogadai.ogadai_secure;

import java.util.Date;

/**
 * Created by alee on 30/09/2016.
 */

public class CameraInfo {
    /**
     * Name
     */
    @com.google.gson.annotations.SerializedName("name")
    private String mName;

    /**
     * Node
     */
    @com.google.gson.annotations.SerializedName("node")
    private String mNode;

    public CameraInfo(String name, String node) {
        setName(name);
        setNode(node);
    }

    public String getName() { return mName; }
    public final void setName(String name) { mName = name; }

    public String getNode() { return mNode; }
    public final void setNode(String node) { mNode = node; }}
