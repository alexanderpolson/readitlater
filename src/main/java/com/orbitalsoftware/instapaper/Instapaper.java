package com.orbitalsoftware.instapaper;

import com.orbitalsoftware.oauth.AuthToken;
import com.orbitalsoftware.oauth.OAuth;
import lombok.NonNull;

public class Instapaper {

    private static final String HMACSHA1SignatureType = "HMAC-SHA1";

    private static final String BASE_API_URL = "https://www.instapaper.com/api/1";
    private static final String AUTHORIZATION_URL = BASE_API_URL + "/oauth/access_token";

    private final OAuth oAuth;

    public Instapaper(@NonNull final String consumerKey, @NonNull final String consumerSecret) throws Exception {
        this.oAuth = new OAuth(BASE_API_URL, consumerKey, consumerSecret);
    }

    // TODO: Use more specific exceptions.
    public AuthToken getAuthToken(@NonNull String username, @NonNull String password) throws Exception {
        return oAuth.getAccessToken(username, password);
    }
}
