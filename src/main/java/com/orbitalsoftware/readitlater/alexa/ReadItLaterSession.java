package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.attributes.AttributesManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarkId;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

@Slf4j
public class ReadItLaterSession {
  private static final Integer GET_BOOKMARKS_LIMIT = 100;

  @Getter private final AttributesManager attributesManager;
  private final ObjectMapper mapper;
  private Instapaper instapaperService;

  private static final String KEY_ARTICLES_TO_SKIP = "ArticlesToSkip";
  private static final String KEY_CURRENT_ARTICLE = "CurrentArticle";

  private List<Integer> articlesToSkip = new LinkedList<>();
  private Optional<Article> currentArticle = Optional.empty();
  private ArticleFactory articleFactory;

  public ReadItLaterSession(
      @NonNull Instapaper instapaperService, @NonNull AttributesManager attributesManager)
      throws Exception {
    this.attributesManager = attributesManager;
    this.mapper = new ObjectMapper();
    this.mapper.registerModule(new Jdk8Module());
    articleFactory = new ArticleFactory();
    this.instapaperService = instapaperService;
    loadCustomerState();
  }

  public boolean hasArticle() {
    return currentArticle.isPresent();
  }

  public void incrementArticlePage() throws IOException {
    // Updates reading progress for the page, once the next page is started. This prevents from
    // moving the progress up too aggressively.
    currentArticle =
        currentArticle.map(
            (article) -> {
              Bookmark bookmark = tryUpdatingReadProgress(article);
              article.incrementCurrentPage();
              if (article.isMissingCurrentPage()) {
                return articleFactory.createArticle(article, getBookmarkText(bookmark).get()).get();
              } else {
                return article;
              }
            });
    saveSessionState();
  }

  private Bookmark tryUpdatingReadProgress(Article article) {
    try {
      if (article.getCurrentPage() != 0) {
        return instapaperService.updateReadProgress(
            UpdateReadProgressRequest.builder()
                .progress(article.progressPercentage())
                .bookmarkId(article.getBookmark().getBookmarkId().getId())
                .build());
      } else {
        return article.getBookmark();
      }
    } catch (Exception e) {
      log.warn("Failed to update read progress for bookmark {}. Skipping");
      return article.getBookmark();
    }
  }

  public final Optional<Article> getCurrentArticle() {
    return currentArticle;
  }

  private void saveSessionState() throws IOException {
    if (currentArticle.isPresent()) {
      String articleJson = mapper.writeValueAsString(currentArticle.get());
      log.info("Writing article JSON to session: {}", articleJson);
      attributesManager.getSessionAttributes().put(KEY_CURRENT_ARTICLE, articleJson);
      saveCustomerState();
    }
  }

  // TODO: This shouldn't be public
  public void setNextArticle() throws Exception {
    currentArticle = getNextArticle();
    saveSessionState();
  }

  private void loadCustomerState() throws Exception {
    // Persisted Attributes
    Map<String, Object> persistedAttributes = attributesManager.getPersistentAttributes();
    log.info("Persisted attributes: {}", persistedAttributes);
    String rawCustomerState = (String) persistedAttributes.get(KEY_ARTICLES_TO_SKIP);
    if (rawCustomerState == null) {
      this.articlesToSkip = new LinkedList<>();
    } else {
      try {
        this.articlesToSkip =
            mapper.readValue(rawCustomerState, new TypeReference<List<Integer>>() {});
        log.info("Loaded articles to skip: {}", this.articlesToSkip);
      } catch (IOException e) {
        log.error("Exception while trying to load articles to skip.", e);
      }
    }

    // Session attributes
    Map<String, Object> sessionAttributes =
        Optional.ofNullable(attributesManager.getSessionAttributes()).orElse(new HashMap<>());
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
    attributesManager.setPersistentAttributes(persistedAttributes);
    attributesManager.savePersistentAttributes();
  }

  private void removeDeletedBookmarks(List<BookmarkId> deletedBookmarks) throws IOException {
    int sizeBeforeRemoval = articlesToSkip.size();
    articlesToSkip.removeAll(
        deletedBookmarks.stream().map(b -> b.getId()).collect(Collectors.toList()));
    if (sizeBeforeRemoval != articlesToSkip.size()) {
      saveCustomerState();
    }
  }

  private Optional<String> getBookmarkText(Bookmark bookmark) {
    try {
      return Optional.of(
          StringEscapeUtils.unescapeXml(
              Jsoup.parse(instapaperService.getBookmarkText(bookmark.getBookmarkId().getId()))
                  .text()));
    } catch (Exception e) {
      log.error("An error occurred while trying to get bookmark text.", e);
      return Optional.empty();
    }
  }

  private Optional<Article> articleForBookmark(Bookmark bookmark) throws Exception {
    return getBookmarkText(bookmark).flatMap(text -> articleFactory.createArticle(bookmark, text));
  }

  private Optional<Article> getNextArticle() throws Exception {

    BookmarksListResponse response =
        instapaperService.getBookmarks(
            BookmarksListRequest.builder()
                .limit(Optional.of(GET_BOOKMARKS_LIMIT))
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
      return articleForBookmark(nextBookmark.get());
    } else {
      log.info("No bookmarks found.");
      return Optional.empty();
    }
  }

  // TODO: Add star, archive, or delete question at the end.
  public Optional<String> getArticleTextPrompt() throws IOException {
    return currentArticle.map(a -> a.getCurrentPageText());
  }

  public void skipCurrentArticle() throws Exception {
    if (currentArticle.isPresent()) {
      articlesToSkip.add(currentArticle.get().getBookmark().getBookmarkId().getId());
      setNextArticle();
      saveCustomerState();
    }
  }
}
