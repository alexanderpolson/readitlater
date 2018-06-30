package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class User {
    public static final String TYPE = "user";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_SUBSCRIPTION_ACTIVE = "subscription_is_valid";
    private static final String KEY_USERNAME = "username";

    private final Integer userId;
    private final String username;
    private final boolean isSubscriptionActive;

    static User forResponseElement(Map<String, Object> element) {
        return forResponseElement(new ResponseElement(element));
    }

    static User forResponseElement(ResponseElement element) {
        if (!element.getType().equals(TYPE)) {
            throw new IllegalArgumentException("Provided element is not of type user.");
        }

        return builder()
                .userId(element.getAsType(KEY_USER_ID, Integer.class))
                .username(element.get(KEY_USERNAME))
                .isSubscriptionActive(element.getAsBoolean(KEY_IS_SUBSCRIPTION_ACTIVE))
                .build();
    }
}
