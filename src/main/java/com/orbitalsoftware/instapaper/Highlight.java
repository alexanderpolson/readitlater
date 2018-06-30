package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Highlight {
    public static final String TYPE = "highlight";

    private final Integer highlightId;
    private final String text;
    private final String note;
    private final Integer bookmarkId;
    private final Integer time;
    private final Integer position;
}
