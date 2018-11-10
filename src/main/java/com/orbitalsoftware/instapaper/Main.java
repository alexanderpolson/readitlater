package com.orbitalsoftware.instapaper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.harvest.ExecutionTimer;
import com.orbitalsoftware.oauth.AuthToken;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.jsoup.Jsoup;

public class Main {

  private static final String username = "alex@alexpolson.com";
  private static final String password = "uiwVxeAEq7gAi3fN";

  private static final String AUTH_TOKEN_PROPERTIES_PATH = "/Users/apolson/.instapaper_auth_token";

  private static final String O_AUTH_CONSUMER_TOKEN = "533e8f46e9c9473daecea3287c6a167d";
  private static final String O_AUTH_CONSUMER_SECRET = "fdda218ad197490a8814dce49d2bc393";

  private static final String TOKEN_KEY = "tokenKey";
  private static final String TOKEN_SECRET = "tokenSecret";

  private static final Integer BOOKMARK_ID = 1098094746;
  private static final Integer DELETED_ID = 1079451394;

  private final InstapaperService instapaperService;
  private final AuthToken authToken;

  public Main() throws Exception {
    instapaperService = new InstapaperService(O_AUTH_CONSUMER_TOKEN, O_AUTH_CONSUMER_SECRET);
    authToken = getAuthToken(AUTH_TOKEN_PROPERTIES_PATH);
  }

  private void run() throws Exception {
    //        System.out.println(instapaperService.verifyCredentials(authToken));
    //    updateReadProgress();
    //    storyText();
    //        archive();
    //        unarchive();
    //    getBookmarks();
    //        bookmarkParsing();
    timeCalls();
  }

  private void timeCalls() throws IOException {
    BookmarksListRequest request = BookmarksListRequest.builder().build();
    BookmarksListResponse response = instapaperService.getBookmarks(authToken, request);

    //    BookmarkId bookmarkId = BookmarkId.builder().id(1117369971).build();

    for (int passNum = 1; passNum <= 100; passNum++) {
      response
          .getBookmarks()
          .stream()
          .forEach((bookmark) -> getBookmarkText(bookmark.getBookmarkId()));
      //      getBookmarkText(bookmarkId);
    }

    ExecutionTimer.summarize();
  }

  private void getBookmarkText(BookmarkId bookmarkId) {
    try {
      String bookmarkText = instapaperService.getBookmarkText(authToken, bookmarkId.getId());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateReadProgress() throws Exception {
    instapaperService.updateReadProgress(
        authToken,
        UpdateReadProgressRequest.builder().bookmarkId(BOOKMARK_ID).progress(0.0).build());
  }

  private void storyText() throws IOException {
    String fullText = instapaperService.getBookmarkText(authToken, BOOKMARK_ID);
    long startTime = System.currentTimeMillis();
    String filteredText = Jsoup.parse(fullText).text();
    System.err.printf("Filtering text took %d msec.%n", System.currentTimeMillis() - startTime);
    System.out.println(filteredText);
  }

  private void bookmarkParsing() throws Exception {
    List<Map<String, Object>> results = null;
    try (InputStream bookmarksJson =
        this.getClass().getClassLoader().getResourceAsStream("bookmarks.list.json")) {
      ObjectMapper mapper = new ObjectMapper();
      results = mapper.readValue(bookmarksJson, new TypeReference<List<Map<String, Object>>>() {});
    }

    System.out.println(results);
  }

  private void getBookmarks() throws Exception {
    List<BookmarkId> haveBookmarks = new LinkedList<>();
    haveBookmarks.add(BookmarkId.builder().id(DELETED_ID).build());
    BookmarksListRequest request =
        BookmarksListRequest.builder().have(Optional.of(haveBookmarks)).build();
    BookmarksListResponse response = instapaperService.getBookmarks(authToken, request);
    //    System.out.println(response);
    //    request =
    //        BookmarksListRequest.builder()
    //            .have(
    //                Optional.of(response.getBookmarks().subList(0, response.getBookmarks().size()
    // - 2)))
    //            .build();
    //    response = instapaperService.getBookmarks(authToken, request);
    System.out.println(response.getBookmarks().stream().findFirst());
    System.out.println(response.getDeletedIds());
  }

  private void archive() throws Exception {
    ArchiveBookmarkRequest request =
        ArchiveBookmarkRequest.builder().bookmarkId(BOOKMARK_ID).build();
    System.err.printf(
        "Archived bookmark: %s%n", instapaperService.archiveBookmark(authToken, request));
    getBookmarks();
  }

  private void unarchive() throws Exception {
    UnarchiveBookmarkRequest request =
        UnarchiveBookmarkRequest.builder().bookmarkId(BOOKMARK_ID).build();
    System.err.printf(
        "Unarchived bookmark: %s%n", instapaperService.unarchiveBookmark(authToken, request));
    getBookmarks();
  }

  private void writeAuthTokenProvider(AuthToken authToken, String fileName) {
    try {
      Properties authTokenProperties = new Properties();
      authTokenProperties.put(TOKEN_KEY, authToken.getTokenKey());
      authTokenProperties.put(TOKEN_SECRET, authToken.getTokenSecret());
      authTokenProperties.store(
          new FileOutputStream(AUTH_TOKEN_PROPERTIES_PATH),
          "Auth tokens for InstapaperService API access");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean isBlank(String str) {
    return str == null || str.equals("");
  }

  private AuthToken getAuthToken(String fileName) throws Exception {
    try {
      Properties authTokenProperties = new Properties();
      authTokenProperties.load(new FileInputStream(AUTH_TOKEN_PROPERTIES_PATH));
      String tokenKey = authTokenProperties.getProperty(TOKEN_KEY);
      String tokenSecret = authTokenProperties.getProperty(TOKEN_SECRET);
      if (isBlank(tokenKey) || isBlank(tokenSecret)) {
        return getAndWriteToken(fileName);
      } else {
        return AuthToken.builder().tokenKey(tokenKey).tokenSecret(tokenSecret).build();
      }
    } catch (FileNotFoundException e) {
      return getAndWriteToken(fileName);
    }
  }

  private AuthToken getAndWriteToken(String fileName) throws Exception {
    AuthToken authToken = instapaperService.getAuthToken(username, password);
    writeAuthTokenProvider(authToken, fileName);
    return authToken;
  }

  public static final void main(String[] args) throws Exception {
    new Main().run();
  }
}
