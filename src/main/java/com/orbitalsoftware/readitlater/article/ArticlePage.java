package com.orbitalsoftware.readitlater.article;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class ArticlePage {
  private final ArticleMetadata metadata;

  /** The start character position in the article that the provided pageUriAudio file represents. */
  private final long startPosition;

  /** The end character position in the article that the provided pageUriAudio file represents. */
  private final long endPosition;

  private @NonNull final String pageText;
}
