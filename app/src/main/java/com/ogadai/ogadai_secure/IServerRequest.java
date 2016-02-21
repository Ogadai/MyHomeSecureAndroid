package com.ogadai.ogadai_secure;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;

/**
 * Created by alee on 16/02/2016.
 */
public interface IServerRequest {
    String get(String address) throws IOException, AuthenticationException;
    String post(String address, String content) throws IOException, AuthenticationException;
}
