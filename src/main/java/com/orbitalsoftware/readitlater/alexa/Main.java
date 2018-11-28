package com.orbitalsoftware.readitlater.alexa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.harvest.ExecutionTimer;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.BookmarkId;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.UnarchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import com.orbitalsoftware.instapaper.auth.InstapaperAuthTokenProvider;
import com.orbitalsoftware.instapaper.auth.PropertiesInstapaperAuthTokenProvider;
import com.orbitalsoftware.oauth.OAuthCredentialsProvider;
import com.orbitalsoftware.oauth.PropertiesOAuthCredentialsProvider;
import com.orbitalsoftware.readitlater.alexa.article.ArticleTextPaginator;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

public class Main {

  private static final String AUTH_TOKEN_PROPERTIES_PATH = "/Users/apolson/.instapaper_auth_token";
  private static final String O_AUTH_CREDENTIALS_PROPERTIES_PATH =
      "/Users/apolson/.instapaper_oauth_credentials";

  private static final Integer BOOKMARK_ID = 985807278;
  private static final Integer DELETED_ID = 1079451394;

  private final Instapaper instapaper;

  public Main() throws Exception {

    OAuthCredentialsProvider OAuthCredentialsProvider =
        new PropertiesOAuthCredentialsProvider(
            new FileInputStream(O_AUTH_CREDENTIALS_PROPERTIES_PATH));
    InstapaperAuthTokenProvider authTokenProvider =
        new PropertiesInstapaperAuthTokenProvider(new FileInputStream(AUTH_TOKEN_PROPERTIES_PATH));
    instapaper = new InstapaperService(OAuthCredentialsProvider, authTokenProvider);
  }

  private void run() throws Exception {
    //        System.out.println(instapaperService.verifyCredentials(authToken));
    //    updateReadProgress();
    storyText();
    //        archive();
    //        unarchive();
    //    getBookmarks();
    //        bookmarkParsing();
    //    timeCalls();
  }

  private void timeCalls() throws Exception {
    BookmarksListRequest request = BookmarksListRequest.builder().build();
    BookmarksListResponse response = instapaper.getBookmarks(request);

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
      String bookmarkText = instapaper.getBookmarkText(bookmarkId.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateReadProgress() throws Exception {
    instapaper.updateReadProgress(
        UpdateReadProgressRequest.builder().bookmarkId(BOOKMARK_ID).progress(0.0).build());
  }

  private void storyText() throws Exception {
    String fullText = instapaper.getBookmarkText(BOOKMARK_ID);
    String filteredText = Jsoup.parse(fullText).text();
    List<String> pages = ArticleTextPaginator.paginateText(filteredText, 800);
    //    System.out.println(pages.stream().collect(Collectors.joining("\n")));

    System.out.println(StringEscapeUtils.escapeXml11(pages.get(3)));
  }

  private void bookmarkParsing() throws Exception {
    List<Map<String, Object>> results = null;
    try (InputStream bookmarksJson =
        this.getClass().getClassLoader().getResourceAsStream("bookmarks.list.json")) {
      ObjectMapper mapper = new ObjectMapper();
      results = mapper.readValue(bookmarksJson, new TypeReference<List<Map<String, Object>>>() {});
    }
  }

  private void getBookmarks() throws Exception {
    List<BookmarkId> haveBookmarks = new LinkedList<>();
    haveBookmarks.add(BookmarkId.builder().id(DELETED_ID).build());
    BookmarksListRequest request =
        BookmarksListRequest.builder().have(Optional.of(haveBookmarks)).build();
    BookmarksListResponse response = instapaper.getBookmarks(request);
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
    System.err.printf("Archived bookmark: %s%n", instapaper.archiveBookmark(request));
    getBookmarks();
  }

  private void unarchive() throws Exception {
    UnarchiveBookmarkRequest request =
        UnarchiveBookmarkRequest.builder().bookmarkId(BOOKMARK_ID).build();
    System.err.printf("Unarchived bookmark: %s%n", instapaper.unarchiveBookmark(request));
    getBookmarks();
  }

  private boolean isBlank(String str) {
    return str == null || str.equals("");
  }

  public static final void main(String[] args) throws Exception {
    new Main().run();
  }
}
