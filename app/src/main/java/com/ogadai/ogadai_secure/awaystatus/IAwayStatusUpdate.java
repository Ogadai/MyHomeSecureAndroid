package com.ogadai.ogadai_secure.awaystatus;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;

/**
 * Created by alee on 19/02/2016.
 */
public interface IAwayStatusUpdate {
    void updateStatus(String action) throws IOException, AuthenticationException;
}
