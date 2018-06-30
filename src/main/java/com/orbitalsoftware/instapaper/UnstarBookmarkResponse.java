package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnstarBookmarkResponse {
    private final Bookmark bookmark;
}
