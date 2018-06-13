package com.orbitalsoftware.instapaper;

import com.orbitalsoftware.oauth.*;

import java.io.*;
import java.util.Properties;

public class Main {

    private static final String username = "alex@alexpolson.com";
    private static final String password = "uiwVxeAEq7gAi3fN";

    private static final String AUTH_TOKEN_PROPERTIES_PATH = "/Users/apolson/.instapaper_auth_token";

    private static final String O_AUTH_CONSUMER_TOKEN = "533e8f46e9c9473daecea3287c6a167d";
    private static final String O_AUTH_CONSUMER_SECRET = "fdda218ad197490a8814dce49d2bc393";

    private static final String TOKEN_KEY = "tokenKey";
    private static final String TOKEN_SECRET = "tokenSecret";

    private final Instapaper instapaper;

    public Main() throws Exception {
        instapaper = new Instapaper(O_AUTH_CONSUMER_TOKEN, O_AUTH_CONSUMER_SECRET);
    }

    private void run() throws Exception {
        AuthToken authToken = getAuthToken(AUTH_TOKEN_PROPERTIES_PATH)
        System.out.println(authToken);
    }

    private void writeAuthTokenProvider(AuthToken authToken, String fileName) {
        try {
            Properties authTokenProperties = new Properties();
            authTokenProperties.put(TOKEN_KEY, authToken.getTokenKey());
            authTokenProperties.put(TOKEN_SECRET, authToken.getTokenSecret());
            authTokenProperties.store(new FileOutputStream(AUTH_TOKEN_PROPERTIES_PATH), "Auth tokens for Instapaper API access");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.equals("");
    }

    private AuthToken getAuthToken(String fileName) throws Exception {
        try {
            Properties authTokenProperties = new Properties();
            authTokenProperties.load(new FileInputStream(AUTH_TOKEN_PROPERTIES_PATH));
            String tokenKey = authTokenProperties.getProperty(TOKEN_KEY);
            String tokenSecret = authTokenProperties.getProperty(TOKEN_SECRET);
            if (isBlank(tokenKey) || isBlank(tokenSecret)) {
                return getAndWriteToken(fileName);
            } else {
                return AuthToken.builder().tokenKey(tokenKey).tokenSecret(tokenSecret).build();
            }
        } catch (FileNotFoundException e) {
            return getAndWriteToken(fileName);
        }
    }

    private AuthToken getAndWriteToken(String fileName) throws Exception {
        AuthToken authToken = instapaper.getAuthToken(username, password);
        writeAuthTokenProvider(authToken, fileName);
        return authToken;
    }

    public static final void main(String[] args) throws Exception {
        new Main().run();
    }
}
