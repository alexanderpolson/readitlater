package com.orbitalsoftware.instapaper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
class ResponseElement {

    private static final String KEY_ELEMENT_TYPE = "type";

    private final Map<String, Object> responseElement;

    public String getType() {
        return getAsType(KEY_ELEMENT_TYPE, String.class);
    }

    public <T> T getAsType(@NonNull String key, Class<T> clazz) {
        return clazz.cast(responseElement.get(key));
    }

    /**
     * Treats 1 as true...
     * @param key
     * @return
     */
    public boolean getAsBoolean(@NonNull String key) {
        String value = get(key);
        return (value != null && value.equals("1")) || Boolean.parseBoolean(value);
    }

    public String get(@NonNull String key) {
        return getAsType(key, String.class);
    }
}
