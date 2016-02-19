package com.ogadai.ogadai_secure.socket;

/**
 * Created by alee on 10/02/2016.
 */
public interface IHomeSecureSocket {
    void Connect(String token);

    void Disconnect();
}
