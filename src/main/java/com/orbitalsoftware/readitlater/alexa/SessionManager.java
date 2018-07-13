package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.oauth.AuthToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

@Slf4j
public class SessionManager {

  private static final String CONSUMER_TOKEN_KEY = "ConsumerToken";
  private static final String CONSUMER_SECRET_KEY = "ConsumerSecret";
  private static final int MAX_PAGE_LENGTH = 800;

  // TODO: This is hardcoded to my personal account and should be deleted when a proper,
  // user-specific auth mechanism has been created.
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";
  private static final String TOKEN_KEY = "tokenKey";
  private static final String TOKEN_SECRET = "tokenSecret";

  private static final String PROMPT_FORMAT =
      "The next story in your queue is entitled \"%s\". It is %d pages long. What would you like to do?";

  @Getter private final HandlerInput input;
  private final ObjectMapper mapper;
  private InstapaperService instapaperService;
  private final AuthToken authToken;

  private static final String KEY_ARTICLES_TO_SKIP = "ArticlesToSkip";
  private static final String KEY_CURRENT_ARTICLE = "CurrentArticle";

  private List<Integer> articlesToSkip = new LinkedList<>();
  private Optional<Article> currentArticle = Optional.empty();

  public SessionManager(@NonNull HandlerInput input) throws Exception {
    this.input = input;
    this.mapper = new ObjectMapper();
    this.mapper.registerModule(new Jdk8Module());
    String token = System.getenv(CONSUMER_TOKEN_KEY);
    String secret = System.getenv(CONSUMER_SECRET_KEY);
    instapaperService = new InstapaperService(token, secret);
    authToken = getAuthToken();
    loadCustomerState();
  }

  public boolean hasArticle() {
    return currentArticle.isPresent();
  }

  public void incrementArticlePage() throws IOException {
    currentArticle.ifPresent((article) -> article.incrementCurrentPage());
    saveSessionState();
  }

  public final Optional<Article> getCurrentArticle() {
    return currentArticle;
  }

  private AuthToken getAuthToken() throws IOException {
    Properties authTokenProperties = new Properties();
    authTokenProperties.load(getClass().getClassLoader().getResourceAsStream(AUTH_TOKEN_RESOURCE));
    String tokenKey = authTokenProperties.getProperty(TOKEN_KEY);
    String tokenSecret = authTokenProperties.getProperty(TOKEN_SECRET);
    return AuthToken.builder().tokenKey(tokenKey).tokenSecret(tokenSecret).build();
  }

  private void saveSessionState() throws IOException {
    if (currentArticle.isPresent()) {
      String articleJson = mapper.writeValueAsString(currentArticle.get());
      log.info("Writing article JSON to session: {}", articleJson);
      input.getAttributesManager().getSessionAttributes().put(KEY_CURRENT_ARTICLE, articleJson);
      saveCustomerState();
    }
  }

  private void setNextArticle() throws IOException {
    currentArticle = getNextArticle();
    saveSessionState();
  }

  private void loadCustomerState() throws IOException {
    // Persisted Attributes
    String rawCustomerState =
        (String) input.getAttributesManager().getPersistentAttributes().get(KEY_ARTICLES_TO_SKIP);
    if (rawCustomerState == null) {
      this.articlesToSkip = new LinkedList<>();
    } else {
      try {
        this.articlesToSkip =
            mapper.readValue(rawCustomerState, new TypeReference<List<Integer>>() {});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Session attributes
    Map<String, Object> sessionAttributes =
        Optional.ofNullable(input.getAttributesManager().getSessionAttributes())
            .orElse(new HashMap<>());
    String articleJson = (String) sessionAttributes.get(KEY_CURRENT_ARTICLE);
    if (articleJson != null) {
      this.currentArticle =
          Optional.ofNullable(mapper.readValue((String) articleJson, Article.class));
    }
    if (!this.currentArticle.isPresent()) {
      setNextArticle();
    }
  }

  private void saveCustomerState() throws IOException {
    Map<String, Object> persistedAttributes = new HashMap<>();
    persistedAttributes.put(KEY_ARTICLES_TO_SKIP, mapper.writeValueAsString(articlesToSkip));
    input.getAttributesManager().setPersistentAttributes(persistedAttributes);
    input.getAttributesManager().savePersistentAttributes();
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
        DeleteBookmarkRequest.builder()
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void archiveCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.archiveBookmark(
        authToken,
        ArchiveBookmarkRequest.builder()
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void starCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.starBookmark(
        authToken,
        StarBookmarkRequest.builder()
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  private Optional<Article> getNextArticle() throws IOException {
    BookmarksListResponse response =
        instapaperService.getBookmarks(getAuthToken(), BookmarksListRequest.builder().build());
    // TODO: Add more detailed filtering.

    Optional<Bookmark> nextBookmark =
        response
            .getBookmarks()
            .stream()
            .filter(bookmark -> !bookmark.getUrl().startsWith("https://www.youtube.com"))
            .filter(bookmark -> !articlesToSkip.contains(bookmark.getBookmarkId()))
            .findFirst();
    if (nextBookmark.isPresent()) {
      log.info("Next article from bookmark: {}", nextBookmark.get());
      return Optional.of(articleForBookmark(nextBookmark.get()));
    } else {
      log.info("No bookmarks found.");
      return Optional.empty();
    }
  }

  private Article articleForBookmark(Bookmark bookmark) throws IOException {
    String bookmarkText =
        Jsoup.parse(instapaperService.getBookmarkText(authToken, bookmark.getBookmarkId())).text();
    log.info("Found text for bookmark: {}", bookmarkText);
    List<String> pages = ArticleTextPaginator.paginateText(bookmarkText, MAX_PAGE_LENGTH);
    log.info("Calculated pages from bookmark text");
    return Article.builder().bookmark(bookmark).pages(pages).build();
  }

  public Optional<String> getNextStoryTitle() {
    return currentArticle.map(a -> a.getBookmark().getTitle());
  }

  public Optional<String> getNextStoryPrompt() {
    return currentArticle.map(
        (article) ->
            String.format(PROMPT_FORMAT, article.getBookmark().getTitle(), article.numPages()));
  }

  // TODO: Add star, archive, or delete question at the end.
  public Optional<String> getArticleTextPrompt() throws IOException {
    return currentArticle.map(a -> a.getCurrentPageText());
  }

  public void skipCurrentArticle() throws IOException {
    if (currentArticle.isPresent()) {
      articlesToSkip.add(currentArticle.get().getBookmark().getBookmarkId());
      setNextArticle();
      saveCustomerState();
    }
  }
}
