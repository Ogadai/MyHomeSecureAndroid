package com.ogadai.ogadai_secure.socket;

import android.os.AsyncTask;

import com.ogadai.ogadai_secure.ServerRequest;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.net.URI;

/**
 * Created by alee on 10/02/2016.
 */
public class HomeSecureSocket implements IHomeSecureSocket {
    private IHomeSecureSocketClient mClient;

    private AsyncTask<String, Void, Void> mTask;
    private WebsocketClientEndpoint mClientEndPoint;

    public HomeSecureSocket(IHomeSecureSocketClient client) {
        mClient = client;
    }

    public static String ROOTPATH = "wss://" + ServerRequest.HOSTNAME + "/";
    public static String ROOTAPIPATH = ROOTPATH + "api/";

    @Override
    public void Connect(String token) {
        System.out.println("connect home secure socket");
        if (mClientEndPoint == null) {
            connectWebSocketFromTask(token);
        }
    }

    @Override
    public void Disconnect() {
        System.out.println("disconnect home secure socket");
        if (mClientEndPoint != null) {
            disconnectWebSocketFromTask();
        }
    }

    private void clearTask() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private void connectWebSocketFromTask(String token) {
        clearTask();
        mTask = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                connectWebSocket(urls[0]);
                mTask = null;
                return null;
            }

            protected void onPostExecute() {

            }
        };
        mTask.execute(token);
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

            mClientEndPoint.Connect(new URI(ROOTAPIPATH + "userapp"));
        } catch(AuthenticationException authEx) {
            mClient.connectionError(authEx);
        } catch (Exception e) {
            e.printStackTrace();
            mClient.connectionError(e);
        }
    }

    private void disconnectWebSocketFromTask()
    {
        clearTask();
        mTask = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                mTask = null;
                disconnectWebSocket();
                return null;
            }

            protected void onPostExecute() {

            }
        };
        mTask.execute();
    }
    private void disconnectWebSocket()
    {
        if (mClientEndPoint != null) {
            mClientEndPoint.Disconnect();
            mClientEndPoint = null;
        }
    }
}
