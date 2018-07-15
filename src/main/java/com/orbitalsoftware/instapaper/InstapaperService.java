package com.orbitalsoftware.instapaper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.oauth.AuthToken;
import com.orbitalsoftware.oauth.OAuth;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

/** <a href="https://www.instapaper.com/api">Instapaper API</a>. */
public class InstapaperService {

  private static final String HMACSHA1SignatureType = "HMAC-SHA1";
  private static final String BASE_API_URL = "https://www.instapaper.com/api/1.1";

  private static final String AUTHORIZATION_URI = "/oauth/access_token";
  private static final String VERIFY_CREDENTIALS = "/account/verify_credentials";
  private static final String BOOKMARKS_LIST_URI = "/bookmarks/list";
  private static final String BOOKMARK_GET_TEXT_URI = "/bookmarks/get_text";
  private static final String ARCHIVE_URI = "/bookmarks/archive";
  private static final String UNARCHIVE_URI = "/bookmarks/unarchive";
  private static final String DELETE_URI = "/bookmarks/delete";
  private static final String STAR_URI = "/bookmarks/star";
  private static final String UNSTAR_URI = "/bookmarks/unstar";
  private static final String UPDATE_READ_PROGRESS_URI = "/bookmarks/update_read_progress";

  // For bookmarks/list operation.
  private static final String KEY_HAVE = "have";
  private static final String KEY_BOOKMARKS = "bookmarks";
  private static final String KEY_USER = "user";
  private static final String KEY_HIGHLIGHTS = "highlights";

  private static final String KEY_ELEMENT_TYPE = "type";

  private final OAuth oAuth;
  private final ObjectMapper objectMapper;

  public InstapaperService(@NonNull final String consumerKey, @NonNull final String consumerSecret)
      throws Exception {
    this.oAuth = new OAuth(BASE_API_URL, AUTHORIZATION_URI, consumerKey, consumerSecret);
    objectMapper = new ObjectMapper();
  }

  // TODO: Use more specific exceptions.
  public AuthToken getAuthToken(@NonNull String username, @NonNull String password)
      throws Exception {
    return oAuth.getAccessToken(username, password);
  }

  // TODO: The return type may need to be generalized to adjust for different response content.
  private <T> T makeRequest(
      @NonNull AuthToken authToken,
      @NonNull String requestUri,
      @NonNull Optional<Map<String, String>> parameters,
      TypeReference<T> responseType)
      throws IOException {
    String responseJson = oAuth.makeRequest(Optional.of(authToken), requestUri, parameters);
    System.err.printf("Response JSON: %s%n", responseJson);
    return objectMapper.readValue(responseJson, responseType);
  }

  private Stream<ResponseElement> makeResponseElementRequest(
      @NonNull AuthToken authToken,
      @NonNull String requestUri,
      @NonNull Optional<Map<String, String>> parameters)
      throws IOException {
    return makeRequest(
            authToken, requestUri, parameters, new TypeReference<List<Map<String, Object>>>() {})
        .stream()
        .map(ResponseElement::new);
  }

  public VerifyCredentialsResponse verifyCredentials(@NonNull AuthToken authToken)
      throws IOException {
    return VerifyCredentialsResponse.builder()
        .user(
            makeResponseElementRequest(authToken, VERIFY_CREDENTIALS, Optional.empty())
                .findFirst()
                .map(User::forResponseElement)
                .get())
        .build();
  }

  // TODO: Add parameters
  public BookmarksListResponse getBookmarks(
      @NonNull AuthToken authToken, @NonNull BookmarksListRequest request) throws IOException {
    // TODO: Create generic code to add parameters.
    Map<String, String> parameters = new HashMap<>();
    request
        .getHave()
        .ifPresent(
            bookmarks ->
                parameters.put(
                    KEY_HAVE,
                    bookmarks
                        .stream()
                        .map(bookmark -> bookmark.toHaveId())
                        .collect(Collectors.joining(","))));

    Map<String, Object> response =
        makeRequest(
            authToken,
            BOOKMARKS_LIST_URI,
            Optional.of(parameters),
            new TypeReference<Map<String, Object>>() {});

    return BookmarksListResponse.builder()
        .bookmarks(
            ((List<Map<String, Object>>) response.get(KEY_BOOKMARKS))
                .stream()
                .map(Bookmark::forResponseElement)
                .collect(Collectors.toList()))
        .user(User.forResponseElement(((Map<String, Object>) response.get(KEY_USER))))
        // TODO: Add highlights
        .build();
  }

  public String getBookmarkText(@NonNull AuthToken authToken, @NonNull Integer bookmarkId)
      throws IOException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", bookmarkId.toString());
    return oAuth.makeRequest(
        Optional.of(authToken), BOOKMARK_GET_TEXT_URI, Optional.of(parameters));
  }

  private Optional<Bookmark> firstBookmarkFromResponse(Stream<ResponseElement> elementStream) {
    return elementStream
        .filter(Bookmark::isBookmark)
        .findFirst()
        .map(element -> Bookmark.forResponseElement(element));
  }

  // TODO: DRY up these methods.
  // TODO: Create more elegant way to populate parameter Map.
  public ArchiveBookmarkResponse archiveBookmark(
      @NonNull AuthToken authToken, ArchiveBookmarkRequest request) throws IOException {
    ArchiveBookmarkResponse.ArchiveBookmarkResponseBuilder responseBuilder =
        ArchiveBookmarkResponse.builder();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    Optional<Bookmark> bookmark =
        firstBookmarkFromResponse(
            makeResponseElementRequest(authToken, ARCHIVE_URI, Optional.of(parameters)));
    responseBuilder.bookmark(bookmark.get());
    return responseBuilder.build();
  }

  public StarBookmarkResponse starBookmark(
      @NonNull AuthToken authToken, StarBookmarkRequest request) throws IOException {
    StarBookmarkResponse.StarBookmarkResponseBuilder responseBuilder =
        StarBookmarkResponse.builder();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    Optional<Bookmark> bookmark =
        firstBookmarkFromResponse(
            makeResponseElementRequest(authToken, STAR_URI, Optional.of(parameters)));
    responseBuilder.bookmark(bookmark.get());
    return responseBuilder.build();
  }

  public UnstarBookmarkResponse unstarBookmark(
      @NonNull AuthToken authToken, UnstarBookmarkRequest request) throws IOException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    Optional<Bookmark> bookmark =
        firstBookmarkFromResponse(
            makeResponseElementRequest(authToken, UNSTAR_URI, Optional.of(parameters)));
    return UnstarBookmarkResponse.builder().bookmark(bookmark.get()).build();
  }

  public UnarchiveBookmarkResponse unarchiveBookmark(
      @NonNull AuthToken authToken, UnarchiveBookmarkRequest request) throws IOException {
    UnarchiveBookmarkResponse.UnarchiveBookmarkResponseBuilder responseBuilder =
        UnarchiveBookmarkResponse.builder();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    Optional<Bookmark> bookmark =
        firstBookmarkFromResponse(
            makeResponseElementRequest(authToken, UNARCHIVE_URI, Optional.of(parameters)));
    responseBuilder.bookmark(bookmark.get());
    return responseBuilder.build();
  }

  public void deleteBookmark(@NonNull AuthToken authToken, DeleteBookmarkRequest request)
      throws IOException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    makeResponseElementRequest(authToken, DELETE_URI, Optional.of(parameters));
  }

  public Bookmark updateReadProgress(
      @NonNull AuthToken authToken, @NonNull UpdateReadProgressRequest request) throws IOException {
    if (request.getProgress() < 0 || request.getProgress() > 1) {
      throw new IllegalArgumentException("Progress needs to be between 0 and 1 (inclusive).");
    }
    Long timestamp = System.currentTimeMillis() / 1000;
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bookmark_id", request.getBookmarkId().toString());
    parameters.put("progress", request.getProgress().toString());
    parameters.put("progress_timestamp", timestamp.toString());
    return firstBookmarkFromResponse(
            makeResponseElementRequest(
                authToken, UPDATE_READ_PROGRESS_URI, Optional.of(parameters)))
        .get();
  }
}
