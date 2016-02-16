package com.ogadai.ogadai_secure;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by alee on 16/02/2016.
 */
public class ServerRequest implements IServerRequest {
    private String mAppKey;
    private String mAuthenticationToken;

    public ServerRequest(String appKey, String authToken) {
        mAppKey = appKey;
        mAuthenticationToken = authToken;
    }

    public String get(String address) throws IOException {
        return request(address, "GET");
    }

    private String request(String address, String method) throws IOException {
        URL url = new URL(address);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setRequestProperty("X-ZUMO-APPLICATION", mAppKey);
        urlConnection.setRequestProperty("X-ZUMO-AUTH", mAuthenticationToken);

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            return stringBuilder.toString();
        }
        finally{
            urlConnection.disconnect();
        }
    }

}
