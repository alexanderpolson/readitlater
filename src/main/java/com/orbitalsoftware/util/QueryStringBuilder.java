package com.orbitalsoftware.util;

import lombok.NonNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryStringBuilder {

    private static final String KEY_VALUE_DELIMITER = "=";
    public static final String PARAMETER_DELIMITER = "&";

    private static final int INDEX_KEY = 0;
    private static final int INDEX_VALUE = 1;

    private Map<String, String> parameters;

    public QueryStringBuilder() {
        parameters = new TreeMap<String, String>();
    }

    public QueryStringBuilder addParameters(@NonNull Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    public QueryStringBuilder addParameter(@NonNull String key, @NonNull String value) {
        this.parameters.put(key, value);
        return this;
    }

    public String build() {
        return parameters.entrySet().stream().map((entry) -> String.format("%s%s%s",
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                KEY_VALUE_DELIMITER,
                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining(PARAMETER_DELIMITER));
    }

    public static Map<String, String> toParameters(String queryString) {
        Map<String, String> parameters = new HashMap<>();
        Stream.of(queryString.split(PARAMETER_DELIMITER)).forEach(paramAndValue -> {
            String[] parts = paramAndValue.split(KEY_VALUE_DELIMITER);
            // TODO: Need to decode?
            String key = parts[INDEX_KEY];
            String value = parts[INDEX_VALUE];
            parameters.put(key, value);
        });
        return parameters;
    }
}
