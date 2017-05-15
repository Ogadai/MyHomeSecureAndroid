package com.ogadai.ogadai_secure.auth;

import android.content.SharedPreferences;

import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

/**
 * Created by alee on 04/02/2016.
 */
public interface IAuthenticateClient {
    void authenticated();
    void showError(Exception exception, String title);
}
