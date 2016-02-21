package com.ogadai.ogadai_secure.socket;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint(configurator = WebsocketClientConfigurator.class)
public class WebsocketClientEndpoint {
    WebSocketContainer mContainer;
    Session mUserSession = null;
    private MessageHandler mMessageHandler;

    public WebsocketClientEndpoint(final String appKey, final String authenticationToken) {
        try {
            System.out.println("connecting websocket");
            mContainer = ContainerProvider.getWebSocketContainer();
            WebsocketClientConfigurator.setAuthentication(appKey, authenticationToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Connect(URI endpointURI) throws AuthenticationException, IOException, DeploymentException {
        try {
            mContainer.connectToServer(this, endpointURI);
        } catch(DeploymentException depEx) {
            Throwable cause = depEx.getCause();
            if (cause instanceof AuthenticationException) {
                throw (AuthenticationException)cause;
            }
            throw depEx;
        }
    }

    public void Disconnect() {
        try {
            if (mUserSession != null) {
                Session session = mUserSession;
                mUserSession = null;
                session.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession, EndpointConfig config) {
        System.out.println("opening websocket");
        mUserSession = userSession;
        if (mMessageHandler != null) {
            mMessageHandler.handleOpen();
        }
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket :" + reason.getCloseCode().getCode() + " - " + reason.getReasonPhrase());
        if (mUserSession != null) {
            mUserSession = null;
            if (mMessageHandler != null) {
                mMessageHandler.handleClose(reason.getCloseCode().getCode() != 0);
            }
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (mMessageHandler != null) {
            mMessageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        mMessageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        mUserSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {
        public void handleMessage(String message);
        public void handleOpen();
        public void handleClose(boolean error);
    }
}
