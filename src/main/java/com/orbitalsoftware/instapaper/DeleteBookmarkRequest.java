package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DeleteBookmarkRequest {
    private final Integer bookmarkId;
}
