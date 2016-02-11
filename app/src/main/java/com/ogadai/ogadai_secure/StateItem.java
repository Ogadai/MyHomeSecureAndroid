package com.ogadai.ogadai_secure;

/**
 * Created by alee on 11/02/2016.
 */
public class StateItem {
    /**
     * Name
     */
    @com.google.gson.annotations.SerializedName("Name")
    private String mName;

    /**
     * Active
     */
    @com.google.gson.annotations.SerializedName("Active")
    private boolean mActive;

    @Override
    public String toString() {
        return getName() + " - " + getActive();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StateItem && ((StateItem)o).getName() == getName();
    }

    public StateItem(String name, boolean active) {
        setName(name);
        setActive(active);
    }

    public String getName() { return mName; }
    public final void setName(String name) { mName = name; }

    public boolean getActive() { return mActive; }
    public final void setActive(boolean active) { mActive = active; }
}
