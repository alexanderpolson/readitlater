package com.orbitalsoftware.oauth;

import java.util.Properties;

public class PropertiesAuthTokenProvider implements AuthTokenProvider {

    private final AuthToken authToken;

    public PropertiesAuthTokenProvider(Properties properties) {
        authToken = AuthToken.builder().tokenKey(properties.getProperty("token_key")).tokenSecret(properties.getProperty("token_secret")).build();
    }

    @Override
    public AuthToken getAuthToken() {
        return authToken;
    }
}
