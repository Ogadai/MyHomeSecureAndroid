package com.ogadai.ogadai_secure.socket;

/**
 * Created by alee on 10/02/2016.
 */
public interface IHomeSecureSocketClient {
    void connected();
    void connectionError(Exception ex);
    void disconnected(boolean error);
    void showError(Exception e, String error);

    void messageReceived(String message);
}
