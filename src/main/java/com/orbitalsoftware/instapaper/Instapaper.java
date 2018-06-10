package com.orbitalsoftware.instapaper;

import lombok.NonNull;

import java.net.URI;

public class Instapaper { //extends DefaultApi10a {

    private static final String HMACSHA1SignatureType = "HMAC-SHA1";

    private static final String BASE_API_URL = "https://www.instapaper.com/api/1";
    private static final String AUTHORIZATION_URL = BASE_API_URL + "/oauth/access_token";

    private final OAuth oAuth;

    public Instapaper(@NonNull final String consumerKey, @NonNull final String consumerSecret) throws Exception {
        this.oAuth = new OAuth(consumerKey, consumerSecret);
    }

    // TODO: Use more specific exceptions.
    public String getAuthToken(@NonNull String username, @NonNull String password) throws Exception {
        return oAuth.getAccessToken(username, password);
    }
}
