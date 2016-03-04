package com.ogadai.ogadai_secure.awaystatus;

import android.content.Context;

/**
 * Created by alee on 22/02/2016.
 */
public interface IEnterExitSetup {
    void setup(Runnable failCallback);
    void remove();
}
