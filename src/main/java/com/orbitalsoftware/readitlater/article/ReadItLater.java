package com.orbitalsoftware.readitlater.article;

import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterException;
import java.util.Optional;

/**
 * Work in progress API for the skill to interact with to get and advance article queue positions.
 * The names are not quite right, but they'll do for now.
 */
public interface ReadItLater {

  /**
   * Gets the metadata and title audio for the article that is at the top of the customer's article
   * queue. This * disregards skipped articles, as well as those that are not supported on the
   * current device such as videos on a headless device.
   *
   * <p>NOTE: Called on launch request.
   *
   * @param session
   * @return
   * @throws ReadItLaterException
   */
  Optional<ArticleMetadataAudio> getCurrentArticleTitle(Session session)
      throws ReadItLaterException;

  /**
   * Gets the current page of the article that is at the top of the customer's article queue. This
   * disregards skipped articles, as well as those that are not supported on the current device such
   * as videos on a headless device.
   *
   * <p>NOTE: Called on launch request.
   *
   * @return the next page for the customer to listen to, or {@link Optional#empty()} if there isn't
   *     one.
   */
  Optional<ArticlePageAudio> getCurrentArticlePage(Session session) throws ReadItLaterException;

  /**
   * Advances the customer's current article position one spot and returns the next page. If the
   * article has already been read to the end, this will return {@link Optional#empty()}.
   *
   * <p>NOTE: Called on playback nearly finished
   *
   * @return the next page for the customer to listen to or {@link Optional#empty()} if no pages
   *     remain for the current article.
   */
  Optional<ArticlePageAudio> advanceToNextArticlePage(Session session) throws ReadItLaterException;

  /**
   * Starts the current article over.
   *
   * @param session
   * @return
   * @throws ReadItLaterException
   */
  Optional<ArticleMetadataAudio> startCurrentArticleOver(Session session)
      throws ReadItLaterException;

  /**
   * Skips the current article and moves to the next article's first page.
   *
   * <p>NOTE: Called on skip.
   *
   * @return the next page for the customer to listen to.
   */
  Optional<ArticleMetadataAudio> skipToNextArticle(Session session) throws ReadItLaterException;

  /**
   * Moves back to the previously skipped article, if one exists.
   *
   * <p>NOTE: Called on previous
   *
   * @return the first page of the previous article if one exists. If not, then {@link
   *     Optional#empty()} is returned.
   */
  Optional<ArticleMetadataAudio> previousArticle(Session session) throws ReadItLaterException;

  /**
   * Archives the current article and moves to the next article's first page.
   *
   * @return the next page for the customer to listen to.
   */
  Optional<ArticleMetadataAudio> archiveAndGetNextArticlePage(Session session)
      throws ReadItLaterException;

  /**
   * Favorites and Archives the current article and moves to the next article's first page.
   *
   * @return the next page for the customer to listen to.
   */
  Optional<ArticleMetadataAudio> favoriteAndGetNextArticlePage(Session session)
      throws ReadItLaterException;

  /**
   * Deletes the current article and moves to the next article's first page.
   *
   * @return the next page for the customer to listen to.
   */
  Optional<ArticleMetadataAudio> deleteAndGetNextArticlePage(Session session)
      throws ReadItLaterException;
}
