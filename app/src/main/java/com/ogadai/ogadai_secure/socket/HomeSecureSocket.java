package com.ogadai.ogadai_secure.socket;

import android.os.AsyncTask;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.net.URI;

/**
 * Created by alee on 10/02/2016.
 */
public class HomeSecureSocket implements IHomeSecureSocket {
    private IHomeSecureSocketClient mClient;
    private WebsocketClientEndpoint mClientEndPoint;

    public HomeSecureSocket(IHomeSecureSocketClient client) {
        mClient = client;
    }

    @Override
    public void Connect(String token) {
        System.out.println("connect home secure socket");
        connectWebSocketFromTask(token);
    }

    @Override
    public void Disconnect() {
        System.out.println("disconnect home secure socket");
        disconnectWebSocketFromTask();
    }

    private void connectWebSocketFromTask(String token) {
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            protected String doInBackground(String... urls) {
                connectWebSocket(urls[0]);
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute(token);
    }
    private void connectWebSocket(String token)
    {
        try {
            mClientEndPoint = new WebsocketClientEndpoint(
                    "RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90",
                    token);

            // add listener
            mClientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleOpen() {
                    mClient.connected();
                }

                public void handleMessage(String message) {
                    mClient.messageReceived(message);
                }

                public void handleClose(boolean error) {
                    mClientEndPoint = null;
                    mClient.disconnected(error);
                }
            });

            mClientEndPoint.Connect(new URI("wss://ogadai-secure.azure-mobile.net/api/userapp"));
        } catch(AuthenticationException authEx) {
            mClient.connectionError(authEx);
        } catch (Exception e) {
            e.printStackTrace();
            mClient.connectionError(e);
        }
    }

    private void disconnectWebSocketFromTask()
    {
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            protected String doInBackground(String... urls) {
                disconnectWebSocket();
                return null;
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }
    private void disconnectWebSocket()
    {
        if (mClientEndPoint != null) {
            mClientEndPoint.Disconnect();
            mClientEndPoint = null;
        }
    }
}
