package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnarchiveBookmarkRequest {
    private final Integer bookmarkId;
}
