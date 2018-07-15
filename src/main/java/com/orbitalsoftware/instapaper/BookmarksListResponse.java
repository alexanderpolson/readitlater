package com.orbitalsoftware.instapaper;

import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookmarksListResponse {
  private final User user;
  private final List<Bookmark> bookmarks;
  @Builder.Default private final List<BookmarkId> deletedIds = new LinkedList<>();
}
