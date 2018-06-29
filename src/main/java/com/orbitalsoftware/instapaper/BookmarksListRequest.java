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
    private final AuthToken authToken;
    private final Optional<Integer> limit;
    private final Optional<String> folderId; // unread (default), starred, archive, or a folder_id from folders/list
    private final Optional<List<Integer>> have; // A list of bookmark_ids that the client already has (see API documentation)
    private final Optional<List<Integer>> highlights; // A list of highlight ids that the client already has (see API documentation)
    // TODO: Add helper methods to build up have and highlights attributes.
}
