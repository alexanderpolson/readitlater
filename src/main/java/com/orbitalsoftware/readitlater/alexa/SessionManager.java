package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.model.Session;
import com.orbitalsoftware.instapaper.*;
import com.orbitalsoftware.oauth.AuthToken;
import lombok.NonNull;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class SessionManager {

  private static final String CONSUMER_TOKEN_KEY = "ConsumerToken";
  private static final String CONSUMER_SECRET_KEY = "ConsumerSecret";

  // TODO: This is hardcoded to my personal account and should be deleted when a proper,
  // user-specific auth mechanism has been created.
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";
  private static final String TOKEN_KEY = "tokenKey";
  private static final String TOKEN_SECRET = "tokenSecret";

  private static final String PROMPT_FORMAT =
      "The next story in your queue is entitled \"%s\". What would you like to do?";

  private final Session session;
  private InstapaperService instapaperService;
  private AuthToken authToken;

  private static final String KEY_CURRENT_ARTICLE = "CurrentArticle";

  private Optional<Bookmark> currentArticle = Optional.empty();

  public SessionManager(@NonNull Session session) throws Exception {
    this.session = session;
    String token = System.getProperty(CONSUMER_TOKEN_KEY);
    String secret = System.getProperty(CONSUMER_SECRET_KEY);
    instapaperService = new InstapaperService(token, secret);

    loadOrGetNextStory();
  }

  public boolean hasArticle() {
    return currentArticle.isPresent();
  }

  private AuthToken getAuthToken() throws IOException {
    Properties authTokenProperties = new Properties();
    authTokenProperties.load(getClass().getClassLoader().getResourceAsStream(AUTH_TOKEN_RESOURCE));
    String tokenKey = authTokenProperties.getProperty(TOKEN_KEY);
    String tokenSecret = authTokenProperties.getProperty(TOKEN_SECRET);
    return AuthToken.builder().tokenKey(tokenKey).tokenSecret(tokenSecret).build();
  }

  private void loadOrGetNextStory() throws IOException {
    currentArticle =
        Optional.ofNullable((Bookmark) session.getAttributes().get(KEY_CURRENT_ARTICLE));
    if (!currentArticle.isPresent()) {
      currentArticle = getNextStory();
      if (currentArticle.isPresent()) {
        session.getAttributes().put(KEY_CURRENT_ARTICLE, currentArticle.get());
      }
    }
  }

  private void clearCurrentArticle() {
    currentArticle = Optional.empty();
  }

  private void throwIfNoCurrentArticle() {
    if (!currentArticle.isPresent()) {
      throw new IllegalStateException("There are currently no articles available.");
    }
  }

  public void deleteCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.deleteBookmark(
        authToken,
        DeleteBookmarkRequest.builder().bookmarkId(currentArticle.get().getBookmarkId()).build());
    clearCurrentArticle();
  }

  public void archiveCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.archiveBookmark(
        authToken,
        ArchiveBookmarkRequest.builder().bookmarkId(currentArticle.get().getBookmarkId()).build());
    clearCurrentArticle();
  }

  public void starCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.starBookmark(
        authToken,
        StarBookmarkRequest.builder().bookmarkId(currentArticle.get().getBookmarkId()).build());
    clearCurrentArticle();
  }

  private Optional<Bookmark> getNextStory() throws IOException {
    // TODO: Add skipped stories here.
    BookmarksListResponse response =
        instapaperService.getBookmarks(getAuthToken(), BookmarksListRequest.builder().build());
    // TODO: Add more detailed filtering.
    return response
        .getBookmarks()
        .stream()
        .filter(bookmark -> !bookmark.getUrl().startsWith("https://www.youtube.com"))
        .findFirst();
  }

  public Optional<String> getNextStoryTitle() {
    return currentArticle.map(s -> s.getTitle());
  }

  public Optional<String> getNextStoryPrompt() {
    return getNextStoryTitle().map(t -> String.format(PROMPT_FORMAT, t));
  }

  // TODO: Add star, archive, or delete question at the end.
  public Optional<String> getArticleTextPrompt() throws IOException {
    Optional<String> result = Optional.empty();
    try {
      if (currentArticle.isPresent()) {
        String filteredStoryText =
            Jsoup.parse(
                    instapaperService.getBookmarkText(
                        getAuthToken(), currentArticle.get().getBookmarkId()))
                .text();
        result = Optional.of(filteredStoryText);
      }
    } catch (IOException e) {
      // TODO: Add logging.
    }
    return result;
  }
}
