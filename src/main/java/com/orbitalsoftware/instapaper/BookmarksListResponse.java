package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BookmarksListResponse {
    private final User user;
    private final List<Bookmark> bookmarks;
}
