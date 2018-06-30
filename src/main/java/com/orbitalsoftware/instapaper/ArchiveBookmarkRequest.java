package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArchiveBookmarkRequest {
    private final Integer bookmarkId;
}
