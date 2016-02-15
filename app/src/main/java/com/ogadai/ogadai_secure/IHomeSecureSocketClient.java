package com.ogadai.ogadai_secure;

/**
 * Created by alee on 10/02/2016.
 */
public interface IHomeSecureSocketClient {
    void connected();
    void disconnected(boolean error);
    void showError(Exception e, String error);

    void messageReceived(String message);
}
