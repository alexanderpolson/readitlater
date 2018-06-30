package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnarchiveBookmarkResponse {
    private final Bookmark bookmark;
}
