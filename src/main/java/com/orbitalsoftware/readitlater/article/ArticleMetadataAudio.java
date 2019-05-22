package com.orbitalsoftware.readitlater.article;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class ArticleMetadataAudio {

  private final ArticleMetadata metadata;

  /** The uri that refers to this article's title audio MP3 file. */
  private @NonNull final String titleUri;
}
