package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VerifyCredentialsResponse {
    private final User user;
}
