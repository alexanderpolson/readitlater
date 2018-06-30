package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StarBookmarkResponse {
    private final Bookmark bookmark;
}
