package com.orbitalsoftware.instapaper;

import com.orbitalsoftware.oauth.AuthToken;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

@Builder()
@Data
public class BookmarksListRequest {
    @Builder.Default
    private final Optional<Integer> limit = Optional.empty();
    @Builder.Default
    private final Optional<String> folderId = Optional.empty(); // unread (default), starred, archive, or a folder_id from folders/list
    @Builder.Default
    private final Optional<List<Bookmark>> have = Optional.empty(); // A list of bookmark_ids that the client already has (see API documentation)
    @Builder.Default
    private final Optional<List<Integer>> highlights = Optional.empty(); // A list of highlight ids that the client already has (see API documentation)
    // TODO: Add helper methods to build up have and highlights attributes.
}
