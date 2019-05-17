package com.orbitalsoftware.readitlater.article;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class Article {

  private final List<ArticlePage> pages;

  public final long length() {
    return pages.stream().mapToInt((p) -> p.getPageText().length()).sum();
  }

  public final Optional<ArticlePage> nextPage(@NonNull final ArticlePage currentPage) {
    // TODO: For larger articles it will be quicker on average to do a binary search.
    // Find the page that is nearest to the current page's last character position + 1
    long nextPosition = currentPage.getEndPosition() + 1;

    if (nextPosition < length()) {
      for (final ArticlePage articlePage : getPages()) {
        // Is nextPosition between the start and end position of this page?
        if (articlePage.getStartPosition() <= nextPosition
            && articlePage.getEndPosition() >= nextPosition) {
          return Optional.of(articlePage);
        }
      }
    }

    return Optional.empty();
  }
}
