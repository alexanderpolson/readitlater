package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnstarBookmarkRequest {
    private final Integer bookmarkId;
}
