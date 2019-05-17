package com.orbitalsoftware.readitlater.article;

import com.orbitalsoftware.harvest.annotations.Timed;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.ArchiveBookmarkResponse;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarkId;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.instapaper.StarBookmarkResponse;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.alexa.article.ArticleTextPaginator;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterDependencyException;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Builder
public class ReadItLaterImpl implements ReadItLater {

  private static final Integer GET_BOOKMARKS_LIMIT = 100;
  private static final int POLLY_CHUNK_SIZE = 1200;

  private @NonNull final Instapaper instapaper;
  private @NonNull final TextToSpeechEngine textToSpeechEngine;

  @Timed
  @Override
  public Optional<ArticlePageAudio> getCurrentArticlePage(@NonNull final Session session)
      throws ReadItLaterException {
    if (!session.getCurrentArticlePageAudio().isPresent()) {
      try {
        BookmarksListResponse response =
            instapaper.getBookmarks(
                BookmarksListRequest.builder()
                    .limit(Optional.of(GET_BOOKMARKS_LIMIT))
                    .have(Optional.of(BookmarkId.forIds(session.getArticlesToSkip())))
                    .build());
        // TODO: Add more detailed filtering.

        Optional<Bookmark> nextBookmark =
            response.getBookmarks().stream()
                .filter(bookmark -> !bookmark.getUrl().startsWith("https://www.youtube.com"))
                .filter(bookmark -> !session.shouldSkipArticle(bookmark.getBookmarkId().getId()))
                .findFirst();
        session.setCurrentArticlePageAudio(articlePageAudio(articlePageForBookmark(nextBookmark)));
      } catch (final Exception e) {
        // TODO: Need to update Instapaper to throw more specific exceptions.
        throw new ReadItLaterDependencyException(e);
      }
    }

    return session.getCurrentArticlePageAudio();
  }

  private final ArticleMetadata bookmarkToArticleMetaData(final Bookmark bookmark) {
    return ArticleMetadata.builder()
        .id(bookmark.getBookmarkId().getId())
        .title(bookmark.getTitle())
        .originalUrl(bookmark.getUrl())
        .build();
  }

  private final Article articlePagesForMetaData(@NonNull final ArticleMetadata articleMetadata) {
    try {
      // TODO: Add caching.
      String articleText = instapaper.getBookmarkText(articleMetadata.getId());
      List<String> articlePageTexts =
          ArticleTextPaginator.paginateText(articleText, POLLY_CHUNK_SIZE);

      final List<ArticlePage> articlePages = new ArrayList<>(articlePageTexts.size());
      long startPosition = 0;
      long endPosition = 0;

      for (String articlePageText : articlePageTexts) {
        endPosition = startPosition + articlePageText.length() - 1;
        final ArticlePage articlePage =
            ArticlePage.builder()
                .startPosition(startPosition)
                .endPosition(endPosition)
                .pageText(articlePageText)
                .build();
        articlePages.add(articlePage);
        startPosition = endPosition + 1;
      }

      return Article.builder().pages(articlePages).build();
    } catch (Exception e) {
      throw new ReadItLaterDependencyException(e);
    }
  }

  private Optional<ArticlePage> articlePageForBookmark(final @NonNull Optional<Bookmark> bookmark) {
    return bookmark.map(
        (b) -> {
          final ArticleMetadata articleMetadata = bookmarkToArticleMetaData(b);
          final Article article = articlePagesForMetaData(articleMetadata);

          // Figure out which page the customer is currently in.
          // TODO: Reset the progress if it's been long enough?
          double progress = b.getProgress();
          long articleCharacterPosition = Math.round(progress * article.length());

          // Iterate through the pages until we find the page we should be in.
          for (final ArticlePage articlePage : article.getPages()) {
            if (articleCharacterPosition >= articlePage.getStartPosition()
                && articleCharacterPosition <= articlePage.getEndPosition()) {
              return articlePage;
            }
          }

          log.info(
              "Unable to determine current page of article based on progress. Resetting to first page.");
          return article.getPages().get(0);
        });
  }

  private Optional<ArticlePageAudio> articlePageAudio(
      @NonNull final Optional<ArticlePage> articlePage) {
    return articlePage.map(
        (a) -> {
          return ArticlePageAudio.builder()
              .page(a)
              .pageUri(textToSpeechEngine.textToSpeech(a.getPageText()).toExternalForm())
              .offset(0)
              .build();
        });
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> advanceToNextArticlePage(@NonNull final Session session)
      throws ReadItLaterException {
    if (session.getCurrentArticlePageAudio().isPresent()) {
      final ArticlePageAudio currentArticlePageAudio = session.getCurrentArticlePageAudio().get();
      final Article article =
          articlePagesForMetaData(currentArticlePageAudio.getPage().getMetadata());

      final Optional<ArticlePageAudio> nextArticlePageAudio =
          articlePageAudio(article.nextPage(currentArticlePageAudio.getPage()));

      // Update progress in Instapaper
      try {
        ArticlePage nextArticlePage = nextArticlePageAudio.get().getPage();
        double progress = nextArticlePage.getStartPosition() / article.length();
        final UpdateReadProgressRequest updateReadProgressRequest =
            UpdateReadProgressRequest.builder()
                .bookmarkId(nextArticlePage.getMetadata().getId())
                .progress(progress)
                .build();
        instapaper.updateReadProgress(updateReadProgressRequest);
      } catch (Exception e) {
        log.warn("Failure when updating progress of article to Instapaper.", e);
      }
      session.setCurrentArticlePageAudio(nextArticlePageAudio);
    }

    return getCurrentArticlePage(session);
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> skipToNextArticle(@NonNull final Session session)
      throws ReadItLaterException {
    session.skipCurrentArticle();
    return getCurrentArticlePage(session);
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> previousArticle(@NonNull final Session session)
      throws ReadItLaterException {
    if (session.previousArticle()) {
      return getCurrentArticlePage(session);
    } else {
      return Optional.empty();
    }
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> archiveAndGetNextArticlePage(@NonNull final Session session)
      throws ReadItLaterException {
    if (session.getCurrentArticlePageAudio().isPresent()) {
      try {
        final ArticleMetadata articleMetadata =
            session.getCurrentArticlePageAudio().get().getPage().getMetadata();
        ArchiveBookmarkRequest archiveBookmarkRequest =
            ArchiveBookmarkRequest.builder().bookmarkId(articleMetadata.getId()).build();
        ArchiveBookmarkResponse archiveBookmarkResponse =
            instapaper.archiveBookmark(archiveBookmarkRequest);
        log.info("Successfully archived bookmark {}", archiveBookmarkResponse.getBookmark());
        session.clearCurrentArticlePage();
      } catch (Exception e) {
        log.warn(
            "Exception occurred while trying to archive a bookmark. Adding to skip list instead for now.");
        // TODO: Figure out how to handle this better.
        session.skipCurrentArticle();
      }
    }

    return getCurrentArticlePage(session);
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> favoriteAndGetNextArticlePage(@NonNull final Session session)
      throws ReadItLaterException {
    if (session.getCurrentArticlePageAudio().isPresent()) {
      try {
        final ArticleMetadata articleMetadata =
            session.getCurrentArticlePageAudio().get().getPage().getMetadata();
        StarBookmarkRequest starBookmarkRequest =
            StarBookmarkRequest.builder().bookmarkId(articleMetadata.getId()).build();
        StarBookmarkResponse archiveBookmarkResponse = instapaper.starBookmark(starBookmarkRequest);
        log.info("Successfully favorited bookmark {}", archiveBookmarkResponse.getBookmark());
      } catch (Exception e) {
        log.warn(
            "Exception occurred while trying to star a bookmark. Adding to skip list instead for now.");
      }
    }

    return archiveAndGetNextArticlePage(session);
  }

  @Timed
  @Override
  public Optional<ArticlePageAudio> deleteAndGetNextArticlePage(@NonNull final Session session)
      throws ReadItLaterException {
    if (session.getCurrentArticlePageAudio().isPresent()) {
      try {
        final ArticleMetadata articleMetadata =
            session.getCurrentArticlePageAudio().get().getPage().getMetadata();
        DeleteBookmarkRequest deleteBookmarkRequest =
            DeleteBookmarkRequest.builder().bookmarkId(articleMetadata.getId()).build();
        instapaper.deleteBookmark(deleteBookmarkRequest);
        session.clearCurrentArticlePage();
      } catch (Exception e) {
        log.warn(
            "Exception occurred while trying to delete a bookmark. Adding to skip list instead for now.");
        // TODO: Figure out how to handle this better.
        session.skipCurrentArticle();
      }
    }

    return getCurrentArticlePage(session);
  }
}
