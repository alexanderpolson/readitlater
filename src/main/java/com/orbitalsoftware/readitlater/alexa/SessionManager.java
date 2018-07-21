package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarkId;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import com.orbitalsoftware.oauth.AuthToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
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
      "The next story in your queue is entitled \"%s\" and their are %d pages remaining. What would you like to do?";

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
    // Updates reading progress for the page, once the next page is started. This prevents from
    // moving the progress up too aggressively.
    currentArticle.ifPresent(
        (article) -> {
          tryUpdatingReadProgress(article);
          article.incrementCurrentPage();
        });
    saveSessionState();
  }

  private void tryUpdatingReadProgress(Article article) {
    try {
      if (article.getCurrentPage() != 0) {
        Bookmark updatedBookmark =
            instapaperService.updateReadProgress(
                authToken,
                UpdateReadProgressRequest.builder()
                    .progress(article.progressPercentage())
                    .bookmarkId(article.getBookmark().getBookmarkId().getId())
                    .build());
      }
    } catch (IOException e) {
      log.warn("Failed to update read progress for bookmark {}. Skipping");
    }
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
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId().getId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void archiveCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.archiveBookmark(
        authToken,
        ArchiveBookmarkRequest.builder()
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId().getId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void starCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.starBookmark(
        authToken,
        StarBookmarkRequest.builder()
            .bookmarkId(currentArticle.get().getBookmark().getBookmarkId().getId())
            .build());
    // Also archive it so we move to the next article.
    archiveCurrentArticle();
  }

  private void removeDeletedBookmarks(List<BookmarkId> deletedBookmarks) throws IOException {
    int sizeBeforeRemoval = articlesToSkip.size();
    articlesToSkip.removeAll(
        deletedBookmarks.stream().map(b -> b.getId()).collect(Collectors.toList()));
    if (sizeBeforeRemoval != articlesToSkip.size()) {
      saveCustomerState();
    }
  }

  private Optional<Article> getNextArticle() throws IOException {
    BookmarksListResponse response =
        instapaperService.getBookmarks(
            getAuthToken(),
            BookmarksListRequest.builder()
                .have(Optional.of(BookmarkId.forIds(articlesToSkip)))
                .build());
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
      removeDeletedBookmarks(response.getDeletedIds());
      return Optional.of(articleForBookmark(nextBookmark.get()));
    } else {
      log.info("No bookmarks found.");
      return Optional.empty();
    }
  }

  private int calculateCurrentPage(Bookmark bookmark, List<String> pages) {
    // A return value of 0 essentially means the article hasn't been read yet or at least not gotten
    // past the first page. This doesn't feel quite right as it's muddling the meaning of this data
    // because of how state is updated between requests (progress for a page is only updated once
    // the
    // next page is requested.
    // TODO: Reevaluate this and in general clean up this code.
    return Double.valueOf((Math.floor(bookmark.getProgress() * pages.size()))).intValue();
  }

  private Article articleForBookmark(Bookmark bookmark) throws IOException {
    String bookmarkText =
        StringEscapeUtils.escapeXml11(
            Jsoup.parse(
                    instapaperService.getBookmarkText(authToken, bookmark.getBookmarkId().getId()))
                .text());
    log.info("Found text for bookmark: {}", bookmarkText);
    List<String> pages = ArticleTextPaginator.paginateText(bookmarkText, MAX_PAGE_LENGTH);
    log.info("Calculated pages from bookmark text");

    return Article.builder()
        .bookmark(bookmark)
        .pages(pages)
        .currentPage(calculateCurrentPage(bookmark, pages))
        .build();
  }

  public Optional<String> getNextStoryTitle() {
    return currentArticle.map(a -> StringEscapeUtils.escapeXml11(a.getBookmark().getTitle()));
  }

  public Optional<String> getNextStoryPrompt() {
    log.info("Creating prompt for article: {}", currentArticle);
    return currentArticle.map(
        (article) ->
            String.format(PROMPT_FORMAT, getNextStoryTitle().get(), article.numPagesLeft()));
  }

  // TODO: Add star, archive, or delete question at the end.
  public Optional<String> getArticleTextPrompt() throws IOException {
    return currentArticle.map(a -> a.getCurrentPageText());
  }

  public void skipCurrentArticle() throws IOException {
    if (currentArticle.isPresent()) {
      articlesToSkip.add(currentArticle.get().getBookmark().getBookmarkId().getId());
      setNextArticle();
      saveCustomerState();
    }
  }
}
