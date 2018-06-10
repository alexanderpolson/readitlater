package com.orbitalsoftware.instapaper;

import lombok.NonNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class QueryStringBuilder {

    private static final String KEY_VALUE_DELIMITER = "=";
    public static final String PARAMETER_DELIMITER = "&";

    private Map<String, String> parameters;

    public QueryStringBuilder() {
        // TODO: The parameter sorting is intended to accommodate Oauth but shouldn't really be handled here.
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

    public static final void main(String[] args) {
        QueryStringBuilder builder = new QueryStringBuilder()
                .addParameter("parameter 1", "value1")
                .addParameter("parameter & 2", "value2");

        System.out.printf("Query String: %s\n", builder.build());
        System.exit(0);
    }
}
