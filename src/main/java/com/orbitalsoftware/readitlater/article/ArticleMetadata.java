package com.orbitalsoftware.readitlater.article;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class ArticleMetadata {
  private final int id;
  private @NonNull final String title;
  private @NonNull final String originalUrl;
}
