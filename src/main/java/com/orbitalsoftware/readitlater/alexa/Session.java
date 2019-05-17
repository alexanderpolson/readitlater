package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.attributes.AttributesManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitalsoftware.harvest.annotations.Timed;
import com.orbitalsoftware.readitlater.article.ArticlePageAudio;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterFatalException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
// TODO: Update article ids to Strings from Integers.
public class Session {

  private static final String KEY_ARTICLES_TO_SKIP = "ArticlesToSkip";
  private static final String KEY_CURRENT_ARTICLE_PAGE = "CurrentArticlePage";

  @Getter private final AttributesManager attributesManager;
  private final ObjectMapper mapper;
  private List<Integer> articlesToSkip = new LinkedList<>();
  @Getter private @NonNull Optional<ArticlePageAudio> currentArticlePageAudio = Optional.empty();

  public Session(@NonNull AttributesManager attributesManager) {
    this.attributesManager = attributesManager;
    this.mapper = new ObjectMapper();
    this.mapper.registerModule(new Jdk8Module());

    Map<String, Object> persistedAttributes = attributesManager.getPersistentAttributes();
    log.info("Persisted attributes: {}", persistedAttributes);
    String rawCustomerState = (String) persistedAttributes.get(KEY_ARTICLES_TO_SKIP);

    if (rawCustomerState != null) {
      try {
        this.articlesToSkip =
            mapper.readValue(rawCustomerState, new TypeReference<List<Integer>>() {});
        log.info("Loaded articles to skip: {}", this.articlesToSkip);
      } catch (IOException e) {
        log.error("Exception while trying to load articles to skip.", e);
      }
    }

    currentArticlePageAudio = deserializeSessionState();
  }

  public void setCurrentArticlePageAudio(
      @NonNull Optional<ArticlePageAudio> currentArticlePageAudio) {
    log.info("Current article page set to: {}", currentArticlePageAudio);
    this.currentArticlePageAudio = currentArticlePageAudio;
    saveSessionState();
  }

  public void clearCurrentArticlePage() {
    setCurrentArticlePageAudio(Optional.empty());
  }

  @Timed
  private Optional<ArticlePageAudio> deserializeSessionState() {
    try {
      Map<String, Object> sessionAttributes =
          Optional.ofNullable(attributesManager.getSessionAttributes()).orElse(new HashMap<>());
      String articlePageJson = (String) sessionAttributes.get(KEY_CURRENT_ARTICLE_PAGE);
      if (articlePageJson != null) {
        return Optional.ofNullable(
            mapper.readValue((String) articlePageJson, ArticlePageAudio.class));
      } else {
        return Optional.empty();
      }
    } catch (final IOException e) {
      throw new ReadItLaterFatalException(
          "An exception occurred when attempting to deserialize session state.", e);
    }
  }

  public final List<Integer> getArticlesToSkip() {
    // TODO: Use ImmutableList
    return new LinkedList<>(articlesToSkip);
  }

  public boolean shouldSkipArticle(@NonNull Integer articleId) {
    return articlesToSkip.contains(articleId);
  }

  public void skipCurrentArticle() {
    if (getCurrentArticlePageAudio().isPresent()) {
      articlesToSkip.add(getCurrentArticlePageAudio().get().getPage().getMetadata().getId());
      clearCurrentArticlePage();
      savePersistedState();
    }
  }

  /**
   * If articles have been skipped, this removes the last skipped article so that the next request
   * for the current article will return the previous skipped one.
   *
   * @return true if the previously skipped article was removed, false if there are no previously
   *     skipped articles.
   */
  public boolean previousArticle() {
    if (articlesToSkip.size() > 0) {
      /// Remove the last skipped article.
      articlesToSkip.remove(articlesToSkip.size() - 1);
      return true;
    } else {
      return false;
    }
  }

  @Timed
  private void savePersistedState() {
    try {
      Map<String, Object> persistedAttributes = new HashMap<>();
      persistedAttributes.put(KEY_ARTICLES_TO_SKIP, mapper.writeValueAsString(articlesToSkip));
      attributesManager.setPersistentAttributes(persistedAttributes);
      attributesManager.savePersistentAttributes();
    } catch (IOException e) {
      throw new ReadItLaterFatalException(
          "An exception occurred while trying to save persisted state.", e);
    }
  }

  @Timed
  private void saveSessionState() {
    try {
      // TODO: What does this do if the article page is empty?
      String articlePageJson = mapper.writeValueAsString(getCurrentArticlePageAudio());
      log.info("Writing article JSON to session: {}", articlePageJson);
      attributesManager.getSessionAttributes().put(KEY_CURRENT_ARTICLE_PAGE, articlePageJson);
    } catch (IOException e) {
      throw new ReadItLaterFatalException(
          "An exception occurred while trying to save session state.", e);
    }
  }
}
