package com.orbitalsoftware.readitlater.alexa;

import com.orbitalsoftware.instapaper.Bookmark;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Responsible for creating {@link Article} objects, based on page and other limits. */
@Slf4j
@RequiredArgsConstructor
public class ArticleFactory {

  private static final int MAX_NUM_PAGES = 10;
  private static final int MAX_PAGE_LENGTH = 800;

  public Optional<Article> createArticle(Article article, String articleText) {
    return createArticle(
        article.getBookmark(), articleText, (numPages) -> article.getCurrentPage() - 1);
  }

  public Optional<Article> createArticle(Bookmark bookmark, String articleText) {
    return createArticle(
        bookmark, articleText, (numPages) -> calculateCurrentPageIndex(bookmark, numPages));
  }

  private Optional<Article> createArticle(
      Bookmark bookmark, String articleText, Function<Integer, Integer> startIndexFunction) {
    List<String> pages = ArticleTextPaginator.paginateText(articleText, MAX_PAGE_LENGTH);
    log.info("Calculated pages from bookmark text");

    int startIndex = startIndexFunction.apply(pages.size());
    int endIndex =
        (startIndex + MAX_NUM_PAGES < pages.size() ? startIndex + MAX_NUM_PAGES : pages.size() - 1);
    log.info("Article has {} pages.", pages.size());
    log.info("Pages Start index: {}, End index: {}", startIndex, endIndex);
    return Optional.of(
        Article.builder()
            .currentPage(startIndex + 1)
            .pageOffset(startIndex)
            .pageLengths(pages.stream().map(String::length).collect(Collectors.toList()))
            .bookmark(bookmark)
            .pages(pages.subList(startIndex, endIndex + 1))
            .build());
  }

  private int calculateCurrentPageIndex(Bookmark bookmark, int articlePageCount) {
    // A return value of 0 essentially means the article hasn't been read yet or at least not gotten
    // past the first page. This doesn't feel quite right as it's muddling the meaning of this data
    // because of how state is updated between requests (progress for a page is only updated once
    // the
    // next page is requested.
    // TODO: Reevaluate this and in general clean up this code.
    // thing.
    return Double.valueOf((Math.floor(bookmark.getProgress() * articlePageCount))).intValue();
  }
}
