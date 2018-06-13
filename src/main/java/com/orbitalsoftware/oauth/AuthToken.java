package com.orbitalsoftware.oauth;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
public class AuthToken {
    private final String tokenKey;
    private final String tokenSecret;
}
