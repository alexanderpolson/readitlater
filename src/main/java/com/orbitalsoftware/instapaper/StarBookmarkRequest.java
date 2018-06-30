package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StarBookmarkRequest {
    private final Integer bookmarkId;
}
