package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArchiveBookmarkResponse {
    private final Bookmark bookmark;
}
