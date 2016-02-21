package com.ogadai.ogadai_secure;

import android.accounts.AuthenticatorException;
import android.os.AsyncTask;
import android.util.Log;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by alee on 16/02/2016.
 */
public class ServerRequest implements IServerRequest {
    private String mAppKey;
    private String mAuthenticationToken;

    public ServerRequest(String appKey) {
        mAppKey = appKey;
    }

    public ServerRequest(String appKey, String authToken) {
        mAppKey = appKey;
        mAuthenticationToken = authToken;
    }

    public String get(String address) throws IOException, AuthenticationException {
        return request(address, "GET", null);
    }

    public String post(String address, String content) throws IOException, AuthenticationException {
        return request(address, "POST", content);
    }

    private String request(String address, String method, String content) throws IOException, AuthenticationException {
        URL url = new URL(address);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setRequestProperty("X-ZUMO-APPLICATION", mAppKey);

        if (mAuthenticationToken != null) {
            urlConnection.setRequestProperty("X-ZUMO-AUTH", mAuthenticationToken);
        }

        try {
            if (content != null) {
                //URLEncoder.encode(content, "UTF-8")
                byte[] postData = content.getBytes(StandardCharsets.UTF_8);

                urlConnection.setDoOutput(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestProperty("Content-Type", "application/json"); // x-www-form-urlencoded
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                urlConnection.setUseCaches(false);
                urlConnection.connect();

                DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
                outStream.write(postData);
                outStream.flush();
                outStream.close();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            return stringBuilder.toString();
        }
        catch(Exception e) {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 401) {
                throw new AuthenticationException(urlConnection.getResponseMessage());
            }
            throw e;
        }
        finally{
            urlConnection.disconnect();
        }
    }

}
