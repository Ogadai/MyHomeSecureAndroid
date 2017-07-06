package com.ogadai.ogadai_secure;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by alee on 16/02/2016.
 */
public class ServerRequest implements IServerRequest {
    public static String APPKEY = "RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90";
    public static String HOSTNAME = "ogadai-secure.azure-mobile.net"; // local "10.0.2.2:8080"
    public static String ROOTPATH = "https://" + HOSTNAME + "/";
    public static String ROOTAPIPATH = ROOTPATH + "api/";

    public static String get(Context context, String path) throws IOException, AuthenticationException {
        return requestWithAuth(context, "GET", path, null);
    }

    public static <T> T get(Context context, String path, Class<T> classOfT) throws IOException, AuthenticationException, JsonSyntaxException {
        String response = get(context, path);
        return gson().fromJson(response, classOfT);
    }

    public static String post(Context context, String path, String content) throws IOException, AuthenticationException {
        return requestWithAuth(context, "POST", path, content);
    }

    public static <T> String post(Context context, String path, T content) throws IOException, AuthenticationException {
        String message = gson().toJson(content);

        return post(context, path, message);
    }
    public static <T> T post(Context context, String path, Class<T> classOfT) throws IOException, AuthenticationException, JsonSyntaxException {
        String response = post(context, path, (String) null);
        return gson().fromJson(response, classOfT);
    }
    public static <T, U> U post(Context context, String path, T content, Class<U> classOfU) throws IOException, AuthenticationException, JsonSyntaxException {
        Gson gson = gson();
        String message = gson.toJson(content);

        String response = post(context, path, message);

        return gson.fromJson(response, classOfU);
    }

    private static String requestWithAuth(Context context, String method, String path, String content) throws IOException, AuthenticationException {
        String authToken = null;
        if (context != null) {
            authToken = getAuthenticationToken(context);
        }

        ServerRequest serverRequest = new ServerRequest(APPKEY, authToken);
        return serverRequest.request(method, ROOTAPIPATH + path, content);
    }

    public static HttpURLConnection setupConnectionWithAuth(Context context, String method, String path, String content) throws IOException, AuthenticationException {
        String authToken = null;
        if (context != null) {
            authToken = getAuthenticationToken(context);
        }

        ServerRequest serverRequest = new ServerRequest(APPKEY, authToken);
        return serverRequest.setupConnection(method, ROOTAPIPATH + path, content);
    }

    private static String getAuthenticationToken(Context context) {
        ITokenCache googleToken = new TokenCache(context, TokenCache.GOOGLE_PREFFILE);
        CachedToken cachedToken = googleToken.get();
        if (cachedToken == null) {
            Logger.e("geofence", "No cached token available");
        }
        return cachedToken.getToken();
    }

    private static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new gsonUTCdateAdapter())
                .create();
    }

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
        return request("GET", address, null);
    }

    public String post(String address, String content) throws IOException, AuthenticationException {
        return request("POST", address, content);
    }

    public String request(String method, String address, String content) throws IOException, AuthenticationException {

        HttpURLConnection urlConnection = setupConnection(method, address, content);
        try {
            InputStream inputStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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
            throw new IOException("Server error. Response code: " + Integer.toString(responseCode) + " - " + urlConnection.getResponseMessage(), e);
        }
        finally {
            urlConnection.disconnect();
        }
    }

    public HttpURLConnection setupConnection(String method, String address, String content) throws IOException, AuthenticationException {
        URL url = new URL(address);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (method.compareTo("GET") != 0) {
            urlConnection.setRequestMethod(method);
        }
        if (mAppKey != null) {
            urlConnection.setRequestProperty("APPKEY", mAppKey);
        }

        if (mAuthenticationToken != null) {
            urlConnection.setRequestProperty("AUTHTOKEN", mAuthenticationToken);
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

            return urlConnection;
        }
        catch(Exception e) {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 401) {
                throw new AuthenticationException(urlConnection.getResponseMessage());
            }
            throw new IOException("Server error. Response code: " + Integer.toString(responseCode) + " - " + urlConnection.getResponseMessage(), e);
        }
    }

    public static class gsonUTCdateAdapter implements JsonSerializer<Date>,JsonDeserializer<Date> {

        private final DateFormat dateFormat1;
        private final DateFormat dateFormat2;

        public gsonUTCdateAdapter() {
            dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));

            dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(dateFormat1.format(date));
        }

        @Override
        public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            String dateString = jsonElement.getAsString();

            try {
                return dateFormat1.parse(dateString);
            } catch (ParseException fail1) {
                try {
                    return dateFormat2.parse(dateString);
                } catch (ParseException e) {
                    throw new JsonParseException(e);
                }
            }
        }
    }
}
