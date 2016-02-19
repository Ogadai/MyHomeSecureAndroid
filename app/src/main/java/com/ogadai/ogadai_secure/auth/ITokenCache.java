package com.ogadai.ogadai_secure.auth;

/**
 * Created by alee on 19/02/2016.
 */
public interface ITokenCache {
    void set(CachedToken cachedToken);

    CachedToken get();
}
