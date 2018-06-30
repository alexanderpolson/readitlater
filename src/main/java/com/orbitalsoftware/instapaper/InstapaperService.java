package com.orbitalsoftware.instapaper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.oauth.AuthToken;
import com.orbitalsoftware.oauth.OAuth;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * <a href="https://www.instapaper.com/api">Instapaper API</a>.
 */
public class InstapaperService {

    private static final String HMACSHA1SignatureType = "HMAC-SHA1";
    private static final String BASE_API_URL = "https://www.instapaper.com/api";

    private static final String AUTHORIZATION_URI = "/1/oauth/access_token";
    private static final String BOOKMARKS_LIST_URI = "/1/bookmarks/list";
    private static final String ARCHIVE_URI = "/1/bookmarks/archive";
    private static final String UNARCHIVE_URI = "/1/bookmarks/unarchive";
    private static final String DELETE_URI = "/1/bookmarks/delete";
    private static final String STAR_URI = "/1/bookmarks/star";
    private static final String UNSTAR_URI = "/1/bookmarks/unstar";

    private static final String KEY_ELEMENT_TYPE = "type";

    private final OAuth oAuth;
    private final ObjectMapper objectMapper;

    public InstapaperService(@NonNull final String consumerKey, @NonNull final String consumerSecret) throws Exception {
        this.oAuth = new OAuth(BASE_API_URL, AUTHORIZATION_URI, consumerKey, consumerSecret);
        objectMapper = new ObjectMapper();
    }

    // TODO: Use more specific exceptions.
    public AuthToken getAuthToken(@NonNull String username, @NonNull String password) throws Exception {
        return oAuth.getAccessToken(username, password);
    }

    // TODO: The return type may need to be generalized to adjust for different response content.
    private Stream<ResponseElement> makeRequest(@NonNull AuthToken authToken, @NonNull String requestUri, @NonNull Optional<Map<String, String>> parameters) throws IOException {
        List<Map<String, Object>> response = objectMapper.readValue(oAuth.makeRequest(Optional.of(authToken), requestUri, parameters), new TypeReference<List<Map<String, Object>>>() {});
        return response.stream().map(ResponseElement::new);
    }

    // TODO: Add parameters
    public BookmarksListResponse getBookmarks(@NonNull AuthToken authToken, @NonNull BookmarksListRequest request) throws IOException {
        // TODO: Create generic code to add parameters.
        BookmarksListResponse.BookmarksListResponseBuilder responseBuilder =  BookmarksListResponse.builder();
        List<Bookmark> bookmarks = new LinkedList<Bookmark>();
        responseBuilder.bookmarks(bookmarks);
        makeRequest(authToken, BOOKMARKS_LIST_URI, Optional.empty()).forEach(element -> {
            switch (element.getType()) {
                case (User.TYPE):
                    responseBuilder.user(User.forResponseElement(element));
                    break;
                case (Bookmark.TYPE):
                    bookmarks.add(Bookmark.forResponseElement(element));
                    break;
                default:
                    System.err.printf("Don't know how to handle element of type \"%s\"%n", element.getType());
            }
        });
        return responseBuilder.build();
    }

    private Optional<Bookmark> firstBookmarkFromResponse(Stream<ResponseElement> elementStream) {
        return elementStream.filter(Bookmark::isBookmark).findFirst().map(element -> Bookmark.forResponseElement(element));
    }

    // TODO: DRY up these methods.
    // TODO: Create more elegant way to populate parameter Map.
    public ArchiveBookmarkResponse archiveBookmark(@NonNull AuthToken authoToken, ArchiveBookmarkRequest request) throws IOException {
        ArchiveBookmarkResponse.ArchiveBookmarkResponseBuilder responseBuilder = ArchiveBookmarkResponse.builder();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("bookmark_id", request.getBookmarkId().toString());
        Optional<Bookmark> bookmark = firstBookmarkFromResponse(makeRequest(authoToken, ARCHIVE_URI, Optional.of(parameters)));
        responseBuilder.bookmark(bookmark.get());
        return responseBuilder.build();
    }

    public StarBookmarkResponse archiveBookmark(@NonNull AuthToken authoToken, StarBookmarkRequest request) throws IOException {
        StarBookmarkResponse.StarBookmarkResponseBuilder responseBuilder = StarBookmarkResponse.builder();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("bookmark_id", request.getBookmarkId().toString());
        Optional<Bookmark> bookmark = firstBookmarkFromResponse(makeRequest(authoToken, STAR_URI, Optional.of(parameters)));
        responseBuilder.bookmark(bookmark.get());
        return responseBuilder.build();
    }

    public UnstarBookmarkResponse unstarBookmark(@NonNull AuthToken authoToken, UnstarBookmarkRequest request) throws IOException {
        UnstarBookmarkResponse.UnstarBookmarkResponseBuilder responseBuilder = UnstarBookmarkResponse.builder();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("bookmark_id", request.getBookmarkId().toString());
        Optional<Bookmark> bookmark = firstBookmarkFromResponse(makeRequest(authoToken, UNSTAR_URI, Optional.of(parameters)));
        responseBuilder.bookmark(bookmark.get());
        return responseBuilder.build();
    }

    public UnarchiveBookmarkResponse unarchiveBookmark(@NonNull AuthToken authoToken, UnarchiveBookmarkRequest request) throws IOException {
        UnarchiveBookmarkResponse.UnarchiveBookmarkResponseBuilder responseBuilder = UnarchiveBookmarkResponse.builder();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("bookmark_id", request.getBookmarkId().toString());
        Optional<Bookmark> bookmark = firstBookmarkFromResponse(makeRequest(authoToken, UNARCHIVE_URI, Optional.of(parameters)));
        responseBuilder.bookmark(bookmark.get());
        return responseBuilder.build();
    }

    public void deleteBookmark(@NonNull AuthToken authoToken, DeleteBookmarkRequest request) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("bookmark_id", request.getBookmarkId().toString());
        makeRequest(authoToken, DELETE_URI, Optional.of(parameters));
    }
}
