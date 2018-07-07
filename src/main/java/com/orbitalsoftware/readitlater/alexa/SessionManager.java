package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.oauth.AuthToken;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.NonNull;
import org.jsoup.Jsoup;

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

  @Getter private final HandlerInput input;
  private final ObjectMapper mapper;
  private InstapaperService instapaperService;
  private final AuthToken authToken;

  private static final String KEY_CURRENT_ARTICLE = "CurrentArticle";

  private Optional<Bookmark> currentArticle = Optional.empty();

  public SessionManager(@NonNull HandlerInput input) throws Exception {
    this.input = input;
    this.mapper = new ObjectMapper();
    String token = System.getenv(CONSUMER_TOKEN_KEY);
    String secret = System.getenv(CONSUMER_SECRET_KEY);
    instapaperService = new InstapaperService(token, secret);
    authToken = getAuthToken();
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
        Optional.ofNullable(
                input.getAttributesManager().getSessionAttributes().get(KEY_CURRENT_ARTICLE))
            .flatMap(rawArticle -> bookmarkFromJson((String) rawArticle));
    if (!currentArticle.isPresent()) {
      currentArticle = getNextStory();
      if (currentArticle.isPresent()) {
        input
            .getAttributesManager()
            .getSessionAttributes()
            .put(KEY_CURRENT_ARTICLE, mapper.writeValueAsString(currentArticle.get()));
      }
    }
  }

  private Optional<Bookmark> bookmarkFromJson(String json) {
    try {
      return Optional.of(mapper.readValue(json, Bookmark.class));
    } catch (IOException e) {
      return Optional.empty();
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
                        authToken, currentArticle.get().getBookmarkId()))
                .text();
        result = Optional.of(filteredStoryText);
      }
    } catch (IOException e) {
      // TODO: Add logging.
    }
    return result;
  }
}
