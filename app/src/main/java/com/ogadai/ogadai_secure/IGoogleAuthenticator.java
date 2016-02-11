package com.ogadai.ogadai_secure;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

/**
 * Created by alee on 04/02/2016.
 */
public interface IGoogleAuthenticator {
    void authenticate(MobileServiceClient client, boolean update, IAuthenticateClient authenticateClient);
}
