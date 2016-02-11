package com.ogadai.ogadai_secure;

import android.content.SharedPreferences;

import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

/**
 * Created by alee on 04/02/2016.
 */
public interface IAuthenticateClient {
    void Authenticated(MobileServiceUser user);
    void showError(Exception exception, String title);
    SharedPreferences getSharedPreferences(String name, int mode);
    void runOnUiThread(Runnable runnable);
}
