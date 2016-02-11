package com.ogadai.ogadai_secure;

/**
 * Created by alee on 10/02/2016.
 */
public interface IHomeSecureSocketClient {
    void Connected();
    void Disconnected(boolean error);
    void ShowError(Exception e, String error);

    void MessageReceived(String message);
}
