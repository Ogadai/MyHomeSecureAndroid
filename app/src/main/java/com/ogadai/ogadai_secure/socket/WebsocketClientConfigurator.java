package com.ogadai.ogadai_secure.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;

public class WebsocketClientConfigurator extends ClientEndpointConfig.Configurator {

    private static String mAppKey;
    private static String mAuthenticationToken;

    public static void setAuthentication(final String appKey, final String authenticationToken) {
        mAppKey = appKey;
        mAuthenticationToken = authenticationToken;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.remove("AUTHTOKEN");

        List<String> authList = new ArrayList<String>();
        authList.add(mAuthenticationToken);
        headers.put("AUTHTOKEN", authList);

        headers.remove("APPKEY");

        List<String> appList = new ArrayList<String>();
        appList.add(mAppKey);
        headers.put("APPKEY", appList);

        mAppKey = "";
        mAuthenticationToken = "";
    }
}