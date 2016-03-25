package com.ogadai.ogadai_secure;

import android.graphics.Bitmap;

/**
 * Created by alee on 25/03/2016.
 */
class StateImage {
    private String mState;
    private Bitmap mActiveBitmap;
    private Bitmap mInactiveBitmap;

    public StateImage(String state) {
        mState = state;
    }

    public String getState() {
        return mState;
    }

    public Bitmap getActiveBitmap() {
        return mActiveBitmap;
    }

    public void setActiveBitmap(Bitmap activeBitmap) {
        mActiveBitmap = activeBitmap;
    }

    public Bitmap getInactiveBitmap() {
        return mInactiveBitmap;
    }

    public void setInactiveBitmap(Bitmap inactiveBitmap) {
        mInactiveBitmap = inactiveBitmap;
    }
}
