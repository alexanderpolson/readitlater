package com.orbitalsoftware.oauth;

import lombok.Data;

@Data
public class StaticAuthTokenProvider implements AuthTokenProvider {
    private final AuthToken authToken;

    @Override
    public final AuthToken getAuthToken() {
        return authToken;
    }
}
